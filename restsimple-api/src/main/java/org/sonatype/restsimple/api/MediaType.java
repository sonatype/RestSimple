package org.sonatype.restsimple.api;

/**
 * Represent a Media type consumed or produced by a resource.
 */
public final class MediaType {
    public static MediaType JSON = new MediaType("application","json");
    public static MediaType XML = new MediaType("application","xml");
    public static MediaType TEXT = new MediaType("text","plain");
    public static MediaType HTML = new MediaType("text","html");
    
    private final String type;
    private final String subType;

    public MediaType() {
        this("*", "*");
    }

    public MediaType(String type) {
        this(type, "*");
    }

    @Override
    public String toString() {
        return toMediaType();
    }

    public MediaType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    /**
     * Return the Media's Type, e.g the first part of "application/json".
     * @return the Media's Type, e.g the first part of "application/json".
     */
    public String type(){
        return type;
    }

    /**
     * Return the Media's subtype, e.g the last part of "application/json".
     * @return he Media's subtype, e.g the last part of "application/json".
     */
    public String subType(){
        return subType;
    }

    /**
     * Return the complete Media Type.
     * @return
     */
    public String toMediaType(){
        return type + "/" + subType();
    }
}