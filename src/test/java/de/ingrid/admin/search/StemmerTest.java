package de.ingrid.admin.search;

import junit.framework.TestCase;
import de.ingrid.utils.queryparser.QueryStringParser;

public class StemmerTest extends TestCase {

    public void testGermanStemming() throws Exception {
        Stemmer stemmer = new GermanStemmer();
        String stem = stemmer.stem("autos");
        assertEquals("auto", stem);
        stem = stemmer.stem("139.0");
        assertEquals("139.0", stem);

        System.out.println(QueryStringParser.parse("189.0"));
    }
}
