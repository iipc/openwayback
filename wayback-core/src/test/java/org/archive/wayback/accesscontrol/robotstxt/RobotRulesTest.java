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
	
	public void testNonBlocksPathForUA() {
		String testString = "User-agent: *\nAllow: /\nUser-agent: Google-bot\nDisallow: /\n";
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
