package org.archive.accesscontrol.robotstxt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.archive.accesscontrol.LruCache;
import org.archive.accesscontrol.RobotsUnavailableException;

/**
 * The CchingRobotClient wraps another RobotClient and caches requests.
 * 
 * @author aosborne
 *
 */
public class CachingRobotClient extends RobotClient {
    private static final Logger LOGGER = Logger.getLogger(
            CachingRobotClient.class.getName());
    protected LruCache<String, RobotRules> cache = new LruCache<String, RobotRules>();
    protected RobotClient client;
    private static final int PREPARE_THREAD_COUNT = 15;
    
    public RobotClient getClient() {
        return client;
    }

    public void setClient(RobotClient client) {
        this.client = client;
    }

    public CachingRobotClient() {
        this.client = new HttpRobotClient();
    }
    
    public CachingRobotClient(RobotClient client) {
        this.client = client;
    }
    
    @Override
    public RobotRules getRulesForUrl(String url, String userAgent)
            throws IOException, RobotsUnavailableException {
        String robotsUrl = robotsUrlForUrl(url);
        RobotRules rules;
        
        synchronized(cache) {
            rules = cache.get(robotsUrl);
        }
        if (rules == null) {
            rules = client.getRulesForUrl(url, userAgent);
            
            synchronized(cache) {
                cache.put(robotsUrl, rules);
            }
        }
        return rules;
    }
    
    public LruCache<String, RobotRules> getCache() {
        return cache;
    }
    
    class FetchThread extends Thread {
        private List<String> urls;
        private String userAgent;

        public FetchThread(List<String> urls, String userAgent) {
            this.urls = urls;
            this.userAgent = userAgent;
        }

        public void run() {
            while (true) {
                String url;
                synchronized (urls) {
                    if (urls.isEmpty())
                        break;
                    url = urls.remove(0);
                }
                try {
                    getRulesForUrl(url, userAgent);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RobotsUnavailableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Prepare the cache to lookup info for a given set of urls. The fetches
     * happen in parallel so this also makes a good option for speeding up bulk lookups.
     */
    public void prepare(Collection<String> urls, String userAgent) {
        List<String> safeUrls = new ArrayList<String>(urls);
        FetchThread threads[] = new FetchThread[PREPARE_THREAD_COUNT ];
        for (int i = 0; i < PREPARE_THREAD_COUNT ; i++) {
            threads[i] = new FetchThread(safeUrls, userAgent);
            threads[i].start();
        }
        for (int i = 0; i < PREPARE_THREAD_COUNT ; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void setRobotProxy(String host, int port) {
        client.setRobotProxy(host, port);
    }

}
