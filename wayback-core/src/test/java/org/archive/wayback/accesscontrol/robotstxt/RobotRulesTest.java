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
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Tests for {@link RobotRules}
 * <p>References:
 * <ul>
 * <li>https://developers.google.com/webmasters/control-crawl-index/docs/robots_txt
 * - referred as "Google/Yahoo/Bing/Ask (GYBA) convention".</li>
 * <li>http://www.robotstxt.org/orig.html</li>
 * <li>http://www.robotstxt.org/norobots-rfc.txt - referred as "RFC".</li>
 * </ul>
 * </p>
 * <p>Note: GYBA convention document and RFC differs in terminology. What GYBA calls
 * "group" is called "record" in RFC.</p>
 * @author brad
 *
 */
public class RobotRulesTest extends TestCase {
	
	public static final String WB_UA = "ia_archiver";
	
	RobotRules rr;
	
	public void setUp() {
		rr = new RobotRules();
	}
	protected RobotRules load(String txt) throws IOException {
		rr.parse(new ByteArrayInputStream(txt.getBytes("UTF-8")));
		assertFalse(rr.hasSyntaxErrors());
		return rr;
	}
	
	/**
	 * basic test
	 */
	public void testDirectivesAreCaseInsensitive() throws Exception {
		final String testString =
				"user-Agent: *\n" +
				"disAllow: /\n";
		load(testString);
		
		assertTrue(rr.blocksPathForUA("/", WB_UA));
	}
	
	/**
	 * Disallow: with empty path has no effect.
	 */
	public void testEmptyDisallowHasNoEffect() throws Exception {
		final String testString = 
				"User-agent: *\n" +
				"Disallow:\n";
		load(testString);

		assertFalse(rr.blocksPathForUA("/", WB_UA));
	}
	
	/**
	 * optional white spaces before/after "{@code :}", before EOL.
	 * @throws Exception
	 */
	public void testLessSpaceExtraSpace() throws Exception {
		final String testString =
				"User-agent :*  \n" +
				"Disallow:/  \n";
		load(testString);
		
		assertTrue(rr.blocksPathForUA("/index.html", WB_UA));
	}
	
	/**
	 * white spaces are allowed at the beginning of the line, too.
	 * @throws Exception
	 */
	public void testExtraSpace2() throws Exception {
		final String testString =
				"  User-agent: *\n" +
				"  Disallow:/\n";
		load(testString);

		assertTrue(rr.blocksPathForUA("/index.html", WB_UA));
	}

	public void testComments() throws Exception {
		final String testString =
				"# User-agent: *\n" +
				"User-agent: google-news\n" +
				"Disallow: /post # block post CGI\n" +
				"\n" +
				"User-agent: ia_archiver # Wayback\n" +
				"Disallow: /dontshow/\n";
		load(testString);
		
		assertFalse(rr.blocksPathForUA("/post", WB_UA));
		assertTrue(rr.blocksPathForUA("/dontshow/secret.html", WB_UA));
	}
	
	/**
	 * LF, CRLF, CR are recognized as end-of-line.
	 */
	public void testEOLs() throws Exception {
		final String testString =
				"User-agent: *\r" +
				"Disallow: /\r";
		load(testString);
		
		assertTrue(rr.blocksPathForUA("/", WB_UA));
	}
	
	// ==== group parsing and user-agent matching tests ====
	
	/**
	 * user-agent name comparisons are case-insensitive.
	 */
	public void testUserAgentIsCaseInsensitive() throws Exception {
		final String testString =
				"User-agent: IA_Archiver\n" +
				"Disallow: /\n";
		load(testString);
		
		assertTrue(rr.blocksPathForUA("/", WB_UA));
	}
	
