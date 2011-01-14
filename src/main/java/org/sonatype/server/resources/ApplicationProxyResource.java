package org.sonatype.server.resources;

import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.resource.Singleton;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy;
import org.atmosphere.jersey.AtmosphereFilter;
import org.atmosphere.jersey.SuspendResponse;
import org.atmosphere.jersey.util.JerseySimpleBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.client.Application;
import org.sonatype.client.Applications;
import org.sonatype.etag.ETagChangeListener;
import org.sonatype.etag.ETagChangeListenerEvent;
import org.sonatype.server.store.ApplicationStore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.atmosphere.cpr.BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY.*;

@Path("/approxy")
@Singleton
public class ApplicationProxyResource {

    private final ApplicationStore applicationStore;
    private final List<Variant> acceptedVariants;
    private static final Logger log = LoggerFactory.getLogger(ApplicationProxyResource.class);

    @Inject
    public ApplicationProxyResource(ApplicationStore applicationStore) {
        this.applicationStore = applicationStore;
        acceptedVariants = VariantListBuilder.newInstance()
                .mediaTypes(MediaType.APPLICATION_JSON_TYPE)
                .encodings("identity")
                .add()
                .mediaTypes(MediaType.APPLICATION_JSON_TYPE)
                .encodings("gzip").add()
                .build();
    }

    @GET
    @Produces(APPLICATION_JSON)
    public SuspendResponse getMessages(final @Context Request request) {

        final String eTagString = applicationStore.getETag();
        final ResponseBuilder responseBuilder = request.evaluatePreconditions(new EntityTag(eTagString));
        final Variant variant = request.selectVariant(acceptedVariants);

        // If null, that means the ETag hasn't changed, hence we can long poll the connection.
        if (responseBuilder != null) {
            log.debug("Request: " + request + " will be suspended with eTag value: " + eTagString);

            final Broadcaster broadcaster;
            try {
                broadcaster = BroadcasterFactory.getDefault().get(JerseySimpleBroadcaster.class, request.toString());
                // Destroy the instance once we resumed.
                broadcaster.setBroadcasterLifeCyclePolicy(new BroadcasterLifeCyclePolicy.Builder().policy(EMPTY_DESTROY).build());
            } catch (IllegalAccessException e) {
                throw new WebApplicationException(e);
            } catch (InstantiationException e) {
                throw new WebApplicationException(e);
            }
            
            final ETagChangeListener eTagChangeListener = new ETagChangeListener() {
                @Override
                public void onChange(ETagChangeListenerEvent change) {
                    change.cancel();
                    log.debug("A suspended request with previous eTag value {} changed to value: {}"
                            , change.newValue(), change.previousValue());

                    Response response = prepareResponse(variant);
                    broadcaster.broadcast(response);
                }
            };

            return new SuspendResponse.SuspendResponseBuilder()
                    .broadcaster(broadcaster)
                    .addListener(new AtmosphereEventsListener(applicationStore, eTagChangeListener, responseBuilder))
                    .outputComments(false)
                    .resumeOnBroadcast(true)
                    .period(5 * 1000 * 60, TimeUnit.MILLISECONDS)     // TODO: Must be configurable.
                    .build();
        }

        log.debug("Request {} will not be suspended.", request);
        throw new WebApplicationException(prepareResponse(variant));
    }

    @PUT
    @Consumes(APPLICATION_JSON)
    @Path("{appname}")
    public Response setMessage(@PathParam("appname") String appName, Application application) {

        log.warn(String.format("setMessage for Application {}", appName));
        if (!appName.equals(application.getName())) {
            return Response.status(Response.Status.FORBIDDEN).entity("Services for the "
                    + application.getName()
                    + " application can not be posted to the "
                    + appName + " application").build();
        }

        Response response = validate(application);
        if (response != null) return response;

        applicationStore.addApplication(application);

        URI location = UriBuilder.fromResource(getClass()).build(application.getName());
        return Response.created(location).build();
    }

    /**
     * TODO: This is where content negociation logic could happens.
     * @param application
     * @return
     */
    private Response validate(Application application) {
        if (application == null) {
            throw new NullPointerException("application is null");
        }

        if (!applicationStore.getEnvironment().equals(application.getEnvironment())) {
            return Response.status(Response.Status.FORBIDDEN).entity("Services for the "
                    + application.getEnvironment()
                    + " environment can not be stored to the " + applicationStore.getEnvironment()
                    + " environment").build();
        }
        return null;
    }

    @DELETE
    @Path("{appname}")
    public void deleteMessage(@PathParam("appname") String appName) {
        log.warn(String.format("deleteMessage for application {}", appName));
        if (appName == null) {
            throw new NullPointerException("appName is null");
        }

        applicationStore.removeApplication(applicationStore.getApplication(appName));
    }

    private Response prepareResponse(Variant variant) {

        Applications messages = applicationStore.getApplications();

        if (variant != null && variant.getEncoding().equals("gzip")) {
            return Response.ok(messages)
                    .header("Content-Type", APPLICATION_JSON)
                    .header("Content-Encoding", "gzip")
                    .tag(new EntityTag(messages.getETag()))
                    .build();
        } else {
            return Response.ok(messages)
                    .header("Content-Type", APPLICATION_JSON)
                    .tag(new EntityTag(messages.getETag()))
                    .build();
        }
    }

    private final static class AtmosphereEventsListener implements AtmosphereResourceEventListener {

        private final ApplicationStore store;
        private final String eTagString;
        private final ResponseBuilder responseBuilder;
        private final AtomicBoolean isResumed = new AtomicBoolean(false);
        private final ETagChangeListener eTagChangeListener;

        public AtmosphereEventsListener(ApplicationStore store,
                                        ETagChangeListener eTagChangeListener,
                                        ResponseBuilder responseBuilder) {
            this.eTagString = store.getETag();
            this.store = store;
            this.responseBuilder = responseBuilder;
            this.eTagChangeListener = eTagChangeListener;
        }

        @Override
        public void onSuspend(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            store.addETagChangeListener(eTagChangeListener);
        }

        @Override
        public void onResume(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            store.removeETagChangeListener(eTagChangeListener);
            if (event.isResumedOnTimeout() && !isResumed.getAndSet(true)) {
                log.debug("A suspended request with previous eTag value {} will now be resumed with the ETag value: {}"
                        , eTagString, store.getETag());

                ContainerResponse cr = (ContainerResponse) event.getResource().getRequest()
                        .getAttribute(AtmosphereFilter.CONTAINER_RESPONSE);
                if (cr != null) {
                    try {
                        cr.setResponse(responseBuilder.build());
                        cr.write();
                    }
                    catch (Throwable t) {
                        log.debug("onResume", t);
                    }
                }
            }
        }

        @Override
        public void onDisconnect(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            log.debug("Remote client disconnected: " + event.getResource().getRequest().getRemoteAddr());
            store.removeETagChangeListener(eTagChangeListener);
        }

        @Override
        public void onBroadcast(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            log.debug("Broadcast occurred: " + event.getMessage());
        }

        @Override
        public void onThrowable(AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            log.warn("onThrowable", event.throwable());
        }
    }

}
