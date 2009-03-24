package de.ingrid.admin;

import java.lang.Thread.State;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/base/indexing.html")
public class IndexController {

    private Thread _thread = null;
    private final IndexRunnable _indexRunnable;

    @Autowired
    public IndexController(IndexRunnable indexRunnable) {
        _indexRunnable = indexRunnable;
        _thread = new Thread(indexRunnable);
    }

    @ModelAttribute("state")
    public State injectState() {
        return !_indexRunnable.isConfigured() ? State.BLOCKED : _thread.getState();
    }

    @ModelAttribute("documentCount")
    public int injectDocumentCount() {
        int count = 0;
        if (_indexRunnable != null) {
            count = _indexRunnable.getDocumentCount();
        }
        return count;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getIndexing() {
        return "/base/indexing";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postIndexing() throws Exception {
        String ret = "redirect:/base/indexing.html";
        if (_indexRunnable.isConfigured()) {
            if (_thread.getState() == State.NEW) {
                _thread.start();
            } else if (_thread.getState() == State.TERMINATED) {
                _thread = new Thread(_indexRunnable);
                _thread.start();
            } else {
                // TODO reject error
                ret = "/base/indexing";
            }
        } else {
            // TODO reject error
            ret = "/base/indexing";
        }
        return ret;

    }
}
