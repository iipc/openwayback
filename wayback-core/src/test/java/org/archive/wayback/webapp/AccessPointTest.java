/**
 * 
 */
package org.archive.wayback.webapp;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.format.ArchiveFileConstants;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
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
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.memento.MementoUtils;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;
import org.archive.wayback.util.url.KeyMakerUrlCanonicalizer;
import org.archive.wayback.util.webapp.RequestMapper;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;

/**
 * unit test for {@link AccessPoint}.
 * 
 * TODO: this unit test is too complex. it is because
 * AccessPoint class has too much responsibility and many execution paths.
 * some good refactoring of AccessPoint class would help.
 * 
 * @author Kenji Nagahashi
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
    
    /**
     * setup HttpServletRequest stubs
     * @param contextPath
     */
    protected void setupRequestStub(String contextPath, String uri, String contextPathPrefix) {
        EasyMock.expect(httpRequest.getRequestURI()).andStubReturn(uri);
        EasyMock.expect(httpRequest.getRequestURL()).andStubReturn(new StringBuffer(uri));
        //EasyMock.expect(httpRequest.getQueryString()).andReturn(null);
        // remote address test
        //EasyMock.expect(httpRequest.getHeader("X-Forwarded-For")).andReturn(null);
        // Ajax mode test
        //EasyMock.expect(httpRequest.getHeader("X-Requested-With")).andReturn("XMLHttpRequest");

        // used by RequestMapper#getRequestPathPrefix(HttpServletRequest)
        // typical value found in ia-wayback-projects/projects/global-wayback/configs/local/wayback.properties
        // TODO: RequestMapper#getRequestContextPath(HttpServletRequest) assumes value of this
        // attribute ends with "/". RequestMapper has constant declaration for 
        // "webapp-request-context-path-prefix", but it's private.
        EasyMock.expect(
                httpRequest.getAttribute("webapp-request-context-path-prefix"))
                .andStubReturn(contextPathPrefix);
        
        EasyMock.expect(httpRequest.getLocalName()).andStubReturn("localhost");
        // commented out because these are default behavior for stub ("Nice") mock.
        //EasyMock.expect(httpRequest.getAuthType()).andReturn(null).anyTimes();
        //EasyMock.expect(httpRequest.getRemoteUser()).andReturn(null).anyTimes();
        //EasyMock.expect(httpRequest.getHeader(WaybackRequest.REQUEST_AUTHORIZATION)).andReturn(null);
        EasyMock.expect(httpRequest.getLocalPort()).andStubReturn(8080);
        EasyMock.expect(httpRequest.getContextPath()).andStubReturn("/static");
        EasyMock.expect(httpRequest.getLocale()).andStubReturn(Locale.CANADA_FRENCH);
        EasyMock.expect(httpRequest.getRequestDispatcher(EasyMock.<String>notNull())).andStubReturn(requestDispatcher);
        //EasyMock.expect(httpRequest.getCookies()).andReturn(null).anyTimes();
    }
    
    // values used in global wayback configuration.
    public static final String WEB_PREFIX = "/web/";
    public static final String STATIC_PREFIX = "/static/";
    
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
        cut.setReplayPrefix(WEB_PREFIX);
        cut.setQueryPrefix(WEB_PREFIX);
        cut.setStaticPrefix(STATIC_PREFIX);
        
        KeyMakerUrlCanonicalizer canonicalizer = new KeyMakerUrlCanonicalizer();
        cut.setSelfRedirectCanonicalizer(canonicalizer);

        resourceStore = EasyMock.createMock(ResourceStore.class);
        resourceIndex = EasyMock.createMock(ResourceIndex.class);
        collection = new WaybackCollection();
        collection.setResourceIndex(resourceIndex);
        collection.setResourceStore(resourceStore);
        cut.setCollection(collection);
        
        // behavior returning null are commented out because EasyMock provides them by default.
        httpRequest = EasyMock.createNiceMock(HttpServletRequest.class);
        httpResponse = EasyMock.createNiceMock(HttpServletResponse.class);
        // RequestDispatcher - setup expectations, call replay() and verify() if
        // method calls are expected.
        requestDispatcher = EasyMock.createMock(RequestDispatcher.class);
        // Memento mode - only called when enableMemento==true.
        //EasyMock.expect(httpRequest.getHeader(MementoUtils.ACCEPT_DATETIME)).andReturn(null);
        setupRequestStub("/", "/", null);
        
        // as we mock-ify RequestParser, WaybackRequest can be independent of httpRequest.
        // it suggests HttpServletRequest method calls in setupRequestStub are better be made through 
        // RequestParser (TODO)
        //wbRequest = new WaybackRequest();
        parser = EasyMock.createMock(RequestParser.class);
        cut.setParser(parser);
        EasyMock.expect(parser.parse(httpRequest, cut)).andAnswer(new IAnswer<WaybackRequest>() {
        	@Override
        	public WaybackRequest answer() throws Throwable {
            	return wbRequest;
        	}
		});
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

		// disable logging
        Logger.getLogger(ArchivalUrlResultURIConverter.class.getName()).setLevel(Level.WARNING);
        Logger.getLogger(PerfStats.class.getName()).setLevel(Level.WARNING);
    }
    
    public static Resource createTestHtmlResource(String uri, String timestamp, byte[] payloadBytes) throws IOException {
        TestWARCRecordInfo recinfo = TestWARCRecordInfo.createCompressedHttpResponse("text/html", payloadBytes);
        recinfo.setCreate14DigitDateFromDT14(timestamp);
        if (uri != null) recinfo.setUrl(uri);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        WarcResource resource = new WarcResource(rec, ar);
        resource.parseHeaders();
        return resource;
    }
    public static Resource createTestHtmlResource(String timestamp, byte[] payloadBytes) throws IOException {
        // by passing null to uri, default "http://test.example.com/" will be used.
        return createTestHtmlResource(null, timestamp, payloadBytes);
    }
    public static Resource createTestRevisitResource(String timestamp, int len, boolean withHeader) throws IOException {
        TestWARCRecordInfo recinfo = TestWARCRecordInfo.createRevisitHttpResponse("text/html", len, withHeader);
        recinfo.setCreate14DigitDateFromDT14(timestamp);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        WarcResource resource = new WarcResource(rec, ar);
        resource.parseHeaders();
        return resource;
    }
	public static Resource createTestRevisitResource(String timestamp, Resource revisited,
			boolean withHeader) throws IOException {
		String clen = revisited.getHttpHeaders().get("Content-Length");
		int len = clen != null ? Integer.parseInt(clen) : -1;
		TestWARCRecordInfo recinfo = TestWARCRecordInfo.createRevisitHttpResponse("text/html", len, withHeader);
		recinfo.setCreate14DigitDateFromDT14(timestamp);
		recinfo.addExtraHeader("WARC-Refers-To-Target-URI", ((WarcResource)revisited).getWarcHeaders().getUrl());
		recinfo.addExtraHeader("WARC-Refers-To-Date", ((WarcResource)revisited).getWarcHeaders().getDate());
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		WarcResource resource = new WarcResource(rec, ar);
		resource.parseHeaders();
		return resource;
	}
    
    /**
     * checks if {@code ts} has expected format (YYYYmmddHHMMSS)
     * @param ts timestamp string to check
     * @return true if ok, false otherwise
     */
    protected static boolean validTimestamp(String ts) {
        return ts != null && Pattern.matches("\\d{14}", ts);
    }
    /**
     * Transform input date to 14-digit timestamp:
     * 2007-08-29T18:00:26Z => 20070829180026
     * (stolen from WARCRecordToSearchResultAdapter - move that method to ArchiveUtils!)
     * @param input date text in ISOZ format.
     * @return date text in DT14 format. 
     */
    private static String transformWARCDate(final String input) {
        StringBuilder output = new StringBuilder(14);
        output.append(input.substring(0,4));
        output.append(input.substring(5,7));
        output.append(input.substring(8,10));
        output.append(input.substring(11,13));
        output.append(input.substring(14,16));
        output.append(input.substring(17,19));
        return output.toString();
    }
    
    /**
     * given a sequence of {@link WarcResource}s,
     * <ul>
     * <li>build CaptureSearchResults, filled with CaptureSearchResult instances, which
     * have auto-generated unique filename and offset of 0. this is necessary for equality
     * (see {@link CaptureSearchResultMatcher}).</li>
     * <li>setup ResourceStore mock to return Resource for each CaptureSearchResult.<li>
     * <li>setup ResourceIndex mock to return CaptureSearchResults.</li>
     * <li>if {@code closestIndex} {@code >= 0}, set corresponding CaptureSearchResult's
     * closest flag, and also set up ReplayDispatcher.getClosest() mock to return it.</li>
     * </ul>
     * <p>Note: {@link #wbRequest} must be set up before calling this method,
     * or {@code ResourceIndex} will not return expected search result set.</p>
     * @param closestIndex 0-based index of resource to be marked as <i>closest</i>
     * @param resources sequence of WarcResources
     * @return CaptureSearchResults built
     */
    protected CaptureSearchResults setupCaptures(int closestIndex, Resource... resources) throws Exception {
        CaptureSearchResults results = new CaptureSearchResults();
        CaptureSearchResult prev = null;
        for (Resource res : resources) {
            CaptureSearchResult result = new CaptureSearchResult();
            if (prev != null) {
            	prev.setNextResult(result);
            	result.setPrevResult(prev);
            }
            // TODO: Resource should have methods for accessing URI and date
            if (res instanceof WarcResource) {
                // TODO: want to use WARCRecordToSearchResultAdapter? WarcResource
                // has no method to retrieve underlining WARCRecord.
                ArchiveRecordHeader h = ((WarcResource)res).getWarcHeaders();
                String originalUrl = h.getUrl();
                String ts = (String)h.getHeaderValue("WARC-Date");
                // WARC-Date is in ISOZ format.
                ts = transformWARCDate(ts);
                result.setOriginalUrl(originalUrl);
                result.setCaptureTimestamp(ts);
                result.setOffset(0);
                // this is (W)ARC file name in real practice. here we use
                // DT14 timestamp as pseudo filename (.warc.gz suffix is not
                // essential).
                result.setFile(ts + ".warc.gz");
            } else if (res instanceof ArcResource) {
                // TODO: should use ARCRecordToSearchResultAdapter? ArcResource has
                // getArcRecord() methods whose result may be cast to ARCRecord.
                // NB: ArcResource#getARCMetadata() creates a new Map object.
                Map<String, String> meta = ((ArcResource)res).getARCMetadata();
                String originalUrl = meta.get(ArchiveFileConstants.URL_FIELD_KEY);
                String ts = meta.get(ArchiveFileConstants.DATE_FIELD_KEY);
                result.setOriginalUrl(originalUrl);
                result.setCaptureTimestamp(ts);
            } else {
                throw new AssertionError("unexpected Resource type: " + res.getClass());
            }
            result.setHttpCode(Integer.toString(res.getStatusCode()));
            // CaptureSearchResultMatcher fails without this, but actual value does not
            // matter. so set it to 0.
            result.setOffset(0);
            assertTrue("invalid timestamp " + result.getCaptureTimestamp(),
                    validTimestamp(result.getCaptureTimestamp()));
            if (closestIndex == 0) {
                result.setClosest(true);
                results.setClosest(result);
                EasyMock.expect(replay.getClosest(wbRequest, results)).andReturn(result);
            }
            
            // Note AccessPoint passes a copy of CaptureSearchResult in some case (ex. Replay_Revisit() test).
            // so we need to use custom argument matcher.
            EasyMock.expect(resourceStore.retrieveResource(eqCaptureSearchResult(result))).andReturn(res).anyTimes();
            
            results.addSearchResult(result);
            --closestIndex;
        }
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        
        return results;
    }
    
    // REFACTORING THOUGHTS: WaybackRequest.setReplayRequest() could take requestUrl and replayTimestamp
    // it is semantically more clear.
    /**
     * create new WaybackRequest set up as replay request for {@code requestUrl} at {@code replayTimestamp}.
     * created object is set to #wbRequest.
     * @param wbRequest
     * @param requestUrl
     * @param replayTimestamp
     */
    public void setReplayRequest(String requestUrl, String replayTimestamp) {
    	wbRequest = new WaybackRequest();
        wbRequest.setReplayRequest();
        wbRequest.setRequestUrl(requestUrl);
        wbRequest.setReplayTimestamp(replayTimestamp);
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
        setReplayRequest("http://www.example.com/", "20100601123456");
        
//        // TODO: originalUrl can be different from wbRequst.requestUrl, and it will be
//        // reflected to redirect URL (worth testing).
//        CaptureSearchResults results = createCaptureSearchResults(
//                "20100601000000", "http://www.example.com/", "200");
//        CaptureSearchResult closest = results.getClosest();
//        // TODO: this can be different from wbRequst.requestUrl, and it will be reflected
//        // to redirect URL.
//        closest.setOriginalUrl("http://www.example.com/");
//        closest.setHttpCode("200");
//        // closest.captureTimestamp != wbRequest.replayTimestamp
//        closest.setCaptureTimestamp("20100601000000");
        // Resource below has originalUrl="http://test.example.com/", which is different from
        // wbRequest.requestUrl above. originalUrl shall be reflected to resultant redirect URL.
        CaptureSearchResults results = setupCaptures(
                0,
                createTestHtmlResource("20100601000000", "hogheogehoge\n".getBytes("UTF-8"))
                );
        // handleRequest()
        // calls handleReplay()
        // - calls checkInterstitialRedirect()
        // - calls selfRedirectCanonicalizer.urlStringToKey(requestURL) if non-null
        // - calls queryIndex(), which calls collection.resourceIndex.query(wbRequest)
        //     which in turn returns results above (setup in setupCaptures(...))
        // - calls replay.getClosest()
        // - calls checkAnchorWindow()
        // - calls getResource(closest, skipFiles), which
        //   - first checks if closest is in skipFiles (and throws
        //     ResourceNotAvailableException if it is),
        //   - then calls collection.resourceStore.retrieveResource(closest),
        //     which returns Resource above.
        
        // when closest's timestamp is different from replay requests's timestamp, it redirects
        // to closest's timestamp.
        httpResponse.setStatus(302);
        httpResponse.setHeader("Location", "/web/20100601000000/http://test.example.com/");
        
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
        setReplayRequest("http://test.example.com/", "20100601000000");
        
        // there's capture with timestamp exactly requested for.
        Resource payloadResource = createTestHtmlResource("20100601000000", "hogheogehoge\n".getBytes("UTF-8"));
        CaptureSearchResults results = setupCaptures(
                0,
                payloadResource
                );
        CaptureSearchResult closest = results.getClosest();
        
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
            // CaptureSearchResult is compared by file name and offset. this is how
            // AccessPoint#retrievePayloadForIdenticalContentRevisit(...) retrieves previous capture.
            // TODO: this could be defined as CaptureSearchResult#equals(Object).
            if (!(actual instanceof CaptureSearchResult)) return false;
            String file = ((CaptureSearchResult)actual).getFile();
            long offset =  ((CaptureSearchResult)actual).getOffset();
            if (expected.getOffset() != offset) return false;
            return file == null ? expected.getFile() == null : file.equals(expected.getFile());
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
    
    public static class CaptureSearchMatcher implements IArgumentMatcher {
    	private String url;
    	private String replayTimestamp;
    	public CaptureSearchMatcher(String url, String replayTimestamp) {
    		this.url = url;
    		this.replayTimestamp = replayTimestamp;
    	}
    	@Override
    	public boolean matches(Object actual) {
    		if (!(actual instanceof WaybackRequest)) return false;
    		WaybackRequest wbRequest = (WaybackRequest)actual;
    		String replayTimestamp = wbRequest.getReplayTimestamp();
    		String url = wbRequest.getRequestUrl();
    		if (url == null || replayTimestamp == null) return false;
    		if (this.url == null || this.replayTimestamp == null) return false;
    		// Only exact match is supported. i.e. http://example.com/ and http://example.com
    		// are different even though they typically get canonicalized into the same string.
    		// Also not checking if wbRequest is in fact a capture search request.
    		return this.url.equals(url) && this.replayTimestamp.equals(replayTimestamp);
    	}
    	@Override
    	public void appendTo(StringBuffer buffer) {
    		buffer.append("eqCaptureSearchRequest(");
    		buffer.append(url).append(",").append(replayTimestamp);
    		buffer.append(")");
    	}
    }
	public static WaybackRequest eqCaptureSearchRequest(String url,
			String replayTimestamp) {
		EasyMock.reportMatcher(new CaptureSearchMatcher(url, replayTimestamp));
		return null;
	}

    /**
     * test of revisit. 
     * closest capture is a revisit. 
     * @throws Exception
     */
    public void testHandleRequest_Replay_Revisit() throws Exception {
        setReplayRequest("http://www.example.com/", "20100601000000");
        // closest SearchResult has isDuplicateDigest() == true.
        byte[] payload = "hogehogehogehoge\n".getBytes("UTF-8");
        Resource payloadResource = createTestHtmlResource("20100501000001", payload);
        Resource headerResource = createTestRevisitResource("20100601000000", payload.length, true);
        CaptureSearchResults results = setupCaptures(1, payloadResource, headerResource);

        CaptureSearchResult previous = results.getResults().get(0);
        CaptureSearchResult closest = results.getClosest();
//        previous.setFile("aaa.warc.gz");
//        previous.setOffset(0);
        closest.flagDuplicateDigest(previous); // right? TODO: could be done in setupCaptures()
        assertTrue(closest.isDuplicateDigest());
        assertTrue(closest.getDuplicatePayloadFile() != null);
        assertTrue(closest.getDuplicatePayloadOffset() != null);

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

    public static Resource createSelfRedirectResource(String url, String timestamp) throws IOException {
    	assert !url.endsWith("/");
    	// typical redirect: http://example.com to http://example.com/
    	String location = url + "/";
		TestWARCRecordInfo recinfo = new TestWARCRecordInfo(
			TestWARCRecordInfo.buildHttpRedirectResponseBlock("301 Moved Permanently", location));
		recinfo.setUrl(url);
		recinfo.setCreate14DigitDateFromDT14(timestamp);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        WarcResource resource = new WarcResource(rec, ar);
        resource.parseHeaders();
        return resource;
    }

	/**
	 * {@code handleReplay()} is supposed to throw {@code ResourceNotAvailableException}
	 * when it cannot find a replay-able capture for a request.
	 * This is a test for one of such "capture not found" case: revisited re
	 * This is rather a corner case. handlReplay() is supposed to throw
	 * ResourceNotAvailableException when it can not find
	 * @throws Exception
	 */
    public void testHandleRequest_MissingRevisitPayload() throws Exception {
    	setReplayRequest("http://example.com", "20140619004054");
    	// resource revisited, but missing in capture search result
    	Resource revisited = createSelfRedirectResource("http://example.com", "20140619015411");
        CaptureSearchResults results = setupCaptures(
            0,
            createSelfRedirectResource("http://example.com", "20140619004054"),
            createTestRevisitResource("20140619016511", revisited, true)
            );
        CaptureSearchResult revisit = results.getResults().get(1);
        revisit.flagDuplicateDigest(); // revisit, but original is not found.

        // expectation:
        // 1. first capture is skipped because it is self-redirect. selects the second.
        // 2. second capture is a revisit, calls resourceIndex.query() for the revisited,
        //    but original capture is not found in the result.
        // 3. ResourceNotAvailableException is thrown
        // 4. exception captured, skip to the next capture and finds none.
        // 5. ResourceNotAvailableException is thrown out of handleReplay
        // 6. ExceptionRenderer.renderException is called (in handleQuery)

        // XXX setting these up manually feels very fragile - perhaps we need a
        // test ResourceIndex + ReplayDispatcher.
		EasyMock.expect(
			resourceIndex.query(eqCaptureSearchRequest("http://example.com",
				"20140619015411"))).andReturn(results);
		EasyMock.expect(
			replay.getClosest(
				eqCaptureSearchRequest("http://example.com", "20140619015411"),
				EasyMock.same(results))).andReturn(results.getResults().get(0));

        // for this test, it is easier to test handleReplay, not handleQuery

        EasyMock.replay(httpRequest, httpResponse, resourceIndex, resourceStore, replay);

        cut.init();
        try {
        	cut.handleReplay(wbRequest, httpRequest, httpResponse);
        	fail("handleReplay did not throw ResourceNotAvailableException");
        } catch (ResourceNotAvailableException ex) {
        	// expected.
        }

        EasyMock.verify(resourceIndex, resourceStore, replay);
    }
    
    /**
     * old-style WARC revisit (no HTTP status line and header, 
     * Content-Length in WARC header is zero).
     * it shall replay HTTP status line, headers and content from previous matching capture.
     * @throws Exception
     */
    public void testHandleRequest_Replay_OldWARCRevisit() throws Exception {
        // TODO - it'd be better to define an interface in Resource class so that AccessPoint
        // needs not to have separate execution path for this. that's easier to test.
    }
    
    /**
     * old-style ARC revisit (no mimetype, filename is '-')
     * @throws Exception
     */
    public void testHandleRequest_Replay_OldARCRevisit() throws Exception {
        // ditto - see TODO comment above.
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
        // request timestamp is different from 'previous' below. it makes handleRequest
        // return redirect. in this case, Resource for 'previous' will not be retrieved.
        setReplayRequest("http://test.example.com/style.css", "20100601000000");
        // if closest is not HTTP-success, 
        // to have isAnyEmbeddedContext() return true - any of cSSContext, iMGContext, jSContext
        // frameWrapperContext, iFrameWrapperContext, objectEmbedContext has the same effect.
        wbRequest.setCSSContext(true);
        assertTrue(wbRequest.isAnyEmbeddedContext());
        
        CaptureSearchResults results = setupCaptures(
                1,
                createTestHtmlResource("http://test.example.com/style.css", 
                        "20100501000000", "hogheogehoge\n".getBytes("UTF-8")),
                createTest502Resource()
                );
        CaptureSearchResult closest = results.getClosest();
        assertTrue(closest.isHttpError());
        
        // or wbRequest.setBestLatestReplayRequest();
        final String expectedRedirectURI = "/web/20100501000000cs_/http://test.example.com/style.css";
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
    
    // REFACTORING THOUGHTS: WaybackRequet.setUrlCaptureQueryRequest() could take requestUrl and replayTimestamp.
    /**
     * create new WaybackRequest set up as capture query request for URL {@code requestUrl}, at time
     * {@ode replayTimestamp}.
     * @param requestUrl
     * @param replayTimestamp
     */
    public final void setCaptureQueryRequest(String requestUrl, String replayTimestamp) {
    	wbRequest = new WaybackRequest();
        wbRequest.setCaptureQueryRequest();
        wbRequest.setRequestUrl(requestUrl);
        wbRequest.setReplayTimestamp(replayTimestamp);
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
        setCaptureQueryRequest("http://www.example.com/", "20100601123456");

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
    
    // REFACTORING THOUGHTS: WaybackRequet.setUrlQueryRequest() could take requestUrl and replayTimestamp.
    /**
     * create new WaybackRequest set up as URL query request for URL {@code requestUrl}
     * at time {@code replayTimestamp}.
     * created object is set to {@link #wbRequest}.
     * @param requestUrl
     * @param replayTimestamp
     */
    public void setUrlQueryRequest(String requestUrl, String replayTimestamp) {
    	wbRequest = new WaybackRequest();
        wbRequest.setUrlQueryRequest();
        wbRequest.setRequestUrl(requestUrl);
        wbRequest.setReplayTimestamp(replayTimestamp);
    }
    
    public void testHandleRequest_UrlSearchResults() throws Exception {
        setUrlQueryRequest("http://www.example.com/", "20100601123456");

        // AccessPoint is not concerned of the details of UrlSearchResults. it just
        // forwards the request to QueryRenderer. so we leave it uninitialized here.
        UrlSearchResults results = new UrlSearchResults();
        EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
        
        // EXPECTATION: AccessPoint.handleQuery() calls query.renderUrlResults().
        query.renderUrlResults(httpRequest, httpResponse, wbRequest, results, cut.getUriConverter());
        
        EasyMock.replay(httpRequest, httpResponse, query, resourceIndex);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        
        EasyMock.verify(query, resourceIndex);
        assertTrue("handleRequest return value", r);
    }

    /**
     * setup expected call to {@link ResourceIndex#query(WaybackRequest)},
     * returning empty {@link UrlSearchResults}.
     * <p>This is sufficient for most cases, as AccessPoint is not concerned
     * with UrlSearchResult, but simply passes it to query renderer.</p>
     * <p>Note: set up {@link #wbRequest} before calling this method.</p>
     * @throws Exception declared, but will never be thrown
     */
    protected void expectUrlIndexQuery() throws Exception {
    	UrlSearchResults results = new UrlSearchResults();
    	EasyMock.expect(resourceIndex.query(wbRequest)).andReturn(results);
		query.renderUrlResults(httpRequest, httpResponse, wbRequest, results, cut.getUriConverter());
    }

    // tests for collapseTime parameter. for query requests (both capture and URL),
    // AccessPoint passes its collapseTime parameter to ResourceIndex#query via
    // WaybackRequest.collapseTime. for replay request, it doesn't.

	public void testHandleRequest_queryCollapseTimeUnspecified()
			throws Exception {
		cut.setQueryCollapseTime(-1);
		setUrlQueryRequest("http://www.example.com/", "20100601123456");

		expectUrlIndexQuery();

		EasyMock.replay(httpRequest, httpResponse, query, resourceIndex);

		cut.init();
		boolean handled = cut.handleRequest(httpRequest, httpResponse);

		EasyMock.verify(query, resourceIndex);
		assertTrue("handleRequest return value", handled);

		assertEquals(-1, wbRequest.getCollapseTime());
	}

	public void testHandleRequest_queryCollapseTimeSpecified() throws Exception {
		cut.setQueryCollapseTime(10);
		setUrlQueryRequest("http://www.example.com/", "20100601123456");

		expectUrlIndexQuery();

		EasyMock.replay(httpRequest, httpResponse, query, resourceIndex);

		cut.init();
		boolean handled = cut.handleRequest(httpRequest, httpResponse);

		EasyMock.verify(query, resourceIndex);
		assertTrue("handleRequest return value", handled);

		assertEquals(10, wbRequest.getCollapseTime());
	}

	public void testHandlerRequest_queryCollapseTimeForReplayQuery()
			throws Exception {
		cut.setQueryCollapseTime(10);
		// query parameters and CaptureSeachResults details are irrelevant to
// this test.
		setReplayRequest("http://www.example.com/", "20100601123456");
		setupCaptures(
			0,
			createTestHtmlResource("20100601000000",
				"hogheogehoge\n".getBytes("UTF-8")));
		EasyMock.replay(httpRequest, httpResponse, query, resourceIndex,
			resourceStore, replay);

		cut.init();
		boolean handled = cut.handleRequest(httpRequest, httpResponse);

		EasyMock.verify(query, resourceIndex);
		assertTrue("handleRequest return value", handled);

		assertEquals(-1, wbRequest.getCollapseTime());

	}

	/**
     * if bounceToReplayPrefix is true, replay request is redirected to
     * other access point.
     * @throws Exception
     */
    public void testBounceToReplayPrefix() throws Exception {
        final String URL = "http://www.example.com/";
        final String TIMESTAMP = "20100601123456";
        
        setReplayRequest(URL, TIMESTAMP);

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
        // first reset the mock for overriding getAttribute(), getRequestURI(), and getRequestURL()
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
        
        // reconfigure RequestParser to return null, which signifies that
        // there's no dynamic handler and the request shall be mapped to local static resource.
        // (AccessPoint#dispatchLocal(HttpServletRequest))
        EasyMock.reset(parser);
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
    
    // *** Memento Tests ***
    
    // REFACTORING THOUGHTS: Memento annotations (adding response headers) could be implemented
    // as decorator of replay renderer. because of this possibility, I separated out tests for
    // memento headers here.

    public void testMemento_replay_exactCapture() throws Exception {
        final String AGGREGATION_PREFIX = "http://web.archive.org";
        
        cut.setEnableMemento(true);
        cut.setConfigs(new Properties());
        cut.getConfigs().setProperty(MementoUtils.AGGREGATION_PREFIX_CONFIG, AGGREGATION_PREFIX);
        
        // make sure wbRequesat.requestUrl, replayTimestamp are set up.
        setReplayRequest("http://www.example.com/", "20100601000000");
        assertFalse(wbRequest.isMementoTimegate());
        Resource payloadResource = createTestHtmlResource("20100601000000", "hogehogehogehoge\n".getBytes("UTF-8"));
        CaptureSearchResults results = setupCaptures(
                0,
                payloadResource
                );
        CaptureSearchResult closest = results.getClosest();
        
        // when closest's timestamp == request's timestamp,
        // it gets ReplayRenderer with replay.getRenderer(wbRequest, closest, httpHeaderResource, payloadResource),
        // and calls renderResource() on it.
        EasyMock.expect(replay.getRenderer(wbRequest, closest, payloadResource, payloadResource)).andReturn(replayRenderer);
        // calls replayRenderer.renderResource(...)
        replayRenderer.renderResource(httpRequest, httpResponse, wbRequest,
                closest, payloadResource, payloadResource, cut.getUriConverter(),
                results);
        
        // key expectations of this test
        // called through MementoUtils.addMementoHeaders(...)
        final String expectedMementoDateTime = "Tue, 01 Jun 2010 00:00:00 GMT";
        httpResponse.setHeader(MementoUtils.MEMENTO_DATETIME, expectedMementoDateTime);
        // MementoUtils.generateMementoLinkHeaders(...)
        // TODO: actually it is acceptable to have various rels in different order.
        final String expectedMementoLink = String.format(
                "<%1$s>; rel=\"original\", " +
                "<%2$s%3$stimemap/link/%1$s>; rel=\"timemap\"; type=\"application/link-format\", " +
                "<%2$s%3$s%1$s>; rel=\"timegate\", " +
                "<%2$s%3$s%4$s/%1$s>; rel=\"first last memento\"; datetime=\"%5$s\"",
                "http://www.example.com/", AGGREGATION_PREFIX, WEB_PREFIX, "20100601000000", expectedMementoDateTime);
        httpResponse.setHeader(MementoUtils.LINK, expectedMementoLink);
        
        EasyMock.replay(httpRequest, httpResponse, resourceIndex, resourceStore, replay);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);

        EasyMock.verify(resourceIndex, resourceStore, replay);
        
        assertTrue("handleRequest return value", r);
    }

    public void testMemento_replay_nearbyCapture() throws Exception {
        cut.setEnableMemento(true);
        // make sure wbRequesat.requestUrl, replayTimestamp are set up.
        setReplayRequest("http://www.example.com/", "20100601123456");
        assertFalse(wbRequest.isMementoTimegate());
        
        Resource payloadResource = createTestHtmlResource("20100601000000", "hogehogehogehoge\n".getBytes("UTF-8"));
        CaptureSearchResults results = setupCaptures(0, payloadResource);
        // handleRequest()
        // calls handleReplay()
        // - calls checkInterstitialRedirect()
        // - calls selfRedirectCanonicalizer.urlStringToKey(requestURL) if non-null
        // - calls queryIndex(), which calls collection.resourceIndex.query(wbRequest)
        
        // redirects to URL for closest capture.
        // also has Link header.
        httpResponse.setHeader("Link", String.format("<%s>; rel=\"original\"", "http://test.example.com/"));
        httpResponse.setStatus(302);
        httpResponse.setHeader("Location", "/web/20100601000000/http://test.example.com/");
        
        EasyMock.replay(httpRequest, httpResponse, resourceIndex, resourceStore, replay);
        
        cut.init();
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        
        EasyMock.verify(httpResponse, resourceIndex, resourceStore, replay);
        
        assertTrue("handleRequest return value", r);
    }

    /**
     * Timemap == Memento rendering of capture query (CaptureSearchResult).
     */
    public void testMementoTimemap() throws Exception {
        cut.setEnableMemento(true);
        setCaptureQueryRequest("http://www.example.com/", "20100601000000");
        wbRequest.setMementoTimegate();
        
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
    
    /**
     * test for local static resource when enableMemento=true.
     * expectations:
     * <ul>
     * <li>do-not-negotiate Link header is set.</ul>
     * </ul>
     * @throws Exception
     */
    public void testMemento_dispatchLocal() throws Exception {
        cut.setEnableMemento(true);
        cut.setServeStatic(true);

        // first reset the mock for overriding getAttribute(), getRequestURI(), and getRequestURL()
        EasyMock.reset(httpRequest);
        setupRequestStub("/static", "/static/aaa.css", "/static/");

        EasyMock.expect(
                httpRequest.getAttribute("webapp-request-context-path-prefix"))
                .andStubReturn("/static/");
        
        // reconfigure RequestParser to return null, which signifies that
        // there's no dynamic handler and the request shall be mapped to local static resource.
        // (AccessPoint#dispatchLocal(HttpServletRequest))
        EasyMock.reset(parser);
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
        
        // key expectation in this test
        httpResponse.setHeader(MementoUtils.LINK, "<http://mementoweb.org/terms/donotnegotiate>; rel=\"type\"");

        EasyMock.replay(httpRequest, parser, servletContext, requestDispatcher);
        
        cut.init();
        
        boolean r = cut.handleRequest(httpRequest, httpResponse);
        
        EasyMock.verify(parser, requestDispatcher);

        assertTrue("handleRequest return value", r);
        
    }
    
}
