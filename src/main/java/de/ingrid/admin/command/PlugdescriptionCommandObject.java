package de.ingrid.admin.command;

import java.io.File;

import de.ingrid.utils.PlugDescription;

public class PlugdescriptionCommandObject extends PlugDescription {

    public void setPartner(final String partner) {
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

    public void setProvider(final String provider) {
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

    public void setDatatypes(final String... strings) {
        for (final String string : strings) {
            addDataType(string);
        }
    }

    @Override
    public int getIplugAdminGuiPort() {
        if (super.containsKey(PlugDescription.IPLUG_ADMIN_GUI_PORT)) {
            return super.getIplugAdminGuiPort();
        }
        return 8082;
    }

    @Override
    public void setWorkinDirectory(File workinDirectory) {
        if (workinDirectory != null) {
            super.setWorkinDirectory(workinDirectory);
        }
    }
}
