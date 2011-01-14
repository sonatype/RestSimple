package org.sonatype.client.json;


import org.sonatype.client.Application;
import org.sonatype.client.Applications;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface ApplicationJson {

    Application readApplication(String source);

    Application readApplication(InputStream source);

    String writeApplication(Application application);

    List<Application> readApplications(String source);

    List<Application> readApplications(InputStream source);

    String writeApplications(Collection<Application> applications);

    String writeApplications(Applications applications);
}