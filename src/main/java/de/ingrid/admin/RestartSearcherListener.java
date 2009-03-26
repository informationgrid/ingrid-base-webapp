package de.ingrid.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("restartSearcher")
public class RestartSearcherListener implements INewIndexListener {

    @Autowired
    public RestartSearcherListener(LuceneSearcher luceneSearcher) {
        // TODO Auto-generated constructor stub
    }
    public void indexIsCreated() {
        // TODO Auto-generated method stub

    }

}
