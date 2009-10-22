package de.ingrid.admin.search;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public abstract class Stemmer {

    public String stem(String text) throws IOException {
        Analyzer analyzer = getAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(text));
        StringBuilder builder = new StringBuilder();
        Token token = tokenStream.next();
        while (null != token) {
            builder.append(" " + token.termText());
            token = tokenStream.next();
        }
        return builder.toString().trim();
    }

    abstract Analyzer getAnalyzer();
}
