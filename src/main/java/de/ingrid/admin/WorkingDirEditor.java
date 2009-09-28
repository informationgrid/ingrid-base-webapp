package de.ingrid.admin;

import org.springframework.beans.propertyeditors.FileEditor;

public class WorkingDirEditor extends FileEditor {

    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        if (null != text && !"".equals(text)) {
            System.out.println("set: " + text);
            super.setAsText(text);
        }
    }

    @Override
    public String getAsText() {
        // TODO Auto-generated method stub
        final String text = super.getAsText();
        System.out.println("get: " + text);
        return text == null ? "./" : text;
    }
}
