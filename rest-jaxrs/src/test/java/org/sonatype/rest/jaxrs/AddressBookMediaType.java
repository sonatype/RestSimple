package org.sonatype.rest.jaxrs;

import org.sonatype.rest.api.ServiceHandlerMediaType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class AddressBookMediaType implements ServiceHandlerMediaType<List<String>> {
    public String entries;

    public AddressBookMediaType(){
    }

    @Override
    public AddressBookMediaType visit(List<String> entries) {
        StringBuilder b = new StringBuilder();

        if (entries == null) {
            this.entries = "";
        } else {
            for(String s: entries) {
                b.append(s);
                b.append(" - ");
            }
            this.entries = b.toString();
        }
        return this;
    }
}
