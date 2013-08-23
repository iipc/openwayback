/**
 * 
 */
package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.RequestParser;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;
import org.archive.wayback.util.webapp.RequestMapper;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

/**
 * unit test for {@link AccessPoint}.
 * 
 * @author kenji
 *
 */
public class AccessPointTest extends TestCase {

    AccessPoint cut;
    
    // AccessPoint public interface
    // init()
    // handleRequest()
    // queryIndex(WaybackRequest) - actually it's only used internally. it's public just because 
    //   LiveWebAccessPoint is reusing it. LiveWebAccessPoint also calls getReplay() for rendering
    //   resource in accordance with rendering mode configured.
    // shutdown()
    
    // dependencies
    // selfRedirectCanonicalizer: UrlCanonicalizer
    // filterFactory: CustomResultFilterFactory
    // exclusionFactory: ExclusionFilterFactory
    // uriConverter: ResultURIConverter
    // replay: ReplayDispatcher
    // parser: RequestParser
    // query: QueryRenderer
    // exception: ExceptionRenderer
    // collection: WaybackCollection
    // - resourceStore: ResourceStore
    // - resourceIndex: ResourceIndex
    // configs: Properties
    // liveWebRedirector: LiveWebRedirector
    WaybackCollection collection;
    ResourceStore resourceStore;
    ResourceIndex resourceIndex;
    
    HttpServletRequest httpRequest;
    HttpServletResponse httpResponse;
    RequestDispatcher requestDispatcher;
    
    RequestParser parser;
    //ResultURIConverter uriConverter;
    QueryRenderer query;
    ReplayDispatcher replay;
    
    WaybackRequest wbRequest;
    
    ReplayRenderer replayRenderer;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        cut = new AccessPoint();
        cut.setEnablePerfStatsHeader(false);
        cut.setEnableMemento(false);
        cut.setExclusionFactory(null);
        cut.setExactSchemeMatch(false); // default
        cut.setExactHostMatch(false); // default
        cut.setEnableWarcFileHeader(false);
        
        resourceStore = EasyMock.createMock(ResourceStore.class);
        resourceIndex = EasyMock.createMock(ResourceIndex.class);
        collection = new WaybackCollection();
        collection.setResourceIndex(resourceIndex);
        collection.setResourceStore(resourceStore);
        cut.setCollection(collection);
        
        // behavior returning null are commented out because EasyMock provides them by default.
        httpRequest = EasyMock.createNiceMock(HttpServletRequest.class);
        httpResponse = EasyMock.createMock(HttpServletResponse.class);        
        // RequestMapper.REQUEST_CONTEXT_PREFIX is private!
        //EasyMock.expect(httpRequest.getAttribute("webapp-request-context-path-prefix")).andReturn(null);
        EasyMock.expect(httpRequest.getRequestURI()).andStubReturn("/");
        EasyMock.expect(httpRequest.getRequestURL()).andStubReturn(new StringBuffer("/"));
        //EasyMock.expect(httpRequest.getQueryString()).andReturn(null);
        // remote address test
        //EasyMock.expect(httpRequest.getHeader("X-Forwarded-For")).andReturn(null);
        // Ajax mode test
        //EasyMock.expect(httpRequest.getHeader("X-Requested-With")).andReturn("XMLHttpRequest");
        
        // Memento mode - only called when enableMemento==true.
        //EasyMock.expect(httpRequest.getHeader(MementoUtils.ACCEPT_DATETIME)).andReturn(null);
        EasyMock.expect(httpRequest.getLocalName()).andStubReturn("localhost");
        //EasyMock.expect(httpRequest.getAuthType()).andReturn(null).anyTimes();
        //EasyMock.expect(httpRequest.getRemoteUser()).andReturn(null).anyTimes();
        //EasyMock.expect(httpRequest.getHeader(WaybackRequest.REQUEST_AUTHORIZATION)).andReturn(null);
        EasyMock.expect(httpRequest.getLocalPort()).andStubReturn(8080);
        EasyMock.expect(httpRequest.getContextPath()).andStubReturn("/");
        EasyMock.expect(httpRequest.getLocale()).andStubReturn(Locale.CANADA_FRENCH);
        
