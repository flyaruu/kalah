package io.floodplain.kalah.stores;

import io.floodplain.kalah.KalahGameEngine;
import io.floodplain.kalah.KalahGameStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An in-memory implementation for running without FireStore
 */
public class KalahInMemoryStore implements KalahGameStorage {

    private final AtomicLong lastId = new AtomicLong(0);
    Map<String, KalahGameEngine> allStates = new HashMap<>();

    @Override
    public void save(String id, Map<String, Object> contents) {
        // TODO
    }

    @Override
    public Map<String, Object> load(String id) {
        return null;
    }

    @Override
    public String generateNextId() {
        return Long.toString(lastId.incrementAndGet());
    }
}
