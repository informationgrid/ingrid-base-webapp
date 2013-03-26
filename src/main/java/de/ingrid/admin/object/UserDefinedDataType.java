package de.ingrid.admin.object;


/**
 * This datatype can be configured and injected via spring !
 * E.g. add arbitrary datatypes for "tagging" of iPlug to be used for facetting !
 */
public class UserDefinedDataType extends AbstractDataType {

    public UserDefinedDataType(String dataTypeName) {
        super(dataTypeName);
    }

    public UserDefinedDataType(String dataTypeName, Boolean isHidden) {
        super(dataTypeName, isHidden);
    }

    /* (non-Javadoc)
     * @see de.ingrid.admin.object.AbstractDataType#setForceActive(boolean)
     */
    public void setForceActive(boolean force) {
        super.setForceActive(force);
    }

    /**
     * @return datatype always set (true) or has to be clicked by user in admin gui (false)
     */
    public boolean getForceActive() {
        return super.getIsForced();
    }
}