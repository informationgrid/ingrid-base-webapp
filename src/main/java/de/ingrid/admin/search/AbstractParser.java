package de.ingrid.admin.search;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;

public abstract class AbstractParser implements IQueryParser {

    private static Logger LOG = Logger.getLogger(AbstractParser.class);

    private static StandardAnalyzer fAnalyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);

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

    protected static String filterTerm(String term) {
        String result = "";

        TokenStream stream = fAnalyzer.tokenStream(null, new StringReader(term));
        // get the TermAttribute from the TokenStream
        TermAttribute termAtt = (TermAttribute) stream.addAttribute(TermAttribute.class);

        try {
            stream.reset();
            // add all tokens until stream is exhausted
            while (stream.incrementToken()) {
            	result = result + " " + termAtt.term();
            }
            stream.end();
            stream.close();
        } catch (IOException ex) {
        	LOG.error("Problems tokenizing term " + term + ", we return full term.", ex);
        	result = term;
        }

        return result.trim();
    }
}
