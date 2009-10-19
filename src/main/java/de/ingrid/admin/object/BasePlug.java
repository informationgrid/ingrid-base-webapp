package de.ingrid.admin.object;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@Service
public class BasePlug extends HeartBeatPlug {


    public BasePlug() {
		super(60000, new PlugDescriptionFieldFilters());
    }

    @Override
    public IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {
        final int count = Math.min(23, length);
        final IngridHit[] hits = new IngridHit[count];
        for (int i = 0; i < count; i++) {
            hits[i] = new IngridHit("abc", i, 0, count - i);
        }
        return new IngridHits(23, hits);
    }

    @Override
    public IngridHitDetail getDetail(final IngridHit hit, final IngridQuery query, final String[] requestedFields)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IngridHitDetail[] getDetails(final IngridHit[] hits, final IngridQuery query, final String[] requestedFields)
            throws Exception {
        final IngridHitDetail[] details = new IngridHitDetail[hits.length];
        for (int i = 0; i < hits.length; i++) {
            details[i] = new IngridHitDetail(hits[i], "Test Titel #" + (i + 1), "Test Summary #" + (i + 1));
        }
        return details;
    }
}
