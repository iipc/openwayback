/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.archivalurl;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.archive.wayback.core.WaybackRequest;

/**
 * @see PathRequestParserTest
 * @author brad
 *
 */
public class ArchivalUrlTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toPrefixQueryString(java.lang.String)}.
	 */
	public void testToString_PrefixQuery() {
		WaybackRequest wbr = new WaybackRequest();
		wbr.setUrlQueryRequest();
		wbr.setRequestUrl("http://www.yahoo.com/");
		ArchivalUrl au = new ArchivalUrl(wbr);
		
		assertEquals("*/http://www.yahoo.com/*", au.toString());
	}

	/**
	 * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toQueryString(java.lang.String)}.
	 */
	public void testToString_CaptureQuery() {
		WaybackRequest wbr = new WaybackRequest();
		wbr.setCaptureQueryRequest();
		wbr.setRequestUrl("http://www.yahoo.com/");
		ArchivalUrl au = new ArchivalUrl(wbr);
		assertEquals("*/http://www.yahoo.com/",au.toString());
	}
	
	public void testToString_CaptureQuery_SpecificDateRange() {
	    WaybackRequest wbr = new WaybackRequest();
	    wbr.setCaptureQueryRequest();
	    wbr.setRequestUrl("http://www.yahoo.com/");
	    wbr.setStartTimestamp("20100101000000");
	    wbr.setEndTimestamp("20101231235959");
	    ArchivalUrl au = new ArchivalUrl(wbr);
	    
	    assertEquals("20100101000000-20101231235959*/http://www.yahoo.com/", au.toString());
	    
	    // Open ended date ranges
	    wbr.setEndTimestamp(null);
	    assertEquals("20100101000000-*/http://www.yahoo.com/", au.toString());
        
	    wbr.setStartTimestamp(null);
	    wbr.setEndTimestamp("20101231235959");
	    assertEquals("-20101231235959*/http://www.yahoo.com/", au.toString());
        
        // Query for exact date
	    wbr.put(WaybackRequest.REQUEST_EXACT_DATE, "20100101000000");
	    assertEquals("20100101000000*/http://www.yahoo.com/", au.toString());
	}

    private WaybackRequest createReplayWaybackRequest() {
        WaybackRequest wbr = new WaybackRequest();
        wbr.setReplayRequest();
        wbr.setReplayTimestamp("20010101000000");
        wbr.setRequestUrl("http://www.yahoo.com/");
        return wbr;
    }
	/**
	 * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toReplayString(java.lang.String)}.
	 */
	public void testToString_ReplayRequest() {
		WaybackRequest wbr = createReplayWaybackRequest();
		ArchivalUrl au = new ArchivalUrl(wbr);
		assertEquals("20010101000000/http://www.yahoo.com/", au.toString());
	}
	
	public String[][] CONTEXT_METHOD_FLAG = {
	        { "setIdentityContext", "id" },
	        { "setCSSContext", "cs" },
	        { "setIMGContext", "im" },
	        { "setObjectEmbedContext", "oe" },
	        { "setJSContext", "js" },
	        { "setFrameWrapperContext", "fw" },
	        { "setIFrameWrapperContext", "if" }
	};
	
	public void testToString_ReplayRequest_Contexts() throws Exception {
	    for (String[] tc : CONTEXT_METHOD_FLAG) {
            WaybackRequest wbr = createReplayWaybackRequest();
	        String setterName = tc[0];
	        String flag = tc[1];
	        // I know there's a method ArchivalUrl.assignFlags() for translating flag text
	        // into flags of WeybackRequest, but I should not use a method of class under
	        // test for building test sample!
	        try {
	            Method setter = wbr.getClass().getMethod(setterName, boolean.class);
	            setter.invoke(wbr, Boolean.TRUE);
	        } catch (NoSuchMethodException ex) {
	            fail("WaybackRequest has no method \"" + setterName + "(boolean)\"");
	        }
	        ArchivalUrl au = new ArchivalUrl(wbr);
	        assertEquals("20010101000000" + flag + "_/http://www.yahoo.com/", au.toString());
	    }
	    
	    // actually current implementation allows multiple context flags to be true
	    // at the same time. do we need to test certain combinations?
	}
}
