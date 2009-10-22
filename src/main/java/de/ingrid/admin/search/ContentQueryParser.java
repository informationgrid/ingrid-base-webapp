package de.ingrid.admin.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("contentParser")
public class ContentQueryParser extends TermQueryParser {

    @Autowired
    public ContentQueryParser(Stemmer stemmer) {
        super("content", null, stemmer);
    }
}
