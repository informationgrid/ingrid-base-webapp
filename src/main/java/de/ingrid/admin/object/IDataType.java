/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.object;


public interface IDataType {

    public static final String DEFAULT = "default";

    public static final String DSC_ECS = "dsc_ecs";

    public static final String DSC_ECS_ADDRESS = "dsc_ecs_address";

    public static final String DSC_CSW = "dsc_csw";

    public static final String DSC_RESEARCH = "dsc_research";

    public static final String DSC_OTHER = "dsc_other";

    public static final String METADATA = "metadata";

    public static final String FIS = "fis";

    public static final String LAW = "law";

    public static final String ADDRESS = "address";

    public static final String EXCEL = "excel";

    public static final String XML = "xml";

    String getName();

    boolean isHidden();

    IDataType[] getIncludedDataTypes();
    
    boolean getIsForced();
}
