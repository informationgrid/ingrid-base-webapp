package de.ingrid.admin.object;


public interface IDataType {

    String getName();

    boolean isHidden();

    IDataType[] getIncludedDataTypes();
}
