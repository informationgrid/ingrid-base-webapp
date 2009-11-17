package de.ingrid.admin.object;

import org.springframework.stereotype.Service;

@Service
public class DefaultDataType extends AbstractDataType {

    public DefaultDataType() {
        super(DEFAULT);
    }

}
