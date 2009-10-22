package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("titleParser")
public class TitleQueryParser extends TermQueryParser {

    public TitleQueryParser() {
        super("title", Occur.SHOULD, null);
    }

}
