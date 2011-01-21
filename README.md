The REST Deflector Framework allow programmatic creation of REST resource that can be deployed inside a variety of framework like jax-rs, Sitebricks, etc. The main idea is to able to define Service Definition, which gets translated into specific framework resource native code.

        ServiceEntity serviceEntity = new AddressBookServiceEntity();
        bind(ServiceEntity.class).toInstance(serviceEntity);

        serviceDefinition.withPath("/{method}/{id}")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "updateAddressBook"))
                .usingEntity(serviceEntity)
                .bind();

Request will be delegated to the {@link ServiceEntity}'s method using the information contained within passed
{@link ServiceHandler}. Using the example above, a request to:

     PUT /createAddressBook/myBook

will be mapped on the server side to the ServiceHandler defined as

    new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook")

which will invoke the ServiceEntity implementation

    serviceEntity.createAddressBook(myBook) 


If JAXRS generator is used, the above ServiceDefinition would translate into.

@Path("/{service}/{method}/{id}/")
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Consumes({APPLICATION_JSON, APPLICATION_XML})
public class ServiceDescriptionResource {

    private Logger logger = LoggerFactory.getLogger(ServiceDescriptionResource.class);

    @Inject
    ServiceEntity serviceEntity;

    @Inject
    ServiceHandlerMapper mapper;

    @Inject
    ServiceHandlerMediaType producer;

    @PUT
    public Response put(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP PUT: Generated Resource invocation for method {} with id {}", service, value);
        URI location = UriBuilder.fromResource(getClass()).build(new String[]{"", ""});
        Object response = createResponse("put", service, value, null);
        return Response.created(location).entity(response).build();
    }

    ....
 }

The client side can also be generated from the ServiceDefinition. A simple interface, which under the hood build on top of AsyncHttpClient, can then be used for REST operation like GET, PUT, POST and DELETE.







