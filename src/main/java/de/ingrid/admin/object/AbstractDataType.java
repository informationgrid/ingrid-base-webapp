package de.ingrid.admin.object;

abstract public class AbstractDataType implements IDataType {

    private final String _name;

    private boolean _hidden = false;
    
    private boolean _force = false;

    private final IDataType[] _includes;

    public AbstractDataType(final String name, final IDataType... includes) {
        _name = name;
        _includes = includes;
    }

    public AbstractDataType(final String name, final boolean isHidden, final IDataType... includes) {
        _name = name;
        _hidden = isHidden;
        _includes = includes;
    }

    public String getName() {
        return _name;
    }

    public boolean isHidden() {
        return _hidden;
    }

    public IDataType[] getIncludedDataTypes() {
        return _includes;
    }

    
    public void setForceActive(boolean force) {
        this._force = force;
    }

    public boolean getIsForced() {
        return _force;
    }
}
