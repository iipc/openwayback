package org.archive.wayback.exception;

import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.memento.MementoConstants;
import org.archive.wayback.webapp.AccessPoint;
import org.easymock.EasyMock;

/**
 * unit test for {@link BetterRequestException}.
 */
public class BetterRequestExceptionTest extends TestCase {

    /**
     * Create a WaybackRequest for a Memento Timemap URI.
     * @param mementoPrefix prefix to use for Memento requests
     * @return new WaybackRequest
     */
    protected final WaybackRequest createMementoWaybackRequest(String mementoPrefix) {
        AccessPoint accessPoint = new AccessPoint();
        WaybackRequest wbRequest = new WaybackRequest();
        wbRequest.setAccessPoint(accessPoint);
        wbRequest.setMementoAcceptDatetime(true);
        Properties configs = new Properties();
        configs.setProperty(MementoConstants.AGGREGATION_PREFIX_CONFIG, mementoPrefix);
        accessPoint.setConfigs(configs);
        return wbRequest;
    }

    protected final HttpServletResponse createResponseMock() {
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        response.setStatus(HttpServletResponse.SC_FOUND);
        return response;
    }

    /**
     * Test the response location and headers are set.
     */
    public void testGenerateResponse() {
        final String betterURI = "http://example.com/test";
        final String headerName = "Vary";
        final String headerValue = "Accept-Encoding";

        HttpServletResponse response = createResponseMock();
        response.setHeader("Location", betterURI);
        response.setHeader(headerName, headerValue);
        EasyMock.replay(response);

        BetterRequestException bre = new BetterRequestException(betterURI);
        bre.addHeader(headerName, headerValue);
        bre.generateResponse(response, null);
        EasyMock.verify(response);
    }

    /**
     * Test the redirect URI consists of all ASCII characters.
     */
    public void testGenerateResponseConvertsRedirectToASCII() {
        final String nonASCII = "http://mawdoo3.com/خاص:اتصال";
        final String uri = "http://mawdoo3.com/%D8%AE%D8%A7%D8%B5:%D8%A7%D8%AA%D8%B5%D8%A7%D9%84";

        HttpServletResponse response = createResponseMock();
        response.setHeader("Location", uri);
        EasyMock.replay(response);

        BetterRequestException bre = new BetterRequestException(nonASCII);
        bre.generateResponse(response, null);
        EasyMock.verify(response);
    }

    /**
     * Test a Memento redirect gets its prefix.
     */
    public void testGenerateResponseUsesMementoPrefix() {
        final String betterURI = "/timemap/link/http://example.com";
        final String mementoPrefix = "http://web.archive.org";

        HttpServletResponse response = createResponseMock();
        response.setHeader("Location", mementoPrefix + betterURI);
        EasyMock.replay(response);
        WaybackRequest wbRequest = createMementoWaybackRequest(mementoPrefix);

        BetterRequestException bre = new BetterRequestException(betterURI);
        bre.generateResponse(response, wbRequest);
        EasyMock.verify(response);
    }

    /**
     * Test a Memento redirect URI gets converted to ASCII.
     */
    public void testGenerateResponseMementoRequestToASCII() {
        final String path = "/timemap/link/";
        final String nonASCII = path + "http://ex.com/خاص:اتصال";
        final String mementoPrefix = "http://web.archive.org";
        final String uri = path + "http://ex.com/%D8%AE%D8%A7%D8%B5:%D8%A7%D8%AA%D8%B5%D8%A7%D9%84";

        HttpServletResponse response = createResponseMock();
        response.setHeader("Location", mementoPrefix + uri);
        EasyMock.replay(response);
        WaybackRequest wbRequest = createMementoWaybackRequest(mementoPrefix);

        BetterRequestException bre = new BetterRequestException(nonASCII);
        bre.generateResponse(response, wbRequest);
        EasyMock.verify(response);
    }
}
