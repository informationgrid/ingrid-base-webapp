package de.ingrid.admin.search;

import junit.framework.TestCase;

public class StemmerTest extends TestCase {

    public void testGermanStemming() throws Exception {
        Stemmer stemmer = new GermanStemmer();
        String stem = stemmer.stem("autos");
        assertEquals("auto", stem);
    }
}
