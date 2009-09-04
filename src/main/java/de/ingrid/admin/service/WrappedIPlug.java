package de.ingrid.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IPlug;

@Service
public class WrappedIPlug {

    private IPlug _plug;

    @Autowired(required = false)
    public void setPlug(IPlug plug) {
        _plug = plug;
    }

    public IPlug getPlug() {
        return _plug;
    }

}
