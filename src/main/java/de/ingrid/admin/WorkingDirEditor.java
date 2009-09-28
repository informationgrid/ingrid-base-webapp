package de.ingrid.admin;

import java.beans.PropertyEditorSupport;
import java.io.File;

public class WorkingDirEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (null != text && !"".equals(text)) {
            setValue(new File(text));
        }
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return value != null ? value.toString() : "";
    }

}
