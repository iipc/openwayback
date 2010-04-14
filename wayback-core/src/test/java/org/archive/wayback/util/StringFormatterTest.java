/* StringFormatterTest
 *
 * $Id$:
 *
 * Created on Apr 13, 2010.
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
		assertEquals("d1",fmt.format("d1",new Date(0L)));
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
