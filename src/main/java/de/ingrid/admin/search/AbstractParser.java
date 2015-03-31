/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.search;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.beans.factory.annotation.Autowired;

import de.ingrid.search.utils.IQueryParser;

public abstract class AbstractParser implements IQueryParser {

    private static Logger LOG = Logger.getLogger(AbstractParser.class);

    /** The default stemmer used in {@link #filterTerm(String)}.
     * Is AUTOWIRED in spring environment via {@link #setDefaultStemmer(Stemmer)}
     */
    private static Stemmer _defaultStemmer;

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

    /** Filter term with the default stemmer (which is autowired !).
     * @param term
     * @return
     */
    protected static String filterTerm(String term) {
        String result = "";
        
        TokenStream stream = null;
        try {
            stream = getDefaultStemmer().getAnalyzer().tokenStream(null, new StringReader(term));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // get the TermAttribute from the TokenStream
        CharTermAttribute termAtt = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);

        try {
            stream.reset();
            // add all tokens until stream is exhausted
            while (stream.incrementToken()) {
            	result = result + " " + termAtt.toString();//term();
            }
            stream.end();
            stream.close();
        } catch (IOException ex) {
        	LOG.error("Problems tokenizing term " + term + ", we return full term.", ex);
        	result = term;
        }

        return result.trim();
    }

    /** We supply a static getDefaultStemmer method to initialize default stemmer if
     * not set via autowiring (e.g. in unit testcases no autowiring) !
     * @return the stemmer set as default stemmer in AbstractParser used in method
     * {@link #filterTerm(String) AbstractParser.filterTerm(String)}
     */
    public static Stemmer getDefaultStemmer() {
        if (_defaultStemmer == null) {
        	// default stemmer is GERMAN, see INGRID-2246
        	_defaultStemmer = new GermanStemmer();
        	LOG.warn("Default stemmer not set via autowiring ! We set default stemmer " + _defaultStemmer);
        }
        
        return _defaultStemmer;
	}

    /** Injects default stemmer via autowiring !
     * @param defaultStemmer
     */
    @Autowired
    public void setDefaultStemmer(Stemmer defaultStemmer) {
    	_defaultStemmer = defaultStemmer;
	}
}
