/* RemoteBDBResourceIndex
 *
 * $Id$
 *
 * Created on 6:06:36 PM Aug 16, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.UrlSearchResult;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * ResourceIndex implementation that relays a query to a remote index 
 * implementation over HTTP. The XMLQueryUI is assumed to be active on the 
 * remote server, and the query is sent over as-is, formulated as an OpenSearch
 * query. Results are also returned as-is -- this class attempts to be as
 * transparent as possible.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteResourceIndex implements ResourceIndex {
	private static final Logger LOGGER = Logger.getLogger(RemoteResourceIndex
			.class.getName());

	private String searchUrlBase;

	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	private static final String WB_XML_REQUEST_TAGNAME = "request";


	private static final String WB_XML_RESULT_TAGNAME = "result";
	private static final String WB_XML_ERROR_TAGNAME = "error";
	private static final String WB_XML_ERROR_TITLE = "title";
	private static final String WB_XML_ERROR_MESSAGE = "message";
	private UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();

	@SuppressWarnings("unchecked")
	private final ThreadLocal tl = new ThreadLocal() {
        protected synchronized Object initialValue() {
        	DocumentBuilder builder = null;
            try {
            	if(factory != null) {
					builder = factory.newDocumentBuilder();
					if (!builder.isNamespaceAware()) {
						LOGGER.severe("Builder is not namespace aware.");
					}
            	}
			} catch (ParserConfigurationException e) {
				// TODO: OK to just "eat" this error? 
				e.printStackTrace();
			}
			return builder;
        }
    };
    private DocumentBuilder getDocumentBuilder() {
        return (DocumentBuilder) tl.get();
    }

    /**
     * @throws ConfigurationException
     */
    public void init() throws ConfigurationException {
		LOGGER.info("initializing RemoteCDXIndex...");

		this.factory.setNamespaceAware(false);
		LOGGER.info("Using base search url " + this.searchUrlBase);		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.ResourceIndex#query(org.archive.wayback.core.WaybackRequest)
	 */
	public SearchResults query(WaybackRequest wbRequest)
		throws ResourceIndexNotAvailableException,
		ResourceNotInArchiveException, BadQueryException,
		AccessControlException {
//		throw new ResourceIndexNotAvailableException("oops");
		return urlToSearchResults(getRequestUrl(wbRequest),
				getSearchResultFilters(wbRequest));
	}

	protected SearchResults urlToSearchResults(String requestUrl,
			ObjectFilter<CaptureSearchResult> filter)
			throws ResourceIndexNotAvailableException,
			ResourceNotInArchiveException, BadQueryException,
			AccessControlException {

		Document document = null;
		try {
			// HTTP Request + parse
			LOGGER.info("Getting index XML from ("+requestUrl+")");
			document = getHttpDocument(requestUrl);
		} catch (IOException e) {
			// TODO: better error for user:
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException("Unexpected SAX: "
					+ e.getMessage());
		}

		checkDocumentForExceptions(document);
		return documentToSearchResults(document, filter);
	}
	
	protected void checkDocumentForExceptions(Document document) 
		throws ResourceIndexNotAvailableException, 
		ResourceNotInArchiveException, BadQueryException, 
		AccessControlException {

		NodeList errors = document.getElementsByTagName(WB_XML_ERROR_TAGNAME);
		if(errors.getLength() != 0) {
			String errTitle = getNodeContent((Element) errors.item(0),
					WB_XML_ERROR_TITLE);
			String errMessage =  getNodeContent((Element) errors.item(0),
					WB_XML_ERROR_MESSAGE);
			
			// TODO: Localization Problems.. Think of something clever.
			if(errTitle == null) {
				throw new ResourceIndexNotAvailableException("Unknown error!");
			} else if(errTitle.equals("Resource Not In Archive")) {
				throw new ResourceNotInArchiveException(errMessage);
			} else if(errTitle.equals("Bad Query Exception")) {
				throw new BadQueryException(errMessage);
			} else if(errTitle.equals("Resource Index Not Available Exception")) {
				throw new ResourceIndexNotAvailableException(errMessage);
			} else if(errTitle.equals("Access Control Exception")) {
				throw new AccessControlException(errMessage);
			} else {
				throw new ResourceIndexNotAvailableException("Unknown error!");				
			}
		}
	}
	private String getResultsType(Document document) {
		NodeList list = document.getElementsByTagName(
				SearchResults.RESULTS_TYPE);
		if(list.getLength() == 1) {
			return list.item(0).getTextContent();
		} else {
			return SearchResults.RESULTS_TYPE_CAPTURE;
		}
	}
	
	protected ObjectFilter<CaptureSearchResult> getSearchResultFilters(
			WaybackRequest wbRequest) {
		ObjectFilterChain<CaptureSearchResult> filters = null;
		if (wbRequest.isReplayRequest()) {
			filters = new ObjectFilterChain<CaptureSearchResult>();
			SelfRedirectFilter selfRedirectFilter = new SelfRedirectFilter();
			selfRedirectFilter.setCanonicalizer(canonicalizer);
			filters.addFilter(selfRedirectFilter);
		} else {
			// no filters for now
			filters = null;
		}
		return filters;
	}
	
	protected SearchResults documentToSearchResults(Document document,
			ObjectFilter<CaptureSearchResult> filter) {
		SearchResults results = null;
		NodeList filters = getRequestFilters(document);
		String resultsType = getResultsType(document);
		if(resultsType.equals(SearchResults.RESULTS_TYPE_CAPTURE)) {
			results = documentToCaptureSearchResults(document,filter);
		} else {
			results = documentToUrlSearchResults(document);
		}
		for(int i = 0; i < filters.getLength(); i++) {
			String key = filters.item(i).getNodeName();
			String value = filters.item(i).getTextContent();
			if(!key.equals("#text")) {
				results.putFilter(key,value);
			}
		}
		return results;
	}
	private UrlSearchResults documentToUrlSearchResults(
			Document document) {
		UrlSearchResults results = new UrlSearchResults();
		NodeList xresults = getSearchResults(document);
		for(int i = 0; i < xresults.getLength(); i++) {
			Node xresult = xresults.item(i);
			UrlSearchResult result = searchElementToUrlSearchResult(xresult);
			results.addSearchResult(result, true);
		}
		return results;
	}
	private CaptureSearchResults documentToCaptureSearchResults(
			Document document, ObjectFilter<CaptureSearchResult> filter) {
		CaptureSearchResults results = new CaptureSearchResults();
		NodeList xresults = getSearchResults(document);
		for(int i = 0; i < xresults.getLength(); i++) {
			Node xresult = xresults.item(i);
			CaptureSearchResult result = searchElementToCaptureSearchResult(xresult);
			
			int ruling = ObjectFilter.FILTER_INCLUDE;
			if (filter != null) {
				ruling = filter.filterObject(result);
			}
			
			if (ruling == ObjectFilter.FILTER_ABORT) {
				break;
			} else if (ruling == ObjectFilter.FILTER_INCLUDE) {
				results.addSearchResult(result, true);
			}
		}
		return results;
	}
	private UrlSearchResult searchElementToUrlSearchResult(Node e) {

		UrlSearchResult result = new UrlSearchResult();
		addNodeDataToSearchResult(e,result);
		return result;
	}
	private CaptureSearchResult searchElementToCaptureSearchResult(Node e) {

		CaptureSearchResult result = new CaptureSearchResult();
		addNodeDataToSearchResult(e,result);
		return result;
	}

	private void addNodeDataToSearchResult(Node e, SearchResult result) {

		NodeList chitlens = e.getChildNodes();
		for(int i = 0; i < chitlens.getLength(); i++) {
			String key = chitlens.item(i).getNodeName();
			String value = chitlens.item(i).getTextContent();
			if(!key.equals("#text")) {
				result.put(key,value);
			}
		}
	}

	protected NodeList getRequestFilters(Document d) {
		if (d == null) {
			return null;
		}
		// Jump to the search item list.
		NodeList nodes = d.getElementsByTagName(WB_XML_REQUEST_TAGNAME);
		if(nodes.getLength() != 1) {
			// TODO: warning?
			return null;
		}
		return nodes.item(0).getChildNodes();
	}

	protected NodeList getSearchResults(Document d) {
		if (d == null) {
			return null;
		}
		NodeList nodes = d.getElementsByTagName(WB_XML_RESULT_TAGNAME);
		return (nodes.getLength() <= 0) ? null : nodes;
	}

	protected String getRequestUrl(WaybackRequest wbRequest)
			throws BadQueryException {
		WaybackRequest tmp = wbRequest.clone();
		if(tmp.isReplayRequest()) {
			tmp.setCaptureQueryRequest();
		}
		return this.searchUrlBase + "?" + tmp.getQueryArguments();
	}

	// extract the text content of a single tag under a node
	protected String getNodeContent(Element e, String key) {
		NodeList nodes = e.getElementsByTagName(key);
		String result = null;
		if (nodes != null && nodes.getLength() > 0) {
			result = nodes.item(0).getTextContent();
		}
		return (result == null || result.length() == 0) ? null : result;
	}

	// do an HTTP request, plus parse the result into an XML DOM
	protected Document getHttpDocument(String url)
			throws IOException, SAXException {
		return (getDocumentBuilder()).parse(url);
	}
	protected Document getFileDocument(File f)
			throws IOException, SAXException {
		return (getDocumentBuilder()).parse(f);
	}

	/**
	 * @return the searchUrlBase
	 */
	public String getSearchUrlBase() {
		return searchUrlBase;
	}

	/**
	 * @param searchUrlBase the searchUrlBase to set
	 */
	public void setSearchUrlBase(String searchUrlBase) {
		this.searchUrlBase = searchUrlBase;
	}

	public void shutdown() throws IOException {
		// No-op
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
}