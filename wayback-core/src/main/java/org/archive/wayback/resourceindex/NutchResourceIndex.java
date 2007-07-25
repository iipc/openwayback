/* NutchResourceIndex
 *
 * $Id$
 *
 * Created on 2:29:41 PM Oct 11, 2006.
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

import it.unimi.dsi.mg4j.util.MutableString;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class NutchResourceIndex implements ResourceIndex {
	   private static final Logger LOGGER =
	        Logger.getLogger(NutchResourceIndex.class.getName());

	private final static int MAX_RECORDS = 1000;
	private int maxRecords = MAX_RECORDS;

	
   private static final String NUTCH_NS =
       "http://www.nutch.org/opensearchrss/1.0/";
   private String searchUrlBase;
   private DocumentBuilderFactory factory;
   private DocumentBuilder builder;
   private static final String NUTCH_ARCNAME = "arcname";
   private static final String NUTCH_ARCOFFSET = "arcoffset";
   private static final String NUTCH_ARCDATE = "tstamp";
   private static final String NUTCH_DIGEST = "digest";
   private static final String NUTCH_PRIMARY_TYPE = "primaryType";
   private static final String NUTCH_SUB_TYPE = "subType";
   private static final String NUTCH_CAPTURE_HOST = "site";
   private static final String NUTCH_CAPTURE_URL = "link";

   private static final String NUTCH_SEARCH_RESULT_TAG = "item";
   private static final String NUTCH_SEARCH_RESULTS_TAG = "channel";
   private static final String NUTCH_FIRST_RESULT = "opensearch:startIndex";
   private static final String NUTCH_NUM_RESULTS = "opensearch:totalResults";
   private static final String NUTCH_NUM_RETURNED = "opensearch:itemsPerPage";
   
   private static final String NUTCH_DEFAULT_HTTP_CODE = "200";
   private static final String NUTCH_DEFAULT_REDIRECT_URL = "-";
   
	/**
	 * @throws ConfigurationException
	 */
	public void init() throws ConfigurationException {
		LOGGER.info("initializing NutchResourceIndex...");
		LOGGER.info("Using base search url " + this.searchUrlBase);

		this.factory = DocumentBuilderFactory.newInstance();
		this.factory.setNamespaceAware(true);
		try {
			this.builder = this.factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO: quiet extra stacktrace..
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceIndex#query(org.archive.wayback.core.WaybackRequest)
	 */
	public SearchResults query(WaybackRequest wbRequest) 
		throws ResourceIndexNotAvailableException,
		ResourceNotInArchiveException, BadQueryException, 
		AccessControlException {

		// Get the URL for the request:
		String requestUrl = getRequestUrl(wbRequest);
		Document document = null;
		try {
			// HTTP Request + parse
			document =  getHttpDocument(requestUrl);
		} catch (IOException e) {
			// TODO: better error for user:
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException("Unexpected SAX: " + 
					e.getMessage());
		}

		SearchResults results = new UrlSearchResults();
		NodeList channel = getSearchChannel(document);
		NodeList nodes = getSearchItems(document);

		if (channel == null || channel.getLength() != 1) {
			// TODO: better error for user:
       	throw new ResourceNotInArchiveException("No results for " +
       			requestUrl);
       }

       if (nodes == null) {
			// TODO: better error for user:
       	throw new ResourceNotInArchiveException("No results for " +
       			requestUrl);
       }

       for (int i = 0; i < nodes.getLength(); i++) {
       	
           Element e = (Element) nodes.item(i);

           SearchResult result = elementToSearchResult(e);
           results.addSearchResult(result);
       }
       Element channelElement = (Element) channel.item(0);
       
       results.putFilter(WaybackConstants.RESULTS_FIRST_RETURNED,
       		getNodeContent(channelElement,NUTCH_FIRST_RESULT));
       
       results.putFilter(WaybackConstants.RESULTS_NUM_RESULTS,
       		getNodeContent(channelElement,NUTCH_NUM_RESULTS));
       
       results.putFilter(WaybackConstants.RESULTS_NUM_RETURNED,
       		getNodeContent(channelElement,NUTCH_NUM_RETURNED));
       
       results.putFilter(WaybackConstants.RESULTS_REQUESTED,
       		String.valueOf(wbRequest.getResultsPerPage()));
       
		results.putFilter(WaybackConstants.REQUEST_START_DATE,
				Timestamp.earliestTimestamp().getDateStr());
		
       results.putFilter(WaybackConstants.REQUEST_END_DATE,
       		Timestamp.latestTimestamp().getDateStr());
		return results;
	}

	private SearchResult elementToSearchResult(Element e) {

		SearchResult result = new SearchResult();

		result.put(WaybackConstants.RESULT_ARC_FILE,
				getNodeNutchContent(e,NUTCH_ARCNAME));
		
        // The date in nutchwax is now named 'tstamp' and its
        // 17 characters rather than 14.  Pass first 14 only.
        String d = getNodeNutchContent(e,NUTCH_ARCDATE);
        if (d.length() == 17) {
            d = d.substring(0, 14);
        }
		result.put(WaybackConstants.RESULT_CAPTURE_DATE, d);
		
		//result.put(WaybackConstants.RESULT_HTTP_CODE,getNodeContent(e,""));
		result.put(WaybackConstants.RESULT_HTTP_CODE,NUTCH_DEFAULT_HTTP_CODE);
		result.put(WaybackConstants.RESULT_MD5_DIGEST,
				getNodeNutchContent(e,NUTCH_DIGEST));
		
		result.put(WaybackConstants.RESULT_MIME_TYPE,
				getNodeNutchContent(e,NUTCH_PRIMARY_TYPE) + "/" + 
				getNodeNutchContent(e,NUTCH_SUB_TYPE));
		
		result.put(WaybackConstants.RESULT_OFFSET,
				getNodeNutchContent(e,NUTCH_ARCOFFSET));
		
		result.put(WaybackConstants.RESULT_ORIG_HOST,
				getNodeNutchContent(e,NUTCH_CAPTURE_HOST));
//		result.put(WaybackConstants.RESULT_REDIRECT_URL,getNodeContent(e,""));
		result.put(WaybackConstants.RESULT_REDIRECT_URL,
				NUTCH_DEFAULT_REDIRECT_URL);
		result.put(WaybackConstants.RESULT_URL,getNodeContent(e,
				NUTCH_CAPTURE_URL));
		
		return result;
	}
	
   protected NodeList getSearchChannel(Document d) {
       if (d ==  null) {
           return null;
       }
       // Jump to the search item list.
       NodeList nodes = d.getElementsByTagName(NUTCH_SEARCH_RESULTS_TAG);
       return (nodes.getLength() <= 0)? null: nodes;
   }
   
   protected NodeList getSearchItems(Document d) {
       if (d ==  null) {
           return null;
       }
       // Jump to the search item list.
       NodeList nodes = d.getElementsByTagName(NUTCH_SEARCH_RESULT_TAG);
       return (nodes.getLength() <= 0)? null: nodes;
   }

	
   protected String getRequestUrl(WaybackRequest wbRequest) 
   throws BadQueryException {
//   	final String urlStr, final String date,
//   }
//           final int count)
//   throws IOException {
   	String urlStr = wbRequest.get(WaybackConstants.REQUEST_URL);
   	String exactDateStr = wbRequest.get(WaybackConstants.REQUEST_EXACT_DATE);
    if (exactDateStr != null && exactDateStr.length() == 0) {
        exactDateStr = null;
    }
   	String endDateStr = wbRequest.get(WaybackConstants.REQUEST_END_DATE);
   	if (endDateStr == null || endDateStr.length() == 0) {
   	   	endDateStr = wbRequest.get(WaybackConstants.REQUEST_DATE);
   	}
   	String startDateStr = wbRequest.get(WaybackConstants.REQUEST_START_DATE);
   	if (startDateStr == null || startDateStr.length() == 0) {
   		startDateStr = Timestamp.earliestTimestamp().getDateStr();
   	}
   	int hitsPerPage = wbRequest.getResultsPerPage();
   	if(hitsPerPage < 1) {
   		throw new BadQueryException("Hits per page must be positive");
   	}
   	if(hitsPerPage > maxRecords) {
   		throw new BadQueryException("Hits per page must be less than " +
   				maxRecords);
   	}
   	int start = (wbRequest.getPageNum()-1) * hitsPerPage;
       if (urlStr == null || urlStr.length() <= 0) {
           throw new BadQueryException("Url is empty.");
       }
       // Construct the search url.
       MutableString ms = new MutableString(this.searchUrlBase)
           .append("?query=");
       // Add 'date:...+' to query string.
       // As searching for exact dates is not what we want in most cases,
       // we will only use this if REQUEST_[END_]DATE is empty;
       if ((endDateStr == null || endDateStr.length() == 0)
				&& exactDateStr != null && exactDateStr.length() > 0) {
           ms.append("date%3A").append(exactDateStr).append('+');
    	   
       } else {
           ms.append("date%3A").append(startDateStr).append('-').append(
               exactDateStr != null ? exactDateStr : endDateStr).append('+');
       }
       // Add 'url:URL'.
       if(wbRequest.get(WaybackConstants.REQUEST_TYPE).equals(
	                WaybackConstants.REQUEST_URL_PREFIX_QUERY)) {
           ms.append("url%3A").append(urlStr);
       } else {
           try {
            ms.append("exacturl%3A").
                append(java.net.URLEncoder.encode(urlStr, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new BadQueryException(e.toString());
        }
    	   // when searching for exacturl, we are mostly
    	   // interested in the different versions over the time
           ms.append("&sort=date");
           ms.append("&reverse=true");
       }
       ms.append("&hitsPerPage=").append(hitsPerPage);
       ms.append("&start=").append(start);
       ms.append("&dedupField=site");
       // As we are always searching agains an url, a
       // higher perDup/Site will return just more versions
       ms.append("&hitsPerDup=").append(hitsPerPage);
       ms.append("&hitsPerSite=").append(hitsPerPage);
       
       return ms.toString();
   }

	
	// extract the text content of a single nutch: tag under a node
   protected String getNodeNutchContent(Element e, String key) {
       NodeList nodes = e.getElementsByTagNameNS(NUTCH_NS, key);
       String result = null;
       if (nodes != null && nodes.getLength() > 0) {
           result = nodes.item(0).getTextContent();
       }
       return (result == null || result.length() == 0)? null: result;
   }

   // extract the text content of a single tag under a node
   protected String getNodeContent(Element e, String key) {
       NodeList nodes = e.getElementsByTagName(key);
       String result = null;
       if (nodes != null && nodes.getLength() > 0) {
           result = nodes.item(0).getTextContent();
       }
       return (result == null || result.length() == 0)? null: result;
   }

   // do an HTTP request, plus parse the result into an XML DOM
   protected synchronized Document getHttpDocument(String url) 
   	throws IOException, SAXException {
   	
       Document d = null;
       d = this.builder.parse(url);
       return d;
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
/**
 * @return the maxRecords
 */
public int getMaxRecords() {
	return maxRecords;
}
/**
 * @param maxRecords the maxRecords to set
 */
public void setMaxRecords(int maxRecords) {
	this.maxRecords = maxRecords;
}
}
