package io.floodplain.kalah;

import java.util.LinkedHashMap;
import java.util.Map;

public interface KalahGameStorage {
    public void save(String id, Map<String,Object> contents);
    public Map<String,Object> load(String id);

}
