package de.ingrid.admin.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;


public class StandardStemmer extends Stemmer {

    private static final Analyzer ANALYZER = new StandardAnalyzer(Version.LUCENE_CURRENT);

    @Override
    public Analyzer getAnalyzer() {
        return ANALYZER;
    }

}