	/**
	 * multiple records for different User-agent's.
	 * <p>while RFC states "the format logically consists of a non-empty set
	 * or records, separated by blank lines", Google's documentation has no
	 * mention to blank lines as group separator - instead, it recognizes a sequence 
	 * of User-agent: as the start of "group". So this sample is syntax error per
	 * RFC, but okay according to Google/Yahoo/Bing/Ask convention.
	 * @throws Exception
	 */
	public void testNonBlocksPathForUA() throws Exception {
		final String testString =
				"User-agent: *\n" +
				"Allow: /\n" +
				"User-agent: Google-bot\n" +
				"Disallow: /\n";
		load(testString);

		assertFalse(rr.blocksPathForUA("/", WB_UA));
	}
	
	/**
	 * there's sitemap (non-allow/disallow) directive after User-agent.
	 * next user-agent should start a new group.
	 * ia_archiver ALLOWED as disallow rule only applies to B.
	 */
	public void testMultiUAWithOtherLinesLine() throws Exception {
		final String testString =
				"User-agent: *\n" +
				"Sitemap: X\n" +
				"\n" +
				"User-agent: B\n" +
				"Disallow: /\n";
		load(testString);

		// ALLOWED
		assertFalse(rr.blocksPathForUA("/", WB_UA));
	}
	
	/**
	 * similarly to previous test case, {@code Crawl-delay:} line shall
	 * end the group.
	 */
	public void testMultiUAWithOtherLinesLine2() throws Exception {
		final String testString =
				"User-agent: *\n" +
				"Crawl-delay: 30\n" +
				"\n" +
				"User-agent: B\n" +
				"Disallow: /\n";
		load(testString);

		// ALLOWED
		assertFalse(rr.blocksPathForUA("/", WB_UA));
	}

	/**
	 * this is a syntax error per RFC, but ok with Google/Yahoo/Bing/Ask convention.
	 * blank lines are permitted within a group to improve readability. they are simply
	 * ignored, don't end group.
	 */
	public void testBlankLineInGroup() throws Exception {
		final String testString =
				"User-agent: *\n" +
				"Disallow: /media/\n" +
				"\n" +
				"Disallow: /cgi-bin/\n" +
				"# images\n" + 
				"Disallow: /img/\n" +
				"Disallow: /icon/\n" +
				"\n" +
				"\n" +
				"Disallow: /actions/\n" +
				"\n" +
				"# sensitive stuff\n" +
				"Disallow: /etc\n";
		load(testString);
		
		assertTrue(rr.blocksPathForUA("/cgi-bin/", WB_UA));
		assertTrue(rr.blocksPathForUA("/img/", WB_UA));
		assertTrue(rr.blocksPathForUA("/icon/", WB_UA));
		assertTrue(rr.blocksPathForUA("/etc/", WB_UA));
	}
	
	/**
	 * multiple User-agent: for a record.
	 * ia_archiver BLOCKED as disallow rule applies to both all and B
	 * @throws Exception
	 */
	public void testMultiUA() throws Exception {
		String testString =
				"User-agent: *\n" +
				"User-agent: B\n" +
				"Disallow: /\n";
		load(testString);

		// BLOCKED
		assertTrue(rr.blocksPathForUA("/", WB_UA));
	}
	
	// ==== path matching tests ===
	
	/**
	 * path matching basics. substring-based, <code>/</code> is no special,
	 * and case-sensitive.
	 */
	public void testSubpath() throws Exception {
		String testString =
				"User-agent: *\n" +
				"Disallow: /media\n" +
				"Disallow: /hidden/\n";
		load(testString);
		
		assertFalse(rr.blocksPathForUA("/", WB_UA));
		assertTrue(rr.blocksPathForUA("/media/theme.mp3", WB_UA));
		assertTrue(rr.blocksPathForUA("/media?order=2", WB_UA));
		assertFalse(rr.blocksPathForUA("/images/logo.png", WB_UA));
		assertFalse(rr.blocksPathForUA("/hidden", WB_UA));
		assertTrue(rr.blocksPathForUA("/hidden/index.html", WB_UA));
		
		// path match is case-sensitive
		assertFalse(rr.blocksPathForUA("/Hidden/notreally.mp3", WB_UA));
	}
	
