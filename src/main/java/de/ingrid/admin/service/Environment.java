package de.ingrid.admin.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

@Service
public class Environment {

    private final IConfigurable[] _configurables;
    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public Environment(final PlugDescriptionService plugDescriptionService, final IConfigurable... configurables)
            throws IOException {
        _plugDescriptionService = plugDescriptionService;
        _configurables = configurables;
        reconfigure(_configurables);
    }

    private void reconfigure(final IConfigurable[] configurables) throws IOException {
        if (_plugDescriptionService.existsPlugdescription()) {
            final PlugDescription plugDescription = _plugDescriptionService.readPlugDescription();
            for (final IConfigurable configurable : configurables) {
                configurable.configure(plugDescription);
            }
        }
    }
}
