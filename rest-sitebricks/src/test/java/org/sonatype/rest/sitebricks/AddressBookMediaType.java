package org.sonatype.rest.sitebricks;

import org.sonatype.rest.api.ServiceHandlerMediaType;

import java.util.List;

public class AddressBookMediaType implements ServiceHandlerMediaType<List<String>> {
    public String entries;

    public AddressBookMediaType(){
    }

    @Override
    public AddressBookMediaType visit(List<String> object) {
        List<String> entries = (List<String>) object;
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

    public String getEntries(){
        return entries;
    }

    public void setEntries(String entries){
        this.entries = entries;
    }

}
