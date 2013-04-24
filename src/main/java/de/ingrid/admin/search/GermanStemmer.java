package de.ingrid.admin.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class GermanStemmer extends Stemmer {

    private static final Analyzer ANALYZER = new GermanAnalyzer();

    @Override
    public Analyzer getAnalyzer() {
        return ANALYZER;
    }

}
