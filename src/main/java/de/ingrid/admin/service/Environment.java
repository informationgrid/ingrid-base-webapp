package de.ingrid.admin.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

@Service
public class Environment {

    @Autowired
    public Environment(final PlugDescriptionService plugDescriptionService, final IConfigurable... configurables)
            throws IOException {
        final PlugDescription plugDescription = plugDescriptionService.getPlugDescription();
        if (plugDescription != null) {
            for (final IConfigurable configurable : configurables) {
                configurable.configure(plugDescription);
            }
        }
    }
}
