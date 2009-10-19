package de.ingrid.admin.search;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("contentParser")
public class ContentQueryParser extends TermQueryParser {

    public ContentQueryParser() {
        super("content");
    }
}