/*-
 * **************************************************-
 * InGrid Base-Webapp
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.admin.elasticsearch;

import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.elasticsearch.IndexManager;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.List;
import java.util.Properties;

import static de.ingrid.admin.elasticsearch.ElasticTests.getConfigProperties;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IndexManagerTest {

    public static ElasticsearchNodeFactoryBean elastic;
    private static Client client;
    private IndexManager indexManager;
    private StatusProvider statusProvider;

    @BeforeClass
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
        elasticConfig.indexSearchDefaultFields = new String[] { "title", "content" };
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

    @Before
    public void prepare() {
        indexManager = new IndexManager( elastic, new ElasticConfig() );
        statusProvider = new StatusProvider();
        // TODO: indexManager.setStatusProvider( statusProvider );
    }
    
    @AfterClass
    public static void afterClass() {
        elastic.getClient().close();
    }

    @Test
    public void testSwitchAlias() {
        String aliasName = "my-alias";
        String indexName = "switch_alias_test0";
        indexManager.createIndex( indexName );

        ImmutableOpenMap<String, List<AliasMetaData>> indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 0 ) );

        indexManager.switchAlias( aliasName, null, indexName );

        indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 1 ) );
    }

    @Test
    public void testSwitchAliasMultiple() {
        String aliasName = "my-alias1";
        String indexName = "switch_alias_test1_1";
        String indexName2 = "switch_alias_test1_2";
        indexManager.createIndex( indexName );
        indexManager.createIndex( indexName2 );

        ImmutableOpenMap<String, List<AliasMetaData>> indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 0 ) );

        indexManager.switchAlias( aliasName, null, indexName );
        indexManager.switchAlias( aliasName, null, indexName2 );

        indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 2 ) );
    }

    @Test
    public void testSwitchAliasMultipleReplace() {
        String aliasName = "my-alias2";
        String indexName = "switch_alias_test2_1";
        String indexName2 = "switch_alias_test2_2";
        indexManager.createIndex( indexName );
        indexManager.createIndex( indexName2 );

        ImmutableOpenMap<String, List<AliasMetaData>> indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 0 ) );

        indexManager.switchAlias( aliasName, null, indexName );
        indexManager.switchAlias( aliasName, indexName, indexName2 );

        indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 1 ) );
    }

    @Test
    public void testIndexFromAlias() {
        String aliasName = "my-alias3";
        String indexName = "switch_alias_test3_1";
        indexManager.createIndex( indexName );

        indexManager.addToAlias( aliasName, indexName );

        String indexFromAlias = indexManager.getIndexNameFromAliasName( aliasName, "switch" );
        assertThat( indexFromAlias, is( indexName ) );
    }

    @Test
    public void testIndexFromAliasMultiple() {
        String aliasName = "my-alias4";
        String indexName = "switch_alias_test4_1";
        String indexName2 = "switch_alias_test4_2";
        indexManager.createIndex( indexName );
        indexManager.createIndex( indexName2 );

        indexManager.addToAlias( aliasName, indexName );
        indexManager.addToAlias( aliasName, indexName2 );
        
        ImmutableOpenMap<String, List<AliasMetaData>> indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 2 ) );

        String indexFromAlias = indexManager.getIndexNameFromAliasName( aliasName, "switch" );
        assertThat( indexFromAlias, anyOf( is( indexName ), is( indexName2 ) ) );
        assertThat( statusProvider.toString(), containsString( "The index name could not be determined correctly" ) );
    }
    
    @Test
    public void testIndexFromAliasMultipleReplace() {
        String aliasName = "my-alias5";
        String indexName = "switch_alias_test5_1";
        String indexName2 = "switch_alias_test5_2";
        indexManager.createIndex( indexName );
        indexManager.createIndex( indexName2 );
        
        indexManager.addToAlias( aliasName, indexName );
        indexManager.switchAlias( aliasName, indexName, indexName2 );
        
        ImmutableOpenMap<String, List<AliasMetaData>> indexToAliasesMap = client.admin().indices().getAliases( new GetAliasesRequest( aliasName ) ).actionGet().getAliases();
        assertThat( indexToAliasesMap.keys().size(), is( 1 ) );
        
        String indexFromAlias = indexManager.getIndexNameFromAliasName( aliasName, "switch" );
        assertThat( indexFromAlias, is( indexName2 ) );
        assertThat( statusProvider.toString(), not( containsString( "The index name could not be determined correctly" ) ) );
    }

}
