/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;

@Service
public class CacheService {

    protected static final Logger LOG = Logger.getLogger(CacheService.class);

    public static final String INGRID_CACHE = "ingrid-cache";

    public static final String DEFAULT_CACHE = "default";

    private boolean _active = true;

    private int _lifeTime = 10;

    private boolean _diskStore = false;

    private int _elements = 1000;

    private PlugDescriptionService _plugDescriptionService;

    public CacheService() {
    }

    @Autowired
    public CacheService(final PlugDescriptionService plugDescriptionService) throws IOException {
        _plugDescriptionService = plugDescriptionService;
        loadFromPlugDescription();
        updateIngridCache();
    }

    public void loadFromPlugDescription() throws IOException {
        if (_plugDescriptionService != null) {
            // load setting
            final PlugDescription plugDescription = _plugDescriptionService.getPlugDescription();
            if (plugDescription != null) {
                LOG.info("load cache settings from plug description");
                if (plugDescription.containsKey(PlugDescription.CACHE_ACTIVE)) {
                    _active = plugDescription.getCacheActive();
                }
                if (plugDescription.containsKey(PlugDescription.CACHED_ELEMENTS)) {
                    _elements = plugDescription.getCachedElements();
                }
                if (plugDescription.containsKey(PlugDescription.CACHED_IN_DISK_STORE)) {
                    _diskStore = plugDescription.getCachedInDiskStore();
                }
                if (plugDescription.containsKey(PlugDescription.CACHED_LIFE_TIME)) {
                    _lifeTime = plugDescription.getCachedLifeTime();
                }
            }
        } else {
            LOG.warn("try to use function without necessary plug description service");
        }
    }

    public void updateCache(CacheService service) throws Exception {
        if (_plugDescriptionService != null) {
            if (service == null) {
                service = this;
            }
            _active = service.getActive();
            _lifeTime = service.getLifeTime();
            _elements = service.getElementsCount();
            _diskStore = service.getDiskStore();

            // update plug description
            LOG.info("updating cache setting in plug description");
            final PlugdescriptionCommandObject plugDescription = _plugDescriptionService.getCommandObect();
            plugDescription.setCacheActive(_active);
            plugDescription.setCachedLifeTime(_lifeTime);
            plugDescription.setCachedElements(_elements);
            plugDescription.setCachedInDiskStore(_diskStore);
            JettyStarter.getInstance().config.writePlugdescriptionToProperties( plugDescription );
            _plugDescriptionService.savePlugDescription(plugDescription);
        } else {
            LOG.warn("try to use function without necessary plug description service");
        }
    }

    public void updateIngridCache() {
        final CacheManager manager = CacheManager.getInstance();

        // clear the cache
        if (manager.cacheExists(INGRID_CACHE)) {
            LOG.info("removing " + INGRID_CACHE + " cache");
            manager.removeCache(INGRID_CACHE);
        }
        if (manager.cacheExists(DEFAULT_CACHE)) {
            LOG.info("removing "+DEFAULT_CACHE+" cache");
            manager.removeCache(DEFAULT_CACHE);
        }

        // update cache
        if (_active) {
            LOG.info("elements: " + _elements);
            LOG.info("diskStore: " + _diskStore);
            LOG.info("lifeTime: " + _lifeTime + "min");
            final int lifeTime = getLifeTimeInSeconds();
            final Cache cache = new Cache(INGRID_CACHE, _elements, _diskStore, getEternal(), lifeTime, lifeTime);
            manager.addCache(cache);
            LOG.info("cache is now activated");
        } else {
            LOG.info("cache is now deactivated");
        }
    }

    public void setActive(final boolean active) {
        _active = active;
    }

    public boolean getActive() {
        return _active;
    }

    public void setLifeTime(final int lifeTime) {
        _lifeTime = lifeTime;
    }

    public int getLifeTime() {
        return _lifeTime;
    }

    public int getLifeTimeInSeconds() {
        return _lifeTime * 60;
    }

    public void setDiskStore(final boolean diskStore) {
        _diskStore = diskStore;
    }

    public boolean getDiskStore() {
        return _diskStore;
    }

    public void setElementsCount(final int elementsCount) {
        _elements = elementsCount;
    }

    public int getElementsCount() {
        return _elements;
    }

    public boolean getEternal() {
        return _lifeTime <= 0;
    }
}
