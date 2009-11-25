package de.ingrid.admin.service;

import org.springframework.stereotype.Service;

@Service
public class CacheService {

    public static final String NAME = "ingrid-cache";

    private boolean _active = true;

    private int _lifeTime = 10;

    private boolean _diskStore = false;

    private int _elements = 1000;

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

    public void set(final CacheService service) {
        _active = service.getActive();
        _lifeTime = service.getLifeTime();
        _diskStore = service.getDiskStore();
        _elements = service.getElementsCount();
    }
}
