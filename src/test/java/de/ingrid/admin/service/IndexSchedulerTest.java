package de.ingrid.admin.service;

import java.io.File;

import junit.framework.TestCase;

import org.mockito.MockitoAnnotations;

import de.ingrid.utils.PlugDescription;

public class IndexSchedulerTest extends TestCase {

    private DummyRunnable _runnable;

    private IndexScheduler _scheduler;

    private static class DummyRunnable extends IndexRunnable {
        private long _time;

        private int _counter = 0;

        public DummyRunnable(final long time) {
            super(null);
            _time = time;

            final PlugDescription pd = new PlugDescription();
            final File file = new File(System.getProperty("java.io.tmpdir"), this.getClass().getName());
            assertTrue(file.exists() ? true : file.mkdirs());
            pd.setWorkinDirectory(file);

            configure(pd);
        }

        @Override
        public void run() {
            _counter++;
            try {
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

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        _runnable = new DummyRunnable(1000L);
        _scheduler = new IndexScheduler(_runnable);
    }

    @Override
    protected void tearDown() throws Exception {
        _scheduler.deletePattern();
    }

    public void testIsStarted() throws Exception {
        assertFalse(_scheduler.isStarted());

        _scheduler.setPattern("* * * * *");
        assertTrue(_scheduler.isStarted());

        _scheduler.deletePattern();
        assertFalse(_scheduler.isStarted());
    }

    public void testScheduling10() throws Exception {
        _runnable.setTime(1000L * 10L);
        _scheduler.setPattern("* * * * *");

        assertEquals(0, _runnable.getCount());
        Thread.sleep(1000L * 61L);
        assertEquals(1, _runnable.getCount());
    }

    public void testScheduling70() throws Exception {
        _runnable.setTime(1000L * 70L);
        _scheduler.setPattern("* * * * *");

        assertEquals(0, _runnable.getCount());
        Thread.sleep(1000L * 121L);
        assertEquals(1, _runnable.getCount());
    }

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
