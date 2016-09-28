/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.TestUtils;
import de.ingrid.admin.elasticsearch.IndexManager;
import de.ingrid.admin.elasticsearch.IndexRunnable;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.utils.PlugDescription;

public class IndexSchedulerTest {

    private DummyRunnable _runnable;

    private IndexScheduler _scheduler;
    
    @Mock
    static ElasticsearchNodeFactoryBean elastic;
    
    private static class DummyRunnable extends IndexRunnable {
        private long _time;

        private int _counter = 0;

        public DummyRunnable(final long time, PlugDescriptionService pdService) throws Exception {
            super(pdService, new IndexManager( elastic ));
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

    
    
    @Before
    public void setUp() throws Exception {
        new JettyStarter( false );
        //setup( "test2", "data/webUrls2.json" );
        MockitoAnnotations.initMocks(this);
        
        Client client = Mockito.mock( Client.class );
        Node node = Mockito.mock( Node.class );
        Mockito.when( elastic.getObject() ).thenReturn( node );
        Mockito.when( node.client() ).thenReturn( client );
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
        System.setProperty(IKeys.PLUG_DESCRIPTION, new File(file.getAbsolutePath(), "plugdescription.xml").getAbsolutePath());

        //IngridIndexSearcher searcher = new IngridIndexSearcher(new QueryParsers(), new LuceneIndexReaderWrapper(null));
        PlugDescriptionService pdService = new PlugDescriptionService();
        _runnable = new DummyRunnable(1000L, pdService);
        _runnable.configure(pd);

        _scheduler = new IndexScheduler(_runnable);
    }

    @After
    public void tearDown() throws Exception {
        _scheduler.deletePattern();
    }

    @Test
    public void testIsStarted() throws Exception {
        assertFalse(_scheduler.isStarted());

        _scheduler.setPattern("* * * * *");
        assertTrue(_scheduler.isStarted());

        _scheduler.deletePattern();
        assertFalse(_scheduler.isStarted());
    }

    @Test
    @Ignore
    public void testScheduling10() throws Exception {
        System.out.println("Sleep for 10 sec.");
        _runnable.setTime(1000L * 10L);
        _scheduler.setPattern("* * * * *");

        assertEquals(0, _runnable.getCount());
        System.out.println("Sleep for 61 sec.");
        Thread.sleep(1000L * 61L);
        assertEquals(1, _runnable.getCount());
    }

    @Test
    @Ignore
    public void testScheduling70() throws Exception {
        System.out.println("Sleep for 70 sec.");
        _runnable.setTime(1000L * 70L);
        _scheduler.setPattern("* * * * *");

        assertEquals(0, _runnable.getCount());
        System.out.println("Sleep for 121 sec.");
        Thread.sleep(1000L * 121L);
        assertEquals(1, _runnable.getCount());
    }

    @Test
    public void testPatternFile() throws Exception {
        final String pattern = "0 12 * * *";

        assertNull(_scheduler.getPattern());

        _scheduler.setPattern(pattern);
        assertEquals(pattern, _scheduler.getPattern());

        // lets simulate a new start
        final IndexScheduler scheduler2 = new IndexScheduler(_runnable);
        assertEquals(pattern, scheduler2.getPattern());
        scheduler2.deletePattern();
    }
}
