package de.ingrid.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.admin.object.IDataType;

@Service
@Qualifier("default")
public class DefaultDataType implements IDataType {

    private static final ArrayList<IDataType> EMPTY = new ArrayList<IDataType>();

    public String getDisplayName() {
        return "Default";
    }

    public List<IDataType> getIncludedDataTypes() {
        return EMPTY;
    }

    public String getName() {
        return "default";
    }

    public boolean isHidden() {
        return true;
    }

}
