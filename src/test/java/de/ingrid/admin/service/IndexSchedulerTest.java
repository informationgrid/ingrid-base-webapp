package de.ingrid.admin.service;

import junit.framework.TestCase;

import org.mockito.MockitoAnnotations;

public class IndexSchedulerTest extends TestCase {

    private class DummyRunnable extends IndexRunnable {
        private final long _time;

        private int _counter = 0;

        public DummyRunnable(final long time) {
            super(null);
            _time = time;
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

        public int getCount() {
            return _counter;
        }
    }

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public void testIsStarted() throws Exception {
        final IndexScheduler scheduler = new IndexScheduler(null);
        assertFalse(scheduler.isStarted());
        scheduler.start();
        assertTrue(scheduler.isStarted());
        scheduler.stop();
        assertFalse(scheduler.isStarted());
    }

    public void testScheduling10_61() throws Exception {
        final DummyRunnable runnable = new DummyRunnable(1000L * 10L);
        final IndexScheduler scheduler = new IndexScheduler(runnable);
        scheduler.schedule("* * * * *");

        assertEquals(0, runnable.getCount());
        scheduler.start();
        Thread.sleep(1000L * 61L);
        assertEquals(1, runnable.getCount());
        scheduler.stop();
    }

    public void testScheduling130_121() throws Exception {
        final DummyRunnable runnable = new DummyRunnable(1000L * 130L);
        final IndexScheduler scheduler = new IndexScheduler(runnable);
        scheduler.schedule("* * * * *");

        assertEquals(0, runnable.getCount());
        scheduler.start();
        Thread.sleep(1000L * 121L);
        assertEquals(1, runnable.getCount());
        scheduler.stop();
    }
}
