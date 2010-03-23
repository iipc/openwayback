/* RobotRulesTest
 *
 * $Id$:
 *
 * Created on Jan 15, 2010.
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

package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class RobotRulesTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.accesscontrol.robotstxt.RobotRules#blocksPathForUA(java.lang.String, java.lang.String)}.
	 */
	public void testBlocksPathForUA() {
		String testString = "User-agent: *\nDisallow:\n";
		RobotRules rr = new RobotRules();
		try {
			rr.parse(new ByteArrayInputStream(testString.getBytes()));
			assertFalse(rr.hasSyntaxErrors());
			assertFalse(rr.blocksPathForUA("/", "ia_archiver"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
