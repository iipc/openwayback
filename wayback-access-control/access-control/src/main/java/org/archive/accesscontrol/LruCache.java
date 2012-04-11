package org.archive.accesscontrol;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple least recently used cache, with a discard policy based on maximum
 * entry count and cache time.  Access
 * 
 * @author aosborne
 * 
 * @param <K>
 * @param <V>
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;
    private int maxEntries = 100;
    private long maxCacheTime = 10 * 60 * 1000; // ten minutes
    private Map<K, Date> refreshTimes = new HashMap<K, Date>();

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries
                || new Date().getTime()
                        - refreshTimes.get(eldest.getKey()).getTime() > maxCacheTime;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    /**
     * Set the maximum number of entries to be stored in the cache.
     * 
     * @param maxEntries
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public long getMaxCacheTime() {
        return maxCacheTime;
    }

    /**
     * Set the maximum time in milliseconds an entry should be cached for.
     * 
     * @param maxCacheTime
     */
    public void setMaxCacheTime(long maxCacheTime) {
        this.maxCacheTime = maxCacheTime;
    }

    @Override
    public V put(K key, V value) {
        refreshTimes.put(key, new Date());
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        refreshTimes.remove(key);
        return super.remove(key);
    }
}
