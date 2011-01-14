package org.sonatype.server.store;

// TODO: This needs to be injectable
public class FileMessageStoreConfig {
    private final String fileDir;
    private final String environment;

    // TODO Needs a ~/.m2 kind of things
    public FileMessageStoreConfig(){
        this.fileDir = "./tests/";
        this.environment = "test";
    }

    public FileMessageStoreConfig(String fileDir, String environment) {
        this.fileDir = fileDir;
        this.environment = environment;
    }

    public String getFileDir() {
        return fileDir;
    }

    public String getEnvironment() {
        return environment;
    }
}
