package org.sonatype.rest.api;

/**
 * Represent a Content-Type used with the Accept header.
 */
public final class MediaType {
    public static MediaType JSON = new MediaType("application","json");
    public static MediaType XML = new MediaType("application","xml");

    private final String type;
    private final String subType;

    public MediaType() {
        this("*", "*");
    }

    public MediaType(String type) {
        this(type, "*");
    }

    public MediaType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    public String type(){
        return type;
    }

    public String subType(){
        return subType;
    }

    public String toMediaType(){
        return type + "/" + subType();
    }
}