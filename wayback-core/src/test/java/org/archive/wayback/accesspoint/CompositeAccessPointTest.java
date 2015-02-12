/**
 *
 */
package org.archive.wayback.accesspoint;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.accesscontrol.CollectionContext;
import org.archive.wayback.accesscontrol.ContextExclusionFilterFactory;
import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
import org.archive.wayback.archivalurl.ArchivalUrlReplayURIConverter;
import org.archive.wayback.archivalurl.ArchivalUrlResultURIConverterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.url.KeyMakerUrlCanonicalizer;
import org.archive.wayback.util.webapp.RequestMapper;
import org.archive.wayback.webapp.AccessPointTest;
import org.archive.wayback.webapp.WaybackCollection;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.LogicalOperator;

/**
 * Test for {@link CompositeAccessPoint}.
 */
public class CompositeAccessPointTest extends TestCase {

	CompositeAccessPoint cut;
	AccessPointConfigs configs;

	WaybackCollection collection;
	ResourceStore resourceStore;
	ResourceIndex resourceIndex;

	RequestDispatcher requestDispatcher;

	//RequestParser parser;
	// ResultURIConverter uriConverter;
	QueryRenderer query;
	ReplayDispatcher replay;

	ReplayRenderer replayRenderer;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		cut = new CompositeAccessPoint();
		// this is passed to ContextResultURIConverterFactory by
		// AccessPointAdapter#getUriConverter()
		cut.setReplayPrefix("/");
		cut.setEnableMemento(false);

		configs = new AccessPointConfigs();
		AccessPointConfig config1 = new AccessPointConfig();
		config1.setBeanName("1001");
		AccessPointConfig config2 = new AccessPointConfig();
		config2.setBeanName("2604");

		HashMap<String, AccessPointConfig> configsMap = new HashMap<String, AccessPointConfig>();
		configsMap.put(config1.getBeanName(), config1);
		configsMap.put(config2.getBeanName(), config2);
		configs.setAccessPointConfigs(configsMap);
		cut.setAccessPointConfigs(configs);

		// XXX following lines are copied from AccessPointTest#setUp()
		// share the code.
		KeyMakerUrlCanonicalizer canonicalizer = new KeyMakerUrlCanonicalizer();
		cut.setSelfRedirectCanonicalizer(canonicalizer);

		resourceStore = EasyMock.createMock(ResourceStore.class);
		resourceIndex = EasyMock.createMock(ResourceIndex.class);
		collection = new WaybackCollection();
		collection.setResourceIndex(resourceIndex);
		collection.setResourceStore(resourceStore);
		cut.setCollection(collection);

		// RequestDispatcher - setup expectations, call replay() and verify() if
		// method calls are expected.
		requestDispatcher = EasyMock.createMock(RequestDispatcher.class);
		// Memento mode - only called when enableMemento==true.
		// EasyMock.expect(httpRequest.getHeader(MementoUtils.ACCEPT_DATETIME)).andReturn(null);

		ArchivalUrlRequestParser parser = new ArchivalUrlRequestParser();
		parser.init();
		cut.setParser(parser);

		query = EasyMock.createMock(QueryRenderer.class);
		cut.setQuery(query);

		replay = EasyMock.createMock(ReplayDispatcher.class);
		cut.setReplay(replay);

		replayRenderer = EasyMock.createMock(ReplayRenderer.class);

		// This ResultURIConverter shall be ignored.
		{
			ArchivalUrlReplayURIConverter uc = new ArchivalUrlReplayURIConverter();
			uc.setReplayURIPrefix("/web/");
			cut.setUriConverter(uc);
		}
		// AccessPointAdapter shall use this ContextResultURIConverterFactory for
		// creating per-AccessPoint ResultURIConverter
		{
			ContextResultURIConverterFactory uriConverterFactory = new ArchivalUrlResultURIConverterFactory();
			cut.setUriConverterFactory(uriConverterFactory);
		}
	}

	static final Comparator<CollectionContext> collectionContextComp = new Comparator<CollectionContext>() {
		public int compare(CollectionContext o1, CollectionContext o2) {
			return o1.getCollectionContextName().compareTo(o2.getCollectionContextName());
		};
	};

	/**
	 * Test of {@code handleRequest()}.
	 * Only thing we test here is {@code createExclusionFilter()}.
	 * @throws Exception
	 */
	public void testHandleRequest() throws Exception {
		HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
		EasyMock.expect(request.getAttribute(RequestMapper.REQUEST_CONTEXT_PREFIX)).andStubReturn(null);
		// TODO: requestURI can start with "/"
		EasyMock.expect(request.getRequestURI()).andStubReturn("2604/2/http://example.com/");
		EasyMock.expect(request.getLocale()).andStubReturn(Locale.US);

		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);

		// Wow, this is way too complex - something must be wrong with the design.

		// Looks comples, but the point is to test if collection name is passed
		// to ContextExclusionFilterFactory.getExclusionFilter().
		CollectionContext expectedContext = new CollectionContext() {
			@Override
			public String getCollectionContextName() {
				return "2604";
			}
		};
		ContextExclusionFilterFactory exclusionFactory = EasyMock.createMock(ContextExclusionFilterFactory.class);
		// currently this is invoked twice for a replay request - should be just once.
		EasyMock.expect(
			exclusionFactory.getExclusionFilter(EasyMock.cmp(expectedContext,
				collectionContextComp, LogicalOperator.EQUAL))).andReturn(
			new ExclusionFilter() {
				@Override
				public int filterObject(CaptureSearchResult o) {
					return FILTER_INCLUDE;
				}
			}).atLeastOnce();

		cut.setExclusionFactory(exclusionFactory);

		Resource resource = AccessPointTest.createTestHtmlResource("http://example.com/", "20140101000000", "<html></html>".getBytes());
		final CaptureSearchResults results = AccessPointTest.setupCaptures(resourceIndex, resourceStore, 0, resource);

		EasyMock
			.expect(resourceIndex.query(EasyMock.<WaybackRequest>notNull()))
			.andAnswer(new IAnswer<SearchResults>() {
				@Override
				public SearchResults answer() throws Throwable {
					WaybackRequest wbRequest = (WaybackRequest)EasyMock
						.getCurrentArguments()[0];
					// This emulates key ResourceIndex behavior 
					wbRequest.getAccessPoint().createExclusionFilter();
					return results;
				}
			});
		EasyMock.expect(replay.getClosest(EasyMock.<WaybackRequest>notNull(), EasyMock.<CaptureSearchResults>notNull())).andReturn(
			results.getClosest());
		
		response.setStatus(302);
		// Redirect URL shall have collection name, not "web" (see ResultURIConverter setup above).
		response.setHeader(EasyMock.matches("(?i)location"),
			EasyMock.eq("/2604/20140101000000/http://example.com/"));
		// XXX currently Memento is always enabled in AccessPointAdapter 
		// no matter what value CompositeAccessPoint.enableMemento has.
		response.setHeader(EasyMock.matches("(?i)link"), EasyMock.<String>notNull());
		EasyMock.expect(response.getWriter()).andReturn(new PrintWriter(new StringWriter()));

		EasyMock.replay(request, response, exclusionFactory,
			resourceStore, resourceIndex, query, replay, replayRenderer);
		
		cut.handleRequest(request, response);

	}
}
