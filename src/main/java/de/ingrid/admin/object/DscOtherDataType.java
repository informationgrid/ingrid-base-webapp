package de.ingrid.admin.object;

import org.springframework.stereotype.Service;

@Service
public class DscOtherDataType extends AbstractDataType {

    public DscOtherDataType() {
        super("dsc_other", true);
    }
}