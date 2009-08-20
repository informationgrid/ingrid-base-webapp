package de.ingrid.admin;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

@Service
@Qualifier("restartSearcher")
public class RestartSearcherListener implements INewIndexListener, IConfigurable {

    private final LuceneSearcher _luceneSearcher;
    private PlugDescription _plugDescription;
    private static final Log LOG = LogFactory.getLog(RestartSearcherListener.class);

    @Autowired
    public RestartSearcherListener(LuceneSearcher luceneSearcher) {
        _luceneSearcher = luceneSearcher;
    }

    public void indexIsCreated() {
        try {
            _luceneSearcher.close();
            _luceneSearcher.configure(_plugDescription);
            // TODO throw exception?
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void configure(PlugDescription plugDescription) {
        LOG.debug("reconfigure...");
        _plugDescription = plugDescription;
    }

}
