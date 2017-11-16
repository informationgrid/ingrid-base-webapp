package de.ingrid.admin.elasticsearch;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import de.ingrid.admin.JettyStarter;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.elasticsearch.IndexManager;

public class IndexManagerTest {

    public static ElasticsearchNodeFactoryBean elastic;
    private static Client client;
    private IndexManager indexManager;
    private StatusProvider statusProvider;

    @BeforeClass
    public static void setup() throws Exception {
        new JettyStarter( false );

        File dir = new File( "./target/test-data" );
        if (dir.exists())
            FileSystemUtils.deleteRecursively( dir );

        elastic = new ElasticsearchNodeFactoryBean();
        elastic.afterPropertiesSet();
        client = elastic.getObject().client();
    }

    @Before
    public void prepare() throws Exception {
        indexManager = new IndexManager( elastic, new ElasticConfig() );
        statusProvider = new StatusProvider();
        // TODO: indexManager.setStatusProvider( statusProvider );
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        elastic.getObject().close();
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
