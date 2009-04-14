package de.ingrid.admin;

import java.util.List;

public interface IDataType {

    String getName();

    String getDisplayName();

    boolean isHidden();

    List<IDataType> getIncludedDataTypes();
}
