/* CDXRecordTest
 *
 * $Id$
 *
 * Created on 3:08:30 PM Feb 23, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.cdx;

import org.apache.commons.httpclient.URIException;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXRecordTest extends TestCase {

	/**
	 * Test method for 'org.archive.wayback.cdx.CDXRecord.urlStringToKey(String)'
	 */
	public void testUrlStringToKey() {
		checkCanonicalization("http://foo.com/","foo.com/");
		checkCanonicalization("http://www.foo.com/","foo.com/");
		checkCanonicalization("http://www.foo.com","foo.com/");
		checkCanonicalization("http://www12.foo.com/","foo.com/");
		checkCanonicalization("www12.foo.com/","foo.com/");
		checkCanonicalization("www12.foo.com","foo.com/");
		checkCanonicalization("foo.com/","foo.com/");
		checkCanonicalization("foo.com","foo.com/");

		
	}
	private void checkCanonicalization(String orig, String want) {
		String got;
		try {
			got = CDXRecord.urlStringToKey(orig);
			assertEquals("Failed canonicalization (" + orig + ") => (" + got + 
					") and not (" + want + ") as expected",got,want);
		} catch (URIException e) {
			e.printStackTrace();
			assertTrue("Exception converting(" + orig + ")",false);
		}
	}

}
