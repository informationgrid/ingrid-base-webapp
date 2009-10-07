package de.ingrid.admin.service;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

@Service
public class IndexScheduler implements IConfigurable {

    private final Scheduler _scheduler;

    private final IndexRunnable _runnable;

    private String _scheduleId;

    private String _pattern;

    private File _patternFile;

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
        if (_runnable.getPlugDescription() != null) {
            configure(_runnable.getPlugDescription());
        }
    }

    public void setPattern(final String pattern) {
        _pattern = pattern;
        schedule();
        if (_patternFile != null) {
            savePatternFile();
        }
    }

    public String getPattern() {
        return _pattern;
    }

    public void deletePattern() {
        _pattern = null;
        if (isStarted()) {
            _scheduler.stop();
        }
        deletePatternFile();
    }

    public boolean isStarted() {
        return _scheduler.isStarted();
    }

    @Override
    public void configure(final PlugDescription plugDescription) {
        _patternFile = new File(plugDescription.getWorkinDirectory(), "pattern");
        if (_patternFile.exists()) {
            loadPatternFile();
            schedule();
        }
    }

    private void schedule() {
        if (_scheduleId == null) {
            _scheduleId = _scheduler.schedule(_pattern, new LockRunnable(_runnable));
        } else {
            _scheduler.reschedule(_scheduleId, _pattern);
        }
        if (!isStarted()) {
            _scheduler.start();
        }
    }

    private void loadPatternFile() {
        try {
            final ObjectInputStream reader = new ObjectInputStream(new FileInputStream(_patternFile));
            _pattern = (String) reader.readObject();
            reader.close();
        } catch (final Exception e) {
            LOG.error(e);
        }
    }

    private void savePatternFile() {
        try {
            deletePatternFile();
            final ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(_patternFile));
            writer.writeObject(_pattern);
            writer.close();
        } catch (final Exception e) {
            LOG.error(e);
        }
    }

    private void deletePatternFile() {
        if (_patternFile != null && _patternFile.exists()) {
            _patternFile.delete();
        }
    }
}
