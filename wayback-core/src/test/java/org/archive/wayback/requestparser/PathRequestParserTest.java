/**
 * 
 */
package org.archive.wayback.requestparser;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.archive.wayback.archivalurl.ArchivalUrl;
import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.ReplayRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.webapp.AccessPoint;

/**
 * unit tests for {@link PathRequestParser} implementations.
 * 
 * @contributor kenji
 *
 */
public class PathRequestParserTest extends TestCase {
    
    ArchivalUrlRequestParser parser = new ArchivalUrlRequestParser();
    WaybackRequest wbr;
    ArchivalUrl au;

    // XXXX PathRequestParser subclass constructor takes BaseRequetParser, not RequestParser
    // why??
    BaseRequestParser brp = new BaseRequestParser() {
        public int getMaxRecords() { return 10; }
        @Override
        public WaybackRequest parse(HttpServletRequest httpRequest,
                AccessPoint wbContext) throws BadQueryException,
                BetterRequestException {
            return null;
        }
    };
    PathRequestParser parsers[] = new PathRequestParser[] {
            new ReplayRequestParser(brp),
            new PathDatePrefixQueryRequestParser(brp),
            new PathDateRangeQueryRequestParser(brp),
            new PathPrefixDatePrefixQueryRequestParser(brp),
            new PathPrefixDateRangeQueryRequestParser(brp),
    };

    private ArchivalUrl parseAU(String path) 
    throws BetterRequestException, BadQueryException {
        WaybackRequest wbRequest = null;
        for(int i = 0; i < parsers.length; i++) {
            wbRequest = parsers[i].parse(path, null);
            if (wbRequest != null) {
                break;
            }
        }
        return new ArchivalUrl(wbRequest);
    }
    
    private void trt(String want, String src) {
        try {
            assertEquals(want,parseAU(src).toString());
        } catch (BetterRequestException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (BadQueryException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toString()}.
     * @throws BadQueryException 
     * @throws BetterRequestException 
     */
    public void testToString() throws BetterRequestException, BadQueryException {
        trt(
                "20010101000000/http://yahoo.com/",
                "20010101000000/http://yahoo.com/");
        
        trt(
                "20010101000000/http://yahoo.com/",
                "20010101000000/http://yahoo.com:80/");
        
        trt(
                "20010101000000/http://www.yahoo.com/",
                "20010101000000/http://www.yahoo.com:80/");
        trt(
                "20010101000000/http://www.yahoo.com/",
                "20010101000000/www.yahoo.com/");
        trt(
                "20010101000000/http://www.yahoo.com/",
                "20010101000000/www.yahoo.com:80/");
        
        trt(
                "20010101000000im_/http://www.yahoo.com/",
                "20010101000000im_/www.yahoo.com:80/");

        trt(
                "20010101235959im_/http://www.yahoo.com/",
                "20010101im_/www.yahoo.com:80/");
    }

    public void testReplayRequestParser() throws Exception {
        ReplayRequestParser parser = new ReplayRequestParser(brp);
        // note AccessPoint arg is unused
        {
            WaybackRequest wbr = parser.parse("20100101000000/http://www.yahoo.com/", null);
            assertEquals("replayTimestamp", "20100101000000", wbr.getReplayTimestamp());
            assertNull("startTimestamp", wbr.getStartTimestamp());
            // endTimestamp is current timestamp
            assertNotNull("endTimestamp", wbr.getEndTimestamp());
        }
        // old Alexa ARCs has 12-digits dates - padded with "00" -> same as prev case
        {
            WaybackRequest wbr = parser.parse("201001010000/http://www.yahoo.com/", null);
            assertEquals("replayTimestamp", "20100101000000", wbr.getReplayTimestamp());
            assertNull("startTimestamp", wbr.getStartTimestamp());
            // endTimestamp is current timestamp
            assertNotNull("endTimestamp", wbr.getEndTimestamp());
        }
        {
            WaybackRequest wbr = parser.parse("20100101/http://www.yahoo.com/", null);
            assertEquals("replayTimestamp", "20100101235959", wbr.getReplayTimestamp());
            assertEquals("startTimestamp", "20100101000000", wbr.getStartTimestamp());
            assertEquals("endTimestamp", "20100101235959", wbr.getEndTimestamp());
        }
        
    }

}
