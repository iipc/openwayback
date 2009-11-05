/* SvnTest
 *
 * $Id$ :
 *
 * Created on Nov 5, 2009.
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

package org.archive.wayback.surt;

/**
 * @author brad
 *
 */
public class SvnTest {
	public static String CURRENT_SVN_VERSION = "$Rev$";
	/**
	 *
	 */
	public String foo() {
		return CURRENT_SVN_VERSION;
	}
	public static void main(String[] args) {
		SvnTest s = new SvnTest();
		System.out.println("SvnTest version is " + s.foo());
	}
}
