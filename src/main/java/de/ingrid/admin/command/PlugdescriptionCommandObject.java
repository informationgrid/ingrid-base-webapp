/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.ingrid.admin.Config;
import org.apache.commons.lang.ArrayUtils;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.StringUtils;
import de.ingrid.admin.object.IDataType;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

public class PlugdescriptionCommandObject extends PlugDescription {

    private Config config;
    private String realWorkingDir = null;
    
    private String newPassword = null;
    
    private Map<String,String[]> datatypesOfIndex = new HashMap<String,String[]>(); 

    // needed for xmlserializer
    public PlugdescriptionCommandObject() {

    }

    public PlugdescriptionCommandObject(final File file, Config config) throws IOException {
        this.config = config;
        if (file.exists()) {
            final XMLSerializer serializer = new XMLSerializer();
            serializer.aliasClass(PlugDescription.class.getName(), PlugDescription.class);
            putAll((PlugDescription) serializer.deSerialize(file));
        }
    }

    public String getTest() {
        return "test";
    }

    public void setDataTypes(final String... types) {
        remove(DATA_TYPE);
        for (final String type : types) {
            addDataType(type);
        }
    }

    public void addIncludedDataTypes(final IDataType... types) {
        // check all data types
        for (final String dataType : getDataTypes()) {
            // find correct idatatype
            for (final IDataType type : types) {
                if (type.getName().equals(dataType)) {
                    // if found add all included data types
                    if (type.getIncludedDataTypes() != null) {
                        for (final IDataType include : type.getIncludedDataTypes()) {
                            addDataType(include.getName());
                        }
                    }
                }
            }
        }
    }
    
    public void removePartner(final String partner) {
        removeFromList(PlugDescription.PARTNER, partner);
    }

    public void removeProvider(final String provider) {
        removeFromList(PlugDescription.PROVIDER, provider);
    }

    @Override
    public void addDataType(final String type) {
        if (!StringUtils.isEmptyOrWhiteSpace(type) && !existsInArray(getDataTypes(), type)) {
            super.addDataType(type);
        }
    }

    @Override
    public void addPartner(final String partner) {
        if (!StringUtils.isEmptyOrWhiteSpace(partner) && !existsInArray(getPartners(), partner)) {
            super.addPartner(partner);
        }
    }

    @Override
    public void addProvider(final String provider) {
        if (!StringUtils.isEmptyOrWhiteSpace(provider) && !existsInArray(getProviders(), provider)) {
            super.addProvider(provider);
        }
    }

    @Override
    public void setOrganisationAbbr(final String provider) {
        super.setOrganisationAbbr(provider);
        addProvider(provider);
    }

    @Override
    public void setOrganisationPartnerAbbr(final String partner) {
        super.setOrganisationPartnerAbbr(partner);
        addPartner(partner);
    }

    @Override
    public int getIplugAdminGuiPort() {
        if (containsKey(PlugDescription.IPLUG_ADMIN_GUI_PORT)) {
            return super.getIplugAdminGuiPort();
        }
        return getOriginalPort();
    }

    public int getOriginalPort() {
        Integer port = config.getWebappPort();
        if (port == null) return 8088;
        return port;
    }

    public String getOriginalWorkingDir() {
        return System.getProperty(IKeys.WORKING_DIR);
    }

    @Override
    public void setWorkinDirectory(final File workinDirectory) {
        if (workinDirectory != null) {
            super.setWorkinDirectory(workinDirectory);
        }
    }

    private boolean existsInArray(final String[] things, final String thing) {
        for (final String t : things) {
            if (t.equals(thing)) {
                return true;
            }
        }
        return false;
    }

    public String getRealWorkingDir() {
        return realWorkingDir;
    }

    public void setRealWorkingDir(String realWorkingDir) {
        this.realWorkingDir = realWorkingDir;
        setWorkinDirectory( new File( realWorkingDir) ); 
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    
    /**
     * 
     */
    public String[] getDatatypesOfIndex(Object key) { 
        return datatypesOfIndex.get(key); 
    } 

    public void setDatatypesOfIndex(String key, String[] value) { 
        datatypesOfIndex.put(key, value); 
    } 

    public Map<String,String[]> getDatatypesOfIndex() { 
        return datatypesOfIndex; 
    } 

    public void setDatatypesOfIndex(Map<String,String[]> currentCarrierMap) { 
        this.datatypesOfIndex = currentCarrierMap; 
    }
    
    public void addDatatypesOfIndex(String indexId, String type) {
        String[] indexDatatypes = getDatatypesOfIndex(indexId);
        if (!StringUtils.isEmptyOrWhiteSpace(type) && (indexDatatypes == null || !existsInArray(indexDatatypes, type))) {
            setDatatypesOfIndex( indexId, (String[]) ArrayUtils.add( indexDatatypes, type ) );
        }
    }
    
    public void addDatatypesOfAllIndices(String type) {
        for (String index : getDatatypesOfIndex().keySet()) {
            addDatatypesOfIndex( index, type );
        }
    }
}
