package org.sonatype.server;

import org.sonatype.rest.api.ServiceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBookServiceEntity implements ServiceEntity {

    private final ConcurrentHashMap<String, List<String>> book = new ConcurrentHashMap<String, List<String>>();

    public String createAddressBook(String id) {
        book.put(id, new ArrayList<String>());
        return "created";
    }

    public List getAddressBook(String id) {
        return book.get(id);
    }

    public String updateAddressBook(String id, String value) {
        List<String> list = book.get(id);
        list.add(value);
        book.put(id, list);
        return "updated";
    }

    public String deleteAddressBook(String id) {
        book.remove(id);
        return "updated";
    }


}
    