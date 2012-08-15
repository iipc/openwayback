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
package org.archive.wayback.util;

import java.util.Date;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class StringFormatterTest extends TestCase {

	/**
	 * Tests the various format methods, albeit not very well..
	 */
	public void testFormatStringObjectArray() {
		ResourceBundle rb = new ListResourceBundle() {
			
			@Override
			protected Object[][] getContents() {
				Object[][] m = {
						{"a","A"},
						{"b","Bee"},
						{"f0","()"},
						{"f1","{0}"},
						{"f2","{1} {0}"},
						{"f3","{2} {1} {0}"},
						{"d1","{0,date,YYYY}"}, // that should be 'yyyy'
						{"d2","{0,date,yyyy}"},
				};
				return m;
			}
		};
		Locale l = Locale.getDefault();
		StringFormatter fmt = new StringFormatter(rb, l);
		assertEquals("A",fmt.format("a"));
		assertEquals("Bee",fmt.format("b"));
		assertEquals("()",fmt.format("f0"));
		assertEquals("{0}",fmt.format("f1"));
		assertEquals("1",fmt.format("f1","1"));
		assertEquals("2 1",fmt.format("f2","1","2"));
		assertEquals("3 2 1",fmt.format("f3","1","2",3));
                assertEquals("1970",fmt.format("d2",new Date(0L)));
	}

	/**
	 * Test method for {@link org.archive.wayback.util.StringFormatter#escapeHtml(java.lang.String)}.
	 */
	public void testEscapeHtml() {
		StringFormatter fmt = new StringFormatter(null, null);
		assertEquals("normal",fmt.escapeHtml("normal"));
		assertEquals("normal&amp;",fmt.escapeHtml("normal&"));
		assertEquals("normal&quot;&amp;",fmt.escapeHtml("normal\"&"));
	}

	/**
	 * Test method for {@link org.archive.wayback.util.StringFormatter#escapeJavaScript(java.lang.String)}.
	 */
	public void testEscapeJavaScript() {
		StringFormatter fmt = new StringFormatter(null, null);
		assertEquals("normal",fmt.escapeHtml("normal"));
		assertEquals("normal&amp;",fmt.escapeHtml("normal&"));
		assertEquals("normal&quot;&amp;",fmt.escapeHtml("normal\"&"));
	}

}
