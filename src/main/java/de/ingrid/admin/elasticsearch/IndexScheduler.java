/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.ingrid.admin.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import it.sauronsoftware.cron4j.TaskExecutor;

@Service
public class IndexScheduler implements IConfigurable {

    private final Scheduler _scheduler;

    private final IndexRunnable _runnable;

    private String _scheduleId;

    private String _pattern;

    private File _patternFile;

    private static final Log LOG = LogFactory.getLog(IndexScheduler.class);

    private static class LockRunnable extends Task  {

        private final Runnable _runnable;

        private static boolean _isRunning = false;

        public LockRunnable(final Runnable runnable) {
            _runnable = runnable;
        }

        @Override
        public void execute(TaskExecutionContext arg0) throws RuntimeException {
            LOG.debug("trying to run index scheduler");
            if (!_isRunning) {
                LOG.info("starting and locking index scheduler");
                _isRunning = true;
                try {
                    _runnable.run();
                } catch (final Throwable t) {
                    LOG.error("Error during indexing", t);
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
    public IndexScheduler(final IndexRunnable runnable, Config config) {
        _runnable = runnable;
        _scheduler = new Scheduler();
        if (_runnable.getPlugDescription() != null) {
            configure(_runnable.getPlugDescription());
        }
        
        // if we want to index on startup we start a new Thread for this, since the
        // other services still need to be configured and we cannot let this thread sleep
        if (config.indexOnStartup) {
            LOG.info("Initial indexing on startup ...");
            new InitialIndexRun().start();
        }
    }

    public void setPattern(final String pattern) {
        _pattern = pattern;
        if (_patternFile != null) {
            savePatternFile();
        }
        schedule();
    }

    public String getPattern() {
        return _pattern;
    }

    public void deletePattern() {
        LOG.debug("delete pattern");
        _pattern = null;
        deletePatternFile();
        if (isStarted()) {
            LOG.info("stop scheduler");
            _scheduler.stop();
        }
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
        if (_pattern == null) {
            LOG.info("No valid pattern found: '" + _pattern + "'");
            return;
        }
        if (_scheduleId == null) {
            LOG.info("scheduling indexer with pattern '" + _pattern + "'");
            _scheduleId = _scheduler.schedule(_pattern, new LockRunnable(_runnable));
        } else {
            LOG.info("rescheduling indexer with pattern '" + _pattern + "'");
            _scheduler.reschedule(_scheduleId, _pattern);
        }
        if (!isStarted()) {
            LOG.info("start scheduler");
            _scheduler.start();
        }
    }
    
    public boolean isRunning() {
        if (!_scheduler.isStarted()) {
            return false;
        }
        for (TaskExecutor task : _scheduler.getExecutingTasks()) {
            if (task.isAlive()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean triggerManually() {
        try {
            if (!isRunning()) {
                if (!_scheduler.isStarted()) {
                    _scheduler.start();
                }
                _scheduler.launch( new LockRunnable(_runnable) );
                return true;
            }
        } catch (Exception e) {
            LOG.error("Error running task now!", e);
        }
        return false;
    }

    private void loadPatternFile() {
        LOG.debug("try to load pattern from file");
        try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(_patternFile))) {
            _pattern = (String) reader.readObject();
        } catch (final Exception e) {
            LOG.error(e);
        }
    }

    private void savePatternFile() {
        deletePatternFile();
        LOG.debug("saving pattern to file");
        try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(_patternFile))) {
            writer.writeObject(_pattern);
        } catch (final Exception e) {
            LOG.error(e);
        }
    }

    private void deletePatternFile() {
        if (_patternFile != null && _patternFile.exists()) {
            LOG.debug("deleting pattern file");
            _patternFile.delete();
        }
    }
    
    /**
     * A separate Thread to start an index action on startup.
     * Since some services need to be configured, we have to add a little delay here.
     * @author Andre
     *
     */
    private class InitialIndexRun extends Thread {
        
        // delay execution in ms
        private int delay = 30000;
        
        @Override
        public void run() {
            try {
                Thread.sleep( delay );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            triggerManually();
        }
    }
}
