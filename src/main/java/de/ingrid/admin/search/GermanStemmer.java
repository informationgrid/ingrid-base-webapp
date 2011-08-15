package de.ingrid.admin.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;

public class GermanStemmer extends Stemmer {

    private static final Analyzer ANALYZER = new GermanAnalyzer();

    @Override
    Analyzer getAnalyzer() {
        return ANALYZER;
    }

}