	/**
	 * character may be %-escaped, but %2f (<code>/</code>) is special.
	 * (TODO: additional tests: robots.txt is assumed to be UTF-8 encoded.
	 * non-7bit-ascii characters are allowed, and also can be %-escaped.)
	 * @throws Exception
	 */
	public void testPercentEncodedPath() throws Exception {
		String testString =
				"User-agent: *\n" +
				"Disallow: /%7Ethomas/\n" +
				"Disallow: /a%2fb.html\n" +
				"Disallow: /n/m.png\n";
		load(testString);
		
		// (some assertions are disabled until we decide to fix it)
		// not sure about these two. if RobotRules expects canonicalized URL,
		// only either of these shall be tested.
//		assertTrue(rr.blocksPathForUA("/~thomas/welcome.html", WB_UA));
//		assertTrue(rr.blocksPathForUA("/%7ethomas/", WB_UA));
		// RFC states %2f does not match "/"
		assertTrue(rr.blocksPathForUA("/a%2fb.html", WB_UA));
		assertFalse(rr.blocksPathForUA("/a/b.html", WB_UA));
//		assertTrue(rr.blocksPathForUA("/n%2fm.png", WB_UA));
	}
	
	/**
	 * <p>By GYBA convention, if multiple
	 * disallow and allow directives matches the URL, the most specific
	 * rule based on the length of the path will win over less specific
	 * (shorter) ones.</p> 
	 * <p>RFC says differently: "a robot must attempt to match
	 * the paths in Allow and Disallow lines against the URL, in the
	 * order they occur in the record. The first match found is used."</p>
	 * <p>we follow GYBA convention here.</p>
	 * @throws Exception
	 */
	public void testMostSpecificPathPrevails() throws Exception {
		final String testString =
				"User-agent: *\n" +
				"Disallow: /\n" +
				"Allow: /media\n" +
				"Disallow: /media/private\n";
		load(testString);
		
		// (some assertions are disabled until we decide to fix it)
		assertTrue(rr.blocksPathForUA("/index.html", WB_UA));
//		assertFalse(rr.blocksPathForUA("/media/toc.html", WB_UA));
//		assertFalse(rr.blocksPathForUA("/media/priv", WB_UA));
		assertTrue(rr.blocksPathForUA("/media/private.html", WB_UA));
		assertTrue(rr.blocksPathForUA("/media/private/voice.mp3", WB_UA));
	}

	/**
	 * wildcard in path, matches any chars including <code>/</code>.
	 * <code>/*</code> is the same as <code>/</code>.
	 * @throws Exception
	 */
	public void testWildcardMatch() throws Exception {
		String testString =
				"User-agent: *\n" +
				"Disallow: /media/*\n" +
				"Disallow: /cgi/*.php\n";
		load(testString);
		
		// (some assertions are disabled until we decide to fix it)
		assertFalse(rr.blocksPathForUA("/media", WB_UA));
//		assertTrue(rr.blocksPathForUA("/media/", WB_UA));
//		assertTrue(rr.blocksPathForUA("/media/theme.mp3", WB_UA));
		// perhaps we don't implement non-trailing wildcard. comment out
		// following three if we don't.
//		assertTrue(rr.blocksPathForUA("/cgi/messy.php", WB_UA));
//		assertTrue(rr.blocksPathForUA("/cgi/really.phpt", WB_UA));
		assertFalse(rr.blocksPathForUA("/cgi/noexec.txt", WB_UA));
	}
	
	/**
	 * Google/Bing/Yahoo/Ask extension: <code>$</code> matches the end of path.
	 */
	public void testEndOfPath() throws Exception {
		String testString =
				"User-agent: *\n" +
				"Disallow: /exactly$\n";
		load(testString);
		
		// (some assertions are disabled until we decide to fix it)
		// perhaps we don't support end-of-path marker. comment out these
		// three asserts if that's the case.
//		assertTrue(rr.blocksPathForUA("/exactly", WB_UA));
		assertFalse(rr.blocksPathForUA("/exactly/", WB_UA));
		assertFalse(rr.blocksPathForUA("/exactly/it.html", WB_UA));
	}
}
