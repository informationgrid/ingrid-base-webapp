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
        int count = 0;
        if (_indexRunnable != null) {
            count = _indexRunnable.getDocumentCount();
        }
        return count;
    }

    @RequestMapping(value = IUris.INDEXING, method = RequestMethod.GET)
    public String getIndexing(ModelMap model) {
    	model.addAttribute("count", injectDocumentCount());
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
