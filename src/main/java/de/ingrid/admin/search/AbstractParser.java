package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanClause.Occur;

public abstract class AbstractParser implements IQueryParser {

    protected Occur transform(boolean required, boolean prohibited) {
        Occur occur = null;
        if (required) {
            if (prohibited) {
                occur = Occur.MUST_NOT;
            } else {
                occur = Occur.MUST;
            }
        } else {
            if (prohibited) {
                occur = Occur.MUST_NOT;
            } else {
                occur = Occur.SHOULD;
            }
        }
        return occur;
    }

}
