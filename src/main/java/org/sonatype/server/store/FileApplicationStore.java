package org.sonatype.server.store;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.client.Application;
import org.sonatype.client.Applications;
import org.sonatype.client.ImmutableApplications;
import org.sonatype.client.json.ApplicationJson;
import org.sonatype.client.json.JacksonProxy;
import org.sonatype.etag.ETag;
import org.sonatype.etag.ETagChangeListener;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FileApplicationStore implements ApplicationStore {
    private static final Logger log = LoggerFactory.getLogger(FileApplicationStore.class);

    private static final ApplicationJson APPLICATION_JSON;

    private final File dir;
    private final String dirName;
    private final String environment;

    private final ConcurrentMap<String, Application> localHosts = new ConcurrentHashMap<String, Application>();

    private final ETag etag;

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        APPLICATION_JSON = JacksonProxy.newProxyInstance(mapper, ApplicationJson.class);
    }

    @Inject
    public FileApplicationStore(FileMessageStoreConfig config, ETag etag) {
        if (config.getFileDir() == null) throw new NullPointerException("dir is null");
        if (config.getEnvironment() == null) throw new NullPointerException("environment is null");

        File dir = new File(config.getFileDir());

        dir.mkdirs();
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("dir is not a directory");
        }

        this.dir = dir;
        this.environment = config.getEnvironment();

        String dirName;
        try {
            dirName = dir.getCanonicalPath();
        }
        catch (IOException e) {
            dirName = dir.getAbsolutePath();
        }
        this.dirName = dirName;

        Collection<File> files = toArray(this.dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.indexOf(".json") > 0) {
                    return true;
                }
                return false;
            }
        }));
        
        log.debug(String.format("Loading files %s", files));

        for (File file : files) {
            String fileName = file.getName();

            if (!file.isFile()) {
                log.info("There is an errant directory, {}, sitting in the store directory {}", fileName, this.dirName);
                continue;
            }

            try {
                // Load application
                String contents = FileUtils.readFileToString(file);
                Application application = APPLICATION_JSON.readApplication(contents);

                localHosts.put(application.getName(), application);
            }
            catch (IOException e) {
                log.warn("Could not read or parse announcement {}", file.getAbsolutePath(), e);
            }
            catch (RuntimeException e) {
                log.warn("Unexpected RuntimeException while reading or parsing announcement {}", file.getAbsolutePath(), e);
            }
        }
        this.etag = etag;
    }

    private static <T> Collection<T> toArray(T[] array) {
        if (array != null) {
            return Arrays.asList(array);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getEnvironment() {
        return environment;
    }

    @Override
    public String getETag() {
        return etag.getEtag();
    }

    @Override
    public Application getApplication(String application) {
        if (application == null) {
            throw new NullPointerException("application is null");
        }
        return localHosts.get(application);
    }

    @Override
    public Applications getApplications() {
        return new ImmutableApplications(getETag(), localHosts.values());
    }

    @Override
    public void addApplication(Application application) {
        if (application == null) {
            throw new NullPointerException("application is null");
        }

        localHosts.put(application.getName(), application);

        // changed the application services
        etag.regenerateEtag();

        // write the application
        File file = getFile(application);
        try {
            FileUtils.writeStringToFile(file, APPLICATION_JSON.writeApplication(application));
        }
        catch (IOException e) {
            log.info("Unable to persist application '{}'", application.getName(), e);
        }
    }

    @Override
    public void removeApplication(Application application) {
        if (application == null) {
            throw new NullPointerException("application is null");
        }
        removeApplication(application.getName());
    }

    @Override
    public void removeApplication(String app) {
        if (app == null) {
            throw new NullPointerException("application is null");
        }

        Application application = localHosts.remove(app);

        if (app == null) return;

        etag.regenerateEtag();

        File file = getFile(application);
        if (!file.delete()) {
            log.info("Unable to delete application {}", file.getName());
        } else {
            log.debug("Removed application {}", file.getName());
        }
    }


    @Override
    public void addETagChangeListener(ETagChangeListener l) {
        etag.addChangeListener(l);
    }

    @Override
    public void removeETagChangeListener(ETagChangeListener l) {
        etag.removeChangeListener(l);
    }

    private File getFile(Application plugInMessage) {
        return new File(dir, plugInMessage.getName());
    }
}