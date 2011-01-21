package org.sonatype.rest.client;


import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.SimpleAsyncHttpClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * This stub will be generated.
 */
public class AHCClientStub {

    private final SimpleAsyncHttpClient sahc;

    private final String remoteServerUri;

    public AHCClientStub(SimpleAsyncHttpClient sahc, String remoteServerUri) {
        this.sahc = sahc;
        this.remoteServerUri = remoteServerUri;
    }

    public Response doGet(String... paths) {

        List<String> uriPaths = Arrays.asList(paths);
        String path = createUri("getAddressBook", uriPaths);


        try {
            return sahc.get(remoteServerUri + path).get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Response doHead(String... paths) {

        List<String> uriPaths = Arrays.asList(paths);
        String path = createUri("getAddressBook", uriPaths);


        try {
            return sahc.head(remoteServerUri + path).get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Response doPut(String... paths) {

        List<String> uriPaths = Arrays.asList(paths);
        String path = createUri("createAddressBook", uriPaths);

        try {
            return sahc.put(remoteServerUri + path, null).get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Response doPost(Map<String,String> maps, String... paths) {

        FluentStringsMap ahcMap = new FluentStringsMap();
        for (Map.Entry<String,String> e : maps.entrySet() ) {
            ahcMap.add(e.getKey(), e.getValue());
        }

        List<String> uriPaths = Arrays.asList(paths);
        String path = createUri("updateAddressBook", uriPaths);

        Request r = new RequestBuilder().setMethod("POST").setParameters(ahcMap).setUrl(remoteServerUri + path).build();
        try {
            return sahc.post(r, null).get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Response doDelete(String... paths) {

        List<String> uriPaths = Arrays.asList(paths);
        String path = createUri("createAddressBook", uriPaths);

        try {
            return sahc.put(remoteServerUri + path, null).get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String createUri(String remoteMethod, List<String> uriPaths) {
        StringBuilder b = new StringBuilder("/");
        b.append(remoteMethod).append("/");
        for (String s: uriPaths) {
            b.append(s).append("/");
        }

        String path = b.toString();
        return path.substring(0, path.length() -1);
    }

}
