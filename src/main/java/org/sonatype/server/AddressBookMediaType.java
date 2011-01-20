package org.sonatype.server;

import org.sonatype.rest.api.ServiceHandlerMediaType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class AddressBookMediaType implements ServiceHandlerMediaType {
    public String entries;


    public AddressBookMediaType(){
    }

    @Override
    public AddressBookMediaType visit(Object object) {
        List<String> entries = (List<String>) object;
        StringBuilder b = new StringBuilder();
        for(String s: entries) {
            b.append(s);
            b.append(" - ");
        }
        this.entries = b.toString();
        return this;
    }
}

