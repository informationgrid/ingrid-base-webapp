/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package de.ingrid.admin.controller;

import java.lang.Thread.State;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.search.IndexRunnable;

@Controller
public class IndexController extends AbstractController {

    private Thread _thread = null;
    private final IndexRunnable _indexRunnable;
    private static final Log LOG = LogFactory.getLog(IndexController.class);

    @Autowired
    public IndexController(final IndexRunnable indexRunnable) {
        _indexRunnable = indexRunnable;
        _thread = new Thread(indexRunnable);
    }

    @ModelAttribute("state")
    public State injectState() {
        return !_indexRunnable.isProduceable() ? State.BLOCKED : _thread.getState();
    }

    @ModelAttribute("documentCount")
    public int injectDocumentCount() {
        int documentCount = 0;
        if (_indexRunnable != null) {
        	documentCount = _indexRunnable.getDocumentCount();
        }
        return documentCount;
    }

    @RequestMapping(value = IUris.INDEXING, method = RequestMethod.GET)
    public String getIndexing(ModelMap model) {
        return IViews.INDEXING;
    }

    @RequestMapping(value = IUris.INDEXING, method = RequestMethod.POST)
    public String postIndexing(ModelMap model) throws Exception {
        if (_indexRunnable.isProduceable()) {
            if (_thread.getState() == State.NEW) {
                LOG.info("start indexer");
                _thread.start();
            } else if (_thread.getState() == State.TERMINATED) {
                LOG.info("start indexer");
                _thread = new Thread(_indexRunnable);
                _thread.start();
            } else {
                LOG.info("indexer was not started");
            }
        } else {
            LOG.warn("can not start indexer, because it is not produceable");
        }

        model.addAttribute("started", true);
        return IViews.INDEXING;

    }
    
    @RequestMapping(value = IUris.INDEX_STATE, method = RequestMethod.GET)
    public String getIndexState(ModelMap model){
    	model.addAttribute("state", _thread.getState());
    	return "/base/indexState";
    }
}
