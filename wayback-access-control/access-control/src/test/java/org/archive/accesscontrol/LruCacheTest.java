package org.archive.accesscontrol;

import junit.framework.TestCase;

public class LruCacheTest extends TestCase {
    LruCache<String,Integer> cache;
    
    public void testMaxItems() {
        cache = new LruCache<String,Integer>();
        
        cache.setMaxEntries(3);
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);
        
        assertEquals(3, cache.size());
        
        cache.put("four", 4);
        
        assertEquals("Maximum entry cap", 3, cache.size());
        assertNull("Ensure 'one' was the evicted object.", cache.get("one"));
        
    }
}
