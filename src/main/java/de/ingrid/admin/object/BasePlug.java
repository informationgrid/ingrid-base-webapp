/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.object;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.IngridCall;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;

@Service
public class BasePlug extends HeartBeatPlug {

    @Autowired
    public BasePlug(IMetadataInjector[] metadataInjectors, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors) {
        super(60000, new PlugDescriptionFieldFilters(), metadataInjectors, preProcessors, postProcessors);
    }

    @Override
    public IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {
        final int count = Math.min(23, length);
        final IngridHit[] hits = new IngridHit[count];
        for (int i = 0; i < count; i++) {
            hits[i] = new IngridHit("abc", String.valueOf( i ), 0, count - i);
        }
        return new IngridHits(23, hits);
    }

    @Override
    public IngridHitDetail getDetail(final IngridHit hit, final IngridQuery query, final String[] requestedFields) throws Exception {
        return null;
    }

    @Override
    public IngridHitDetail[] getDetails(final IngridHit[] hits, final IngridQuery query, final String[] requestedFields) throws Exception {
        final IngridHitDetail[] details = new IngridHitDetail[hits.length];
        for (int i = 0; i < hits.length; i++) {
            details[i] = new IngridHitDetail(hits[i], "Test Titel #" + (i + 1), "Test Summary #" + (i + 1));
        }
        return details;
    }

    @Override
    public IngridDocument call(IngridCall targetInfo) throws Exception {
        throw new RuntimeException( "call-function not implemented in Base-iPlug" );
    }
}
