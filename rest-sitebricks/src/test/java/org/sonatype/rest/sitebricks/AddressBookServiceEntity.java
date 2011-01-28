package org.sonatype.rest.sitebricks;

import org.sonatype.rest.api.ServiceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBookServiceEntity implements ServiceEntity {

    public final static String APPLICATION = "application";
    public final static String JSON = "org.sonatype.rest.tests.addressBook+json";
    public final static String XML = "org.sonatype.rest.tests.addressBook+xml";

    private final ConcurrentHashMap<String, List<String>> book = new ConcurrentHashMap<String, List<String>>();

    public String createAddressBook(String id) {
        book.put(id, new ArrayList<String>());
        return "created";
    }

    public List getAddressBook(String id) {
        List<String> list = book.get(id);
        return list == null ? new ArrayList<String>(): list;
    }

    public String updateAddressBook(String id, String value) {
        List<String> list = book.get(id);
        
        if (list == null) {
            throw new IllegalStateException("No address book have been created for " + id);
        }

        list.add(value);
        book.put(id, list);
        return "updated";
    }

    public String updateAddressBook(String id, String value, String value2) {
        List<String> list = book.get(id);

        if (list == null) {
            throw new IllegalStateException("No address book have been created for " + id);
        }

        list.add(value);
        list.add(value2);        
        book.put(id, list);
        return "updated";
    }

    public String deleteAddressBook(String id) {
        book.remove(id);
        return "updated";
    }

    @Override
    public List<String> version() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(APPLICATION + "/" + JSON);
        list.add(APPLICATION + "/" + XML);
        return list;
    }
}
    