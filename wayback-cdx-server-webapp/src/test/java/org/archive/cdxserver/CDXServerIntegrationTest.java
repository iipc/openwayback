/**
 * 
 */
package org.archive.cdxserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

/**
 * 
 * http://localhost:8080/search/
 * http://localhost:8080/search/cdx?url=http://en.wikipedia.org/&matchType=
 * prefix
 * 
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class CDXServerIntegrationTest extends TestCase {

    public void testDefaultMatchType() throws IOException {
        check("cdx?url=http://bits.wikimedia.org/robots.txt", 200, 1);
        check("cdx?url=http://bits.wikimedia.org/", 200, 0);
        check("cdx?url=http://en.wikipedia.org/&matchType=prefix", 200, 4);
    }

    private List<String> check(String qurl, int expectedStatus,
            int expectedLines)
            throws IOException {
        String urlString = "http://localhost:8080/search/" + qurl;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        assertEquals("Status code mismatch", expectedStatus,
                conn.getResponseCode());
        InputStream is = conn.getInputStream();
        List<String> lines = IOUtils.readLines(is, "UTF-8");
        assertEquals("Line count mismatch", expectedLines, lines.size());
        return lines;
    }

}
