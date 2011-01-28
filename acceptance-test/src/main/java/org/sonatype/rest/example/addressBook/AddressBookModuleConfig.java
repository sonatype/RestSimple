package org.sonatype.rest.example.addressBook;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class AddressBookModuleConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new AddressBookServletModule());
    }
}
