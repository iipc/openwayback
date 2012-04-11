package org.archive.accesscontrol.robotstxt;

import org.apache.commons.httpclient.URIException;

import junit.framework.TestCase;

public class HttpRobotClientTest extends TestCase {
    public void testRobotUrlForUrl() throws URIException {
        assertEquals("http://example.com/robots.txt", HttpRobotClient.robotsUrlForUrl("http://example.com/")); 
        assertEquals("http://example.com/robots.txt", HttpRobotClient.robotsUrlForUrl("http://example.com/foo/bar.html?boozle#bazzle")); 
        assertEquals("https://example.com/robots.txt", HttpRobotClient.robotsUrlForUrl("https://example.com/foo/bar.html?boozle#bazzle")); 
        assertEquals("https://user@example.com/robots.txt", HttpRobotClient.robotsUrlForUrl("https://user@example.com/foo/bar.html?boozle#bazzle")); 
        assertEquals("http://user:pass@example.com/robots.txt", HttpRobotClient.robotsUrlForUrl("http://user:pass@example.com/foo/bar.html?boozle#bazzle")); 
        assertEquals("http://user:pass@example.com:2311/robots.txt", HttpRobotClient.robotsUrlForUrl("http://user:pass@example.com:2311/foo/bar.html?boozle#bazzle")); 
    }
    
    public void testBasic() throws Exception {
        HttpRobotClient client = new HttpRobotClient();
        assertTrue(client.isRobotPermitted("http://www.archive.org/index.html", "wayback-access-control-test"));
        assertTrue(client.isRobotPermitted("http://google.com/fish.html", "wayback-access-control-test"));
        assertFalse(client.isRobotPermitted("http://google.com/news", "wayback-access-control-test"));
        
    }
}
