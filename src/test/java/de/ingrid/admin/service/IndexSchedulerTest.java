/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package de.ingrid.admin.service;

import java.io.File;
import java.util.Optional;

import de.ingrid.admin.Config;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ingrid.admin.TestUtils;
import de.ingrid.admin.elasticsearch.IndexRunnable;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.utils.PlugDescription;

import static org.junit.jupiter.api.Assertions.*;

public class IndexSchedulerTest {

    private DummyRunnable _runnable;

    private IndexScheduler _scheduler;

    @Mock
    static ElasticsearchNodeFactoryBean elastic;

    private static class DummyRunnable extends IndexRunnable {
        private long _time;

        private int _counter = 0;

        public DummyRunnable(final long time, PlugDescriptionService pdService, Config config) {
            super(pdService, new IndexManager( elastic, new ElasticConfig() ), null, config, new ElasticConfig(), Optional.empty(), new StatusProviderService());
            _time = time;
        }

        @Override
        public void run() {
            _counter++;
            try {
                System.out.println("DummyRunnable: Sleep for " + (_time/1000) + " sec.");
                Thread.sleep(_time);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void setTime(final long time) {
            _time = time;
        }

        public int getCount() {
            return _counter;
        }
    }



    @BeforeEach
    public void setUp() throws Exception {
//        new JettyStarter( );
        //setup( "test2", "data/webUrls2.json" );
        MockitoAnnotations.initMocks(this);

        Client client = Mockito.mock( Client.class );
        Mockito.when( elastic.getClient() ).thenReturn( client );
        Mockito.when( client.settings() ).thenReturn( Settings.builder().build() );

        final PlugDescription pd = new PlugDescription();
        final File file = new File(System.getProperty("java.io.tmpdir"), this.getClass().getName());
        System.out.println(file.exists());
        if (file.exists()) {
            TestUtils.delete(file);
        }
        System.out.println(file.exists());

        assertTrue(file.mkdirs());
        pd.setWorkinDirectory(file);

        // store our location of pd as system property to be fetched by pdService
        Config config = new Config();
        config.plugdescriptionLocation = new File(file.getAbsolutePath(), "plugdescription.xml").getAbsolutePath();


        //IngridIndexSearcher searcher = new IngridIndexSearcher(new QueryParsers(), new LuceneIndexReaderWrapper(null));
        PlugDescriptionService pdService = new PlugDescriptionService(config);
        _runnable = new DummyRunnable(1000L, pdService, config);
        _runnable.configure(pd);

        _scheduler = new IndexScheduler(_runnable, config);
    }

    @AfterEach
    public void tearDown() {
        if (_scheduler != null) {
            _scheduler.deletePattern();
        }
    }

    @Test
    void testIsStarted() {
        assertFalse(_scheduler.isStarted());

        _scheduler.setPattern("* * * * *");
        assertTrue(_scheduler.isStarted());

        _scheduler.deletePattern();
        assertFalse(_scheduler.isStarted());
    }

    @Test
    @Disabled
    void testScheduling10() throws Exception {
        System.out.println("Sleep for 10 sec.");
        _runnable.setTime(1000L * 10L);
        _scheduler.setPattern("* * * * *");

        assertEquals(0, _runnable.getCount());
        System.out.println("Sleep for 61 sec.");
        Thread.sleep(1000L * 61L);
        assertEquals(1, _runnable.getCount());
    }

    @Test
    @Disabled
    void testScheduling70() throws Exception {
        System.out.println("Sleep for 70 sec.");
        _runnable.setTime(1000L * 70L);
        _scheduler.setPattern("* * * * *");

        assertEquals(0, _runnable.getCount());
        System.out.println("Sleep for 121 sec.");
        Thread.sleep(1000L * 121L);
        assertEquals(1, _runnable.getCount());
    }

    @Test
    void testPatternFile() {
        final String pattern = "0 12 * * *";

        assertNull(_scheduler.getPattern());

        _scheduler.setPattern(pattern);
        assertEquals(pattern, _scheduler.getPattern());

        // lets simulate a new start
        final IndexScheduler scheduler2 = new IndexScheduler(_runnable, new Config());
        assertEquals(pattern, scheduler2.getPattern());
        scheduler2.deletePattern();
    }
}
