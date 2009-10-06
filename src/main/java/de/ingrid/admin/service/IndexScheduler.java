package de.ingrid.admin.service;

import it.sauronsoftware.cron4j.Scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class IndexScheduler {

    private final Scheduler _scheduler;

    private final Runnable _runnable;

    private String _scheduleId;

    private static final Log LOG = LogFactory.getLog(IndexScheduler.class);

    private static class LockRunnable implements Runnable {

        private final Runnable _runnable;

        private boolean _isRunning = false;

        public LockRunnable(final Runnable runnable) {
            _runnable = runnable;
        }

        @Override
        public void run() {
            LOG.debug("trying to run index scheduler");
            if (!_isRunning) {
                LOG.info("starting and locking index scheduler");
                _isRunning = true;
                try {
                    _runnable.run();
                } catch (final Throwable t) {
                    // TODO: logging
                    LOG.error(t);
                } finally {
                    LOG.info("unlocking index scheduler");
                    _isRunning = false;
                }
            } else {
                LOG.info("index scheduler is still busy");
            }
        }

    }

    @Autowired
    public IndexScheduler(final IndexRunnable runnable) {
        _runnable = runnable;
        _scheduler = new Scheduler();
    }

    public void schedule(final String pattern) {
        if (_scheduleId == null) {
            _scheduleId = _scheduler.schedule(pattern, new LockRunnable(_runnable));
        } else {
            _scheduler.reschedule(_scheduleId, pattern);
        }
    }

    public void start() {
        _scheduler.start();
    }

    public void stop() {
        _scheduler.stop();
    }

    public boolean isStarted() {
        return _scheduler.isStarted();
    }
}
