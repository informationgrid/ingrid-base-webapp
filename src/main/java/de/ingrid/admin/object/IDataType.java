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
