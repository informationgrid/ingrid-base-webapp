package de.ingrid.admin.processors;

import org.springframework.stereotype.Service;

import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.query.IngridQuery;

@Service
public class QueryPostProcessorAdapter implements IPostProcessor {

    @Override
    public void process(IngridQuery ingridQueries, IngridDocument[] documents) throws Exception {
        // nothing todo because currently we have no implemenation

    }

}