        // RequestDispatcher - setup expectations, call replay() and verify() if
        // method calls are expected.
        requestDispatcher = EasyMock.createMock(RequestDispatcher.class);
        EasyMock.expect(
                httpRequest.getRequestDispatcher(EasyMock.<String> notNull()))
                .andStubReturn(requestDispatcher);
        //EasyMock.expect(httpRequest.getCookies()).andReturn(null).anyTimes();
        
        // as we mock-ify RequestParser, WaybackRequest can be independent of httpRequest.
        // it suggests HttpServletRequest method calls above are better be made through 
        // RequestParser (TODO)
        wbRequest = new WaybackRequest();
        parser = EasyMock.createMock(RequestParser.class);
        cut.setParser(parser);
        EasyMock.expect(parser.parse(httpRequest, cut)).andReturn(wbRequest);
        EasyMock.replay(parser);
        
        query = EasyMock.createMock(QueryRenderer.class);
        cut.setQuery(query);
        
        replay = EasyMock.createMock(ReplayDispatcher.class);
        cut.setReplay(replay);
        
        replayRenderer = EasyMock.createMock(ReplayRenderer.class);
        
        {
            ArchivalUrlResultURIConverter uc = new ArchivalUrlResultURIConverter();
            uc.setReplayURIPrefix("/web/");
            cut.setUriConverter(uc);
        }
    }
    
    public static Resource createTestHtmlResource(byte[] payloadBytes) throws IOException {
        WARCRecordInfo recinfo = TestWARCRecordInfo.createCompressedHttpResponse("text/html", payloadBytes);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        WarcResource resource = new WarcResource(rec, ar);
        resource.parseHeaders();
        return resource;
    }
    public static Resource createTestRevisitResource(int len, boolean withHeader) throws IOException {
        WARCRecordInfo recinfo = TestWARCRecordInfo.createRevisitHttpResponse("text/html", len, withHeader);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        WarcResource resource = new WarcResource(rec, ar);
        resource.parseHeaders();
        return resource;
    }
    
    /**
     * creates test CaptureSearchResults with just one CaptureSearchResult
     * @param originalUrl
     * @param timestamp capture timestamp ("YYYYmmddHHMMSS")
     * @param status HTTP status code
     * @return
     */
    public static CaptureSearchResults createCaptureSearchResults(String timestamp, String originalUrl, String status) {
        return createCaptureSearchResults(createCaptureSearchResult(timestamp, originalUrl, status));
    }
    /**
     * create CaptureSearchResults with a sequence of CaptureSearchResult-s.
     * last one is set as closest.
     * @param results
     * @return
     */
    public static CaptureSearchResults createCaptureSearchResults(CaptureSearchResult... captures) {
        if (captures.length == 0) throw new IllegalArgumentException("needs at least one CaptureSearchResult");
        CaptureSearchResults results = new CaptureSearchResults();
        for (CaptureSearchResult result : captures) {
            results.addSearchResult(result);
        }
        results.setClosest(captures[captures.length - 1]);
        return results;
    }
    public static CaptureSearchResult createCaptureSearchResult(String timestamp, String originalUrl, String status) {
        CaptureSearchResult result = new CaptureSearchResult();
        result.setOriginalUrl(originalUrl);
        result.setHttpCode(status);
        // closest.captureTimestamp != wbRequest.replayTimestamp
        result.setCaptureTimestamp(timestamp);
        return result;
    }

    /**
     * test basic behavior 1.
     * <ul>
     * <li>no authorization</li>
     * <li>no exclusion factory</li>
     * </ul>
     * and,
     * <ul>
     * <li>
     * <li>there's no capture on date matching the request.</li>
     * <li>closest capture is not a revisit</li>
     * </li>
     * this shall result in redirect (302) response to the URL with
     * closest capture date in date component.
     * 
     * alternative path: {@link #testBounceToReplayPrefix()}
     * @throws Exception
     */
    public void testHandleRequest_Replay_1() throws Exception {
        // make sure wbRequesat.requestUrl, replayTimestamp are set up.
        wbRequest.setReplayRequest();
        wbRequest.setRequestUrl("http://www.example.com/");
        wbRequest.setReplayTimestamp("20100601123456");
        
        // handleRequest()
        // calls handleReplay()
        // - calls checkInterstitialRedirect()
        // - calls selfRedirectCanonicalizer.urlStringToKey(requestURL) if non-null
        // - calls queryIndex(), which calls collection.resourceIndex.query(wbRequest)
        // TODO: originalUrl can be different from wbRequst.requestUrl, and it will be
        // reflected to redirect URL (worth testing).
        CaptureSearchResults results = createCaptureSearchResults(
                "20100601000000", "http://www.eample.com/", "200");
        CaptureSearchResult closest = results.getClosest();
        // TODO: this can be different from wbRequst.requestUrl, and it will be reflected
        // to redirect URL.
        closest.setOriginalUrl("http://www.example.com/");
        closest.setHttpCode("200");
        // closest.captureTimestamp != wbRequest.replayTimestamp
        closest.setCaptureTimestamp("20100601000000");
        results.setClosest(closest);
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        // - calls replay.getClosest()
        EasyMock.expect(replay.getClosest(wbRequest, results)).andReturn(closest);
        // - calls checkAnchorWindow()
        // - calls getResource(closest, skipFiles), which
        //   - first checks if closest is in skipFiles (and throws
        //     ResourceNotAvailableException if it is),
        //   - then calls collection.resourceStore.retrieveResource(closest)
        // as AccessPoint performs a few tests on payloadResource, this cannot be null.
        // retrieveResource is called for redirect case as well.
        Resource payloadResource = createTestHtmlResource("hogehogehogehoge\n".getBytes("UTF-8"));
        EasyMock.expect(resourceStore.retrieveResource(closest)).andReturn(payloadResource);
        
        // when closest's timestamp is different from replay requests's timestamp, it redirects
        // to closest's timestamp.
        httpResponse.setStatus(302);
        httpResponse.setHeader("Location", "/web/20100601000000/http://www.example.com/");
        
        EasyMock.replay(httpRequest, httpResponse, resourceIndex, resourceStore, replay);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        
        EasyMock.verify(resourceIndex, resourceStore, replay);
        
        assertTrue("handleRequest return value", r);
    }
    
    /**
     * basic replay test part 2.
     * there's a capture whose capture date matches the request.
     * @throws Exception
     */
    public void testHandleRequest_Replay_2() throws Exception {
        // make sure wbRequesat.requestUrl, replayTimestamp are set up.
        wbRequest.setReplayRequest();
        wbRequest.setRequestUrl("http://www.example.com/");
        wbRequest.setReplayTimestamp("20100601000000");
        
        // handleRequest()
        // calls handleReplay()
        // - calls checkInterstitialRedirect()
        // - calls selfRedirectCanonicalizer.urlStringToKey(requestURL) if non-null
        // - calls queryIndex(), which calls collection.resourceIndex.query(wbRequest)
        CaptureSearchResults results = createCaptureSearchResults("20100601000000", "http://www.example.com/",  "200");
        CaptureSearchResult closest = results.getClosest();
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        // - calls replay.getClosest()
        EasyMock.expect(replay.getClosest(wbRequest, results)).andReturn(closest);
        // - calls checkAnchorWindow()
        // - calls getResource(closest, skipFiles), which first checks if closest is in skipFiles
        //   (and throws ResourceNotAvailableException if it is),
        //   then calls collection.resourceStore.retrieveResource(closest)
        // as AccessPoint performs a few tests on payloadResource, this cannot be null.
        Resource payloadResource = createTestHtmlResource("hogehogehogehoge\n".getBytes("UTF-8"));
        EasyMock.expect(resourceStore.retrieveResource(closest)).andReturn(payloadResource);
        
        // when closest's timestamp == request's timestamp,
        // it gets ReplayRenderer with replay.getRenderer(wbRequest, closest, httpHeaderResource, payloadResource),
        // and calls renderResource() on it.
        EasyMock.expect(replay.getRenderer(wbRequest, closest, payloadResource, payloadResource)).andReturn(replayRenderer);
        // calls replayRenderer.renderResource(...)
        replayRenderer.renderResource(httpRequest, httpResponse, wbRequest,
                closest, payloadResource, payloadResource, cut.getUriConverter(),
                results);
        
        EasyMock.replay(httpRequest, httpResponse, resourceIndex, resourceStore, replay);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);

        EasyMock.verify(resourceIndex, resourceStore, replay);
        
        assertTrue("handleRequest return value", r);
    }
    
    /**
     * test CaptureSearchResult equality by file and offset.
     */
    public static class CaptureSearchResultMatcher implements IArgumentMatcher {
        private CaptureSearchResult expected;
        public CaptureSearchResultMatcher(CaptureSearchResult expected) {
            this.expected = expected;
        }
        @Override
        public boolean matches(Object actual) {
            if (!(actual instanceof CaptureSearchResult)) return false;
            String file = ((CaptureSearchResult)actual).getFile();
            long offset = ((CaptureSearchResult)actual).getOffset();
            if (file == null && expected.getFile() != null) return false;
            return file.equals(expected.getFile()) && offset == expected.getOffset();
        }

        @Override
        public void appendTo(StringBuffer buffer) {
            buffer.append("eqCaptureSearchResult(");
            buffer.append(expected.getFile());
            buffer.append(",");
            buffer.append(expected.getOffset());
            buffer.append(")");
        }
        
    }
    public static CaptureSearchResult eqCaptureSearchResult(CaptureSearchResult expected) {
        EasyMock.reportMatcher(new CaptureSearchResultMatcher(expected));
        return null;
    }
    
    /**
     * test of revisit. 
     * closest capture is a revisit. 
     * @throws Exception
     */
    public void testHandleRequest_Replay_Revisit() throws Exception {
        wbRequest.setReplayRequest();
        wbRequest.setRequestUrl("http://www.example.com/");
        wbRequest.setReplayTimestamp("20100601000000");
        // closest SearchResult has isDuplicateDigest() == true.
        CaptureSearchResult previous = createCaptureSearchResult("20100501000001", "http://www.example.com/", "200");
        CaptureSearchResult closest = createCaptureSearchResult("20100601000000", "http://www.example.com/", "200");
        previous.setFile("aaa.warc.gz");
        previous.setOffset(0);
        closest.flagDuplicateDigest(previous); // right?
        assertTrue(closest.isDuplicateDigest());
        assertTrue(closest.getDuplicatePayloadFile() != null);
        assertTrue(closest.getDuplicatePayloadOffset() != null);
        CaptureSearchResults results = createCaptureSearchResults(previous, closest);
        // called from AccessPoint#handleReplay(WaybackRequest, HttpServletRequest, HttpServletResponse)
        // called through AccessPoint#queryIndex(WaybackRequest)
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        EasyMock.expect(replay.getClosest(wbRequest, results)).andReturn(closest);

        // calls getResource(closest, skipFiles) -> httpHeaderResource
        // calls retrievePayloadForIdenticalContentRevisit(httpHeaderResource, captureResults, closest, skipFiles)
        byte[] payload = "hogehogehogehoge\n".getBytes("UTF-8");
        Resource headerResource = createTestRevisitResource(payload.length, true);
        Resource payloadResource = createTestHtmlResource(payload);
        // calls made through AccessPoint#getResource(...)
        EasyMock.expect(resourceStore.retrieveResource(closest)).andReturn(headerResource);
        //EasyMock.expect(resourceStore.retrieveResource(previous)).andReturn(payloadResource);
        EasyMock.expect(resourceStore.retrieveResource(eqCaptureSearchResult(previous))).andReturn(payloadResource);

        EasyMock.expect(replay.getRenderer(wbRequest, closest, headerResource, payloadResource)).andReturn(replayRenderer);
        // calls replayRenderer.renderResource(...)
        replayRenderer.renderResource(httpRequest, httpResponse, wbRequest,
                closest, headerResource, payloadResource, cut.getUriConverter(),
                results);
        
        EasyMock.replay(httpRequest, httpResponse, resourceIndex, resourceStore, replay);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);

        EasyMock.verify(resourceIndex, resourceStore, replay);
        
        assertTrue("handleRequest return value", r);

        // TODO: failure case: closest.duplicatePayloadFile != null -> ResourceNotAvailableException
        // TODO: failure case: self-redirecting -> calls finxNextClosest() and fails if there's no more closest.
        // wbRequest.timestampSearchKey == true -> calls queryIndex() once again.
        
    }
    
    /**
     * old-style WARC revisit (no HTTP status line and header, 
     * Content-Length in WARC header is zero).
     * it shall replay HTTP status line, headers and content from previous matching capture.
     * @throws Exception
     */
    public void testHandleRequest_Replay_OldWARCRevisit() throws Exception {
        // TODO
    }
    
    /**
     * old-style ARC revisit (no mimetype, filename is '-')
     * @throws Exception
     */
    public void testHandleRequest_Replay_OldARCRevisit() throws Exception {
        // TODO
    }
    
    public static final Resource createTest502Resource() throws IOException {
        byte[] failPayload = "failed\n".getBytes("UTF-8");
        byte[] content = TestWARCRecordInfo.buildHttpResponseBlock("502 Bad Gateway", "text/plain", failPayload);
  
        TestWARCRecordInfo recinfo = new TestWARCRecordInfo(content);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        WarcResource resource = new WarcResource(rec, ar);
        resource.parseHeaders();
        return resource;
    }
    
    /**
     * if closest is not HTTP-success AND replaying embedded context (CSS, JavaScript, images, etc.),
     * use next closest with successful response, or for lower priority, a redirect, instead.
     * unless such capture is of the same timestamp as the replay request, redirect to the capture found.
     * @throws Exception
     */
    public void testHandleRequest_Replay_Embedded() throws Exception {
        wbRequest.setReplayRequest();
        wbRequest.setRequestUrl("http://www.example.com/style.css");
        // request timestamp is different from 'previous' below. it makes handleRequest
        // return redirect. in this case, Resource for 'previous' will not be retrieved.
        wbRequest.setReplayTimestamp("20100601000000");
        // if closest is not HTTP-success, 
        // to have isAnyEmbeddedContext() return true - any of cSSContext, iMGContext, jSContext
        // frameWrapperContext, iFrameWrapperContext, objectEmbedContext has the same effect.
        wbRequest.setCSSContext(true);
        assertTrue(wbRequest.isAnyEmbeddedContext());
        
        CaptureSearchResult previous = createCaptureSearchResult(
                "20100501000000", "http://www.example.com/style.css", "200");
        CaptureSearchResult closest = createCaptureSearchResult(
                "20100515000000", "http://www.example.com/style.css", "502");
        assertTrue(closest.isHttpError());
        CaptureSearchResults results = createCaptureSearchResults(previous, closest);
        // handleRequest -> handleReplay -> queryIndex -> ResourceIndex#query
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        EasyMock.expect(replay.getClosest(wbRequest, results)).andReturn(closest);
        
        Resource failResource = createTest502Resource();
        EasyMock.expect(resourceStore.retrieveResource(closest)).andReturn(failResource);
        // this is called only when request timestamp == previous's timestamp
        //byte[] successPayload = "success\n".getBytes("UTF-8");
        //Resource successResource = createTestHtmlResource(successPayload);
        //EasyMock.expect(resourceStore.retrieveResource(previous)).andReturn(successResource);
        // replay.getRenderer() is not called.
        // renderer.renderResource() is not called.

        // or wbRequest.setBestLatestReplayRequest();
        final String expectedRedirectURI = "/web/20100501000000cs_/http://www.example.com/style.css";
        // betterURI is 
        // (in memento mode, memento-prefix is prepended to betterURI.)
        httpResponse.setHeader("Location", expectedRedirectURI);
        httpResponse.setStatus(302);
        // TODO: extraHeaders expectations?
        
        EasyMock.replay(httpRequest, httpResponse, resourceIndex, resourceStore, query, replay);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        // handleReplay throws BetterRequestException and handled inside handleRequest()
        // exception will not be thrown out of handleRequest().
        
        EasyMock.verify(httpResponse, resourceIndex, resourceStore, query, replay);
        assertTrue("handleRequest return value", r);
    }
    
    // REFACTORING THOUGHTS: query rendering could be done in the same mechanism as replay rendering.
    // there's no particular reason CaptureSearchResults rendering and UrlSearchResults
    // rendering must be implemented in the same class. they share nothing.
    // ReplayRenderer and QueryRenderer may be unified by passing UIResults instead of 
    // (CaptureSearchResult, Resource, CaptureSearchResults) for ReplayRenderer, and
    // (CaptureSearchResults / UrlSearchResults) for QueryRenderer.
    // this way, query.Renderer could be replaced by generic "variant dispatcher" class
    // that dispatches rendering to different JSPs depending on the type of output (HTML
    // or XML).
    
    public void testHandleRequest_CaptureSearchResults() throws Exception {
        wbRequest.setCaptureQueryRequest();
        wbRequest.setRequestUrl("http://www.example.com/");
        wbRequest.setReplayTimestamp("20100601123456");

        // handleRequest()
        // redirect to queryPrefix + translateRequestPathQuery(httpRequest)
        //   if bounceToQueryPrefix is true (not tested here)
        // copies exactHostMatch to wbRequest.exactHost (TODO: should be done by parser?)
        // calls handleQuery()
        // - calls queryIndex(), which calls collection.resourceIndex.query(),
        //     which returns CaptureSearchResults
        //   (unexpected object from queryIndex() results in WaybackException("Unknown index format").
        //    this is considered to be a programming/configuration error. not tested.)
        CaptureSearchResults results = new CaptureSearchResults();
        CaptureSearchResult result = new CaptureSearchResult();
        results.setClosest(result);
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        // - calls MementoUtils.printTimemapResponse(results, wbRequest, httpResponse) instead
        //     if wbRequst.isMementoTimemapRequest() (N/A here) (TODO: can we move this to
        //     QueryRenderer implementation?)
        // - calls query.renderCaptureResults(...)
        query.renderCaptureResults(httpRequest, httpResponse, wbRequest, results, cut.getUriConverter());
        
        EasyMock.replay(httpRequest, httpResponse, resourceIndex, query);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        
        EasyMock.verify(query);
        
        // result shall have closest flag set (FIrefox proxy plugin expects this)
        assertTrue("closest flag", result.isClosest());
        
        assertTrue("handleRequest return value", r);
        
    }
    
    public void testHandleRequest_UrlSearchResults() throws Exception {
        wbRequest.setUrlQueryRequest();
        wbRequest.setRequestUrl("http://www.example.com/");
        wbRequest.setReplayTimestamp("20100601123456");

        // AccessPoint is not concerned of the details of UrlSearchResults. it just
        // forwards the request to QueryRenderer. so we leave it uninitialized here.
        UrlSearchResults results = new UrlSearchResults();
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        query.renderUrlResults(httpRequest, httpResponse, wbRequest, results, cut.getUriConverter());
        
        EasyMock.replay(httpRequest, httpResponse, query, resourceIndex);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        // calls handleQuery()
        // - calls queryIndex()
        //   - calls collection.resourceIndex.query() - returns UrlSearchResults
        // - calls getQuery().renderUrlResults() for UrlSearchResults
        
        EasyMock.verify(query, resourceIndex);
        assertTrue("handleRequest return value", r);
    }

    /**
     * if bounceToReplayPrefix is true, replay request is redirected to
     * other access point.
     * @throws Exception
     */
    public void testBounceToReplayPrefix() throws Exception {
        final String URL = "http://www.example.com/";
        final String TIMESTAMP = "20100601123456";
        
        wbRequest.setReplayRequest();
        wbRequest.setRequestUrl(URL);
        wbRequest.setReplayTimestamp(TIMESTAMP);

        EasyMock.reset(httpRequest);
        EasyMock.expect(httpRequest.getRequestURI()).andStubReturn("/" + TIMESTAMP + "/" + URL);
        EasyMock.expect(httpRequest.getLocalName()).andStubReturn("localhost");
        EasyMock.expect(httpRequest.getLocalPort()).andStubReturn(8080);
        EasyMock.expect(httpRequest.getContextPath()).andStubReturn("/");
        EasyMock.expect(httpRequest.getLocale()).andStubReturn(Locale.CANADA_FRENCH);

        final String replayPrefix = "http://test.archive.org/";
        cut.setBounceToReplayPrefix(true);
        cut.setReplayPrefix(replayPrefix);

        final String suffix = "/" + TIMESTAMP + "/" + URL;
        httpResponse.sendRedirect(replayPrefix + suffix);
        
        EasyMock.replay(httpRequest, httpResponse);
        
        cut.handleRequest(httpRequest, httpResponse);

    }
    
    // TODO: the way AccessPoint is reused for rendering static resource looks inefficient.
    // bounceToReplayPrefix and bounceToQueryPrefix are always configured in pair, and they 
    // are set to true only for static resource AccessPoint.
    
    /**
     * static AccessPoint - configured with
     * <ul>
     * <li>accessPointPath=<code>${wayback.staticPrefix}</code></li>
     * <li>serveStatic=true</li>
     * <li>bounceToReplayPrefix=true</li>
     * <li>bounceToQueryPrefix=true</li>
     * </ul>
     * when {@link RequestParser#parse(HttpServletRequest, AccessPoint)} returns null,
     * request is forwarded to dispatchLocal() for rendering static resources.
     * @throws Exception
     */
    public void testDispatchLocal() throws Exception {
        // first rest the mock for overriding getAttribute(), getRequestURI(), and getRequestURL()
        EasyMock.reset(httpRequest);
        EasyMock.expect(httpRequest.getLocalName()).andStubReturn("localhost");
        EasyMock.expect(httpRequest.getLocalPort()).andStubReturn(8080);
        EasyMock.expect(httpRequest.getContextPath()).andStubReturn("/static");
        EasyMock.expect(httpRequest.getLocale()).andStubReturn(Locale.CANADA_FRENCH);
        EasyMock.expect(httpRequest.getRequestDispatcher(EasyMock.<String>notNull())).andStubReturn(requestDispatcher);

        // used by RequestMapper#getRequestPathPrefix(HttpServletRequest)
        // typical value found in ia-wayback-projects/projects/global-wayback/configs/local/wayback.properties
        // TODO: RequestMapper#getRequestContextPath(HttpServletRequest) assumes value of this
        // attribute ends with "/". RequestMapper has constant declaration for 
        // "webapp-request-context-path-prefix", but it's private.
        EasyMock.expect(
                httpRequest.getAttribute("webapp-request-context-path-prefix"))
                .andStubReturn("/static/");
        // override getRequestURI() behavior
        EasyMock.expect(httpRequest.getRequestURI()).andStubReturn("/static/aaa.css");
        EasyMock.expect(httpRequest.getRequestURL()).andStubReturn(new StringBuffer("/static/aaa.css"));
        
        // replace default RequestParser mock with the one returning null, which signifies
        // there's no dynamic handler and the request shall be mapped to local static resource.
        // (AccessPoint#dispatchLocal(HttpServletRequest))
        parser = EasyMock.createMock(RequestParser.class);
        cut.setParser(parser);
        EasyMock.expect(parser.parse(httpRequest, cut)).andReturn(null);
        
        // AccessPoint#dispatchLocal() checks existence of the file if ServletContext#getRealPath()
        // returns non-null value for translated request path. have it skip the test by returning
        // null. otherwise dispatchLocal() will fail.
        ServletContext servletContext = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(servletContext.getRealPath(EasyMock.<String> notNull()))
                .andStubReturn(null);
        cut.setServletContext(servletContext);
        
        // Expectation: AccessPoint#dispatchLocal() eventually calls RequestDispatcher#forward(...)
        requestDispatcher.forward(httpRequest, httpResponse);

        EasyMock.replay(httpRequest, parser, servletContext, requestDispatcher);
        
        assertEquals("aaa.css", RequestMapper.getRequestContextPath(httpRequest));
        
        // AccessPoint#dispatchLocal() returns immediately if serveStatis is false.
        cut.setServeStatic(true);
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        
        EasyMock.verify(parser, requestDispatcher);
        
        assertTrue("handleRequest return value", r);
        
    }

    
    public void testMemento() throws Exception {
        cut.setEnableMemento(true);
        // expectations:
        // - memento-headers are set.
        // - do-not-negotiate header is set.
        // - orig-header is set (for redirect)
        // - printTimemapResponse()
        
        // TODO
    }
}
