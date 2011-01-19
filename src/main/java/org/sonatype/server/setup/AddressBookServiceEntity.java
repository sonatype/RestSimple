package org.sonatype.server.setup;

import org.sonatype.rest.ServiceEntity;

public class AddressBookServiceEntity implements ServiceEntity{

    public String createAddressBook(String id) {
        return "foo" + id;
    }

    public String getAddressBook(String id) {
        return "foo" + id;
    }


}
    