package de.ingrid.admin;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.PlugDescription;

@Service
@Qualifier("restartSearcher")
public class RestartSearcherListener implements INewIndexListener {

    private final LuceneSearcher _luceneSearcher;
    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public RestartSearcherListener(LuceneSearcher luceneSearcher, PlugDescriptionService plugDescriptionService) {
        _luceneSearcher = luceneSearcher;
        _plugDescriptionService = plugDescriptionService;
    }

    public void indexIsCreated() {
        try {
            _luceneSearcher.close();
            PlugDescription plugDescription = _plugDescriptionService.readPlugDescription();
            _luceneSearcher.configure(plugDescription);
            // TODO throw exception?
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
