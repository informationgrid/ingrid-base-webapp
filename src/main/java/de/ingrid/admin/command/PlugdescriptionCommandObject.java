package de.ingrid.admin.command;

import java.io.File;

import de.ingrid.admin.StringUtils;
import de.ingrid.utils.PlugDescription;

public class PlugdescriptionCommandObject extends PlugDescription {

    public void setDataTypes(final String... types) {
        for (final String type : types) {
            addDataType(type);
        }
    }

    public void removePartner(final String partner) {
        removeFromList(PlugDescription.PARTNER, partner);
    }

    public void removeProvider(final String provider) {
        removeFromList(PlugDescription.PROVIDER, provider);
    }

    @Override
    public void addPartner(final String partner) {
        if (!StringUtils.isEmptyOrWhiteSpace(partner)) {
            for (final String p : getPartners()) {
                if (p.equals(partner)) {
                    return;
                }
            }
            super.addPartner(partner);
        }
    }

    @Override
    public void addProvider(final String provider) {
        if (!StringUtils.isEmptyOrWhiteSpace(provider)) {
            for (final String p : getProviders()) {
                if (p.equals(provider)) {
                    return;
                }
            }
            super.addProvider(provider);
        }
    }

    @Override
    public void setOrganisation(final String partner) {
        super.setOrganisation(partner);
        addPartner(partner);
    }

    @Override
    public void setOrganisationAbbr(final String provider) {
        super.setOrganisationAbbr(provider);
        addProvider(provider);
    }

    @Override
    public int getIplugAdminGuiPort() {
        if (containsKey(PlugDescription.IPLUG_ADMIN_GUI_PORT)) {
            return super.getIplugAdminGuiPort();
        }
        return 8082;
    }

    @Override
    public void setWorkinDirectory(final File workinDirectory) {
        if (workinDirectory != null) {
            super.setWorkinDirectory(workinDirectory);
        }
    }
}
