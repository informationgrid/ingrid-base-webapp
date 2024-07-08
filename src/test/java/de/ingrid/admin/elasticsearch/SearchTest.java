/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class SearchTest extends ElasticTests {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger( SearchTest.class );

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        setup( "test_1", "data/webUrls2.json" );
        IndexManager indexManager = new IndexManager( elastic, new ElasticConfig() );
        indexManager.init();
        indexManager.removeAlias("test");
        indexManager.switchAlias( "test", null, "test_1" );
    }

    @AfterAll
    public static void afterClass() {
        elastic.getClient().shutdown();
    }

    @Test
    void search() throws Exception {

        IndexImpl index2 = getIndexer();

        IngridQuery q = QueryStringParser.parse("");
        IngridHits search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(4l));
    }

    @Test
    void searchComplex() throws Exception {

        IndexImpl index2 = getIndexer();
        IngridQuery q = QueryStringParser.parse("(title:Mathematics OR title:Prime) AND (title:Square OR title:Prime)");
        IngridHits search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(1l));

        q = QueryStringParser.parse("(title:Mathematics OR xxx:zzz) partner:bund");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(1l));

        q = QueryStringParser.parse("(title:Mathematics) OR ((xxx:yyy) partner:bund)");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(1l));

        // this should have the same result as the previous one, but since partner, provider and dataype
        // are separated from the other fields inside the InGridQuery, we cannot know the original
        // combination with the other fields. So it could be that partner is connected to title or xxx
        // or even both! See also REDMINE-251
        /*
        q = QueryStringParser.parse( "title:Mathematics OR xxx:www partner:bund" );
        search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 1l ) );
        */

        q = QueryStringParser.parse("(title:Mathematics OR title:Prime) AND (partner:bw OR partner:bund)");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(2l));

        q = QueryStringParser.parse("(title:Mathematics OR title:Prime) OR (title:Square OR title:Prime)");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(3l));

        q = QueryStringParser.parse("(title:Mathematics OR title:Prime) OR (title:Square)");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(3l));
    }

    @Test
    void searchFieldWithWildcards() throws Exception {
        IndexImpl index2 = getIndexer();
        IngridQuery q = QueryStringParser.parse("title:math*");
        IngridHits search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(1l));

        q = QueryStringParser.parse("url:http*");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(4l));

        q = QueryStringParser.parse("url:\"http://www.h*\"");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(1l));
    }

    @Test
    void searchTwoFields() throws Exception {
        IndexImpl index2 = getIndexer();
        IngridQuery q = QueryStringParser.parse("title:Mathematics partner:bund");
        IngridHits search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(1l));

        q = QueryStringParser.parse("title:Mathematics partner:bw");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(0l));

        q = QueryStringParser.parse("title:Mathematics -partner:bw");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(1l));

        q = QueryStringParser.parse("title:Mathematics -partner:bund");
        search2 = index2.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        assertThat(search2.length(), is(0l));
    }

    @Test
    void getDoc() throws Exception {
        ElasticDocument response = indexManager.getDocById("4");
        assertThat(response, not(is(nullValue())));
        assertThat((String) response.get("url"), is("http://www.golemXXX.de"));
    }

    @Test
    void searchFacets() throws Exception {

        List<IngridDocument> facetQueries = new ArrayList<IngridDocument>();
        IngridDocument faceteEntry = new IngridDocument();
        faceteEntry.put("id", "datatype");
        faceteEntry.put("field", "datatype");
        facetQueries.add(faceteEntry);

        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse("");
        q.put("FACETS", facetQueries);

        IngridHits search2 = index.search(q, 0, 10);
        assertThat(search2, not(is(nullValue())));
        IngridDocument facets = (IngridDocument) search2.get("FACETS");
        assertThat(facets.keySet().size(), is(2));
        assertThat(facets.keySet().contains("datatype:www"), is(true));
        assertThat(facets.get("datatype:www").toString(), is("3"));
        assertThat(facets.keySet().contains("datatype:pdf"), is(true));
        assertThat(facets.get("datatype:pdf").toString(), is("1"));
    }

    /**
     * When searching for more than one term, then the terms should be in title and summary
     * but not necessarily all in the same field. It's just important that the document contains
     * those terms.
     * @throws Exception
     */
    @Test
    void searchTitleAndContent() throws Exception {

        IndexImpl index = getIndexer();
        // both terms are found in the content field => one match
        IngridQuery q = QueryStringParser.parse("biggest number");
        IngridHits search = index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.length(), is(1l));

        // one term is found in title, the other in content => one match
        q = QueryStringParser.parse("four rectangle");
        search = index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.length(), is(1l));

        // one term is found in title, the other nowhere => no match
        q = QueryStringParser.parse("impossible rectangle");
        search = index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.length(), is(0l));
    }

}
