package de.ingrid.admin;

import de.ingrid.utils.PlugDescription;

public class PlugdescriptionCommandObject extends PlugDescription {

    public void setPartner(String partner) {
        addPartner(partner);
    }

    public String getPartner() {
        String partner = null;
        if (getPartners() != null) {
            if (getPartners().length > 0) {
                partner = getPartners()[0];
            }
        }
        return partner;
    }

    public void setProvider(String provider) {
        addProvider(provider);
    }

    public String getProvider() {
        String provider = null;
        if (getProviders() != null) {
            if (getProviders().length > 0) {
                provider = getProviders()[0];
            }
        }
        return provider;
    }
    
    public void setDatatypes(String... strings) {
        for (String string : strings) {
            addDataType(string);
        }
    }

}
