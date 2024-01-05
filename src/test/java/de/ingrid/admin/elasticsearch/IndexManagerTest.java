/*-
 * **************************************************-
 * InGrid Base-Webapp
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

import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.utils.statusprovider.StatusProvider;

import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.junit.jupiter.api.*;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.List;
import java.util.Properties;

import static de.ingrid.admin.elasticsearch.ElasticTests.getConfigProperties;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IndexManagerTest {

    public static ElasticsearchNodeFactoryBean elastic;
    private static Client client;
    private IndexManager indexManager;
    private StatusProvider statusProvider;

    @BeforeAll
    public static void setup() throws Exception {
        File dir = new File( "./target/test-data" );
        if (dir.exists())
            FileSystemUtils.deleteRecursively( dir );

        elastic = new ElasticsearchNodeFactoryBean();

        Properties elasticProperties = getConfigProperties();
        ElasticConfig elasticConfig = new ElasticConfig();
        elasticConfig.isEnabled = true;
        IndexInfo[] activeIndices = new IndexInfo[1];
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setToIndex("test_1");
        indexInfo.setToType("base");
        activeIndices[0] = indexInfo;
        elasticConfig.activeIndices = activeIndices;
        elasticConfig.indexFieldSummary = "content";
        elasticConfig.additionalSearchDetailFields = new String[0];
        elasticConfig.indexSearchDefaultFields = new String[] { "title^10.0","title.edge_ngram^4.0","title.ngram^2.0","summary","summary.edge_ngram^0.4","summary.ngram^0.2","content^0.2","content.ngram^0.1" };
        elasticConfig.remoteHosts = ((String)elasticProperties.get("elastic.remoteHosts")).split(",");
        elastic.init(elasticConfig);
        elastic.afterPropertiesSet();
        client = elastic.getClient();

        IndexManager indexManager = new IndexManager( elastic, elasticConfig );
        String[] indicesToDelete = new String[] {"test_1", "switch_alias_test0", "switch_alias_test1_1", "switch_alias_test1_2", "switch_alias_test2_1", "switch_alias_test2_2", "switch_alias_test3_1", "switch_alias_test4_1", "switch_alias_test4_2", "switch_alias_test5_1", "switch_alias_test5_2"};

        for (String index : indicesToDelete) {
            try {
                indexManager.deleteIndex( index );
            } catch (Exception ignored) {}
        }

    }

    @BeforeEach
    public void prepare() {
        indexManager = new IndexManager( elastic, new ElasticConfig() );
        statusProvider = new StatusProvider();
        // TODO: indexManager.setStatusProvider( statusProvider );
    }

    @AfterAll
    public static void afterClass() {
        elastic.getClient().close();
    }

    @Test
    void testSwitchAlias() {
        String aliasName = "my-alias";
        String indexName = "switch_alias_test0";
        indexManager.createIndex(indexName);

        ImmutableOpenMap<String, List<AliasMetadata>> indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(0));

        indexManager.switchAlias(aliasName, null, indexName);

        indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(1));
    }

    @Test
    void testSwitchAliasMultiple() {
        String aliasName = "my-alias1";
        String indexName = "switch_alias_test1_1";
        String indexName2 = "switch_alias_test1_2";
        indexManager.createIndex(indexName);
        indexManager.createIndex(indexName2);

        ImmutableOpenMap<String, List<AliasMetadata>> indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(0));

        indexManager.switchAlias(aliasName, null, indexName);
        indexManager.switchAlias(aliasName, null, indexName2);

        indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(2));
    }

    @Test
    void testSwitchAliasMultipleReplace() {
        String aliasName = "my-alias2";
        String indexName = "switch_alias_test2_1";
        String indexName2 = "switch_alias_test2_2";
        indexManager.createIndex(indexName);
        indexManager.createIndex(indexName2);

        ImmutableOpenMap<String, List<AliasMetadata>> indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(0));

        indexManager.switchAlias(aliasName, null, indexName);
        indexManager.switchAlias(aliasName, indexName, indexName2);

        indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(1));
    }

    @Test
    void testIndexFromAlias() {
        String aliasName = "my-alias3";
        String indexName = "switch_alias_test3_1";
        indexManager.createIndex(indexName);

        indexManager.addToAlias(aliasName, indexName);

        String indexFromAlias = indexManager.getIndexNameFromAliasName(aliasName, "switch");
        assertThat(indexFromAlias, is(indexName));
    }

    // How to handle multiple indices with same prefix under an alias? Do we actually need this function anymore
    @Test
    @Disabled
    void testIndexFromAliasMultiple() {
        String aliasName = "my-alias4";
        String indexName = "switch_alias_test4_1";
        String indexName2 = "switch_alias_test4_2";
        indexManager.createIndex(indexName);
        indexManager.createIndex(indexName2);

        indexManager.addToAlias(aliasName, indexName);
        indexManager.addToAlias(aliasName, indexName2);

        ImmutableOpenMap<String, List<AliasMetadata>> indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(2));

        String indexFromAlias = indexManager.getIndexNameFromAliasName(aliasName, "switch");
        assertThat(indexFromAlias, anyOf(is(indexName), is(indexName2)));
        assertThat(statusProvider.toString(), containsString("The index name could not be determined correctly"));
    }

    @Test
    void testIndexFromAliasMultipleReplace() {
        String aliasName = "my-alias5";
        String indexName = "switch_alias_test5_1";
        String indexName2 = "switch_alias_test5_2";
        indexManager.createIndex(indexName);
        indexManager.createIndex(indexName2);

        indexManager.addToAlias(aliasName, indexName);
        indexManager.switchAlias(aliasName, indexName, indexName2);

        ImmutableOpenMap<String, List<AliasMetadata>> indexToAliasesMap = client.admin().indices().getAliases(new GetAliasesRequest( aliasName )).actionGet().getAliases();
        assertThat(indexToAliasesMap.keys().size(), is(1));

        String indexFromAlias = indexManager.getIndexNameFromAliasName(aliasName, "switch");
        assertThat(indexFromAlias, is(indexName2));
        assertThat(statusProvider.toString(), not(containsString("The index name could not be determined correctly")));
    }

}
