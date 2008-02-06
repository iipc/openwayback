/* TimestampTest
 *
 * $Id$
 *
 * Created on 6:44:30 PM Jan 11, 2006.
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
package org.archive.wayback.core;

import java.util.Calendar;

import junit.framework.TestCase;

import org.archive.wayback.core.Timestamp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TimestampTest extends TestCase {
	/**
	 * run several padding tests
	 */
	public void testPadDateStr() {

		String curYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		
		assertEquals("padStart '1'","19960101000000",Timestamp.padStartDateStr("1"));
		assertEquals("padEnd '1'","19991231235959",Timestamp.padEndDateStr("1"));
		assertEquals("padStart '2'","20000101000000",Timestamp.padStartDateStr("2"));
		assertEquals("padEnd",curYear + "1231235959",Timestamp.padEndDateStr("2"));
		assertEquals("padEnd",curYear + "1231235959",Timestamp.padEndDateStr("3"));
		assertEquals("padEnd","20061231235959",Timestamp.padEndDateStr("2006"));
		assertEquals("padEnd","20061231235959",Timestamp.padEndDateStr("200613"));
		assertEquals("padEnd","20071231235959",Timestamp.padEndDateStr("2007"));
		

		// day of month stuff:
		assertEquals("padEnd","20060131235959",Timestamp.padEndDateStr("200601"));
		assertEquals("padEnd","20060228235959",Timestamp.padEndDateStr("200602"));
		assertEquals("padEnd","20060331235959",Timestamp.padEndDateStr("200603"));
		assertEquals("padEnd","20060430235959",Timestamp.padEndDateStr("200604"));
		assertEquals("padEnd","20060430235959",Timestamp.padEndDateStr("2006044"));

		assertEquals("padEnd","20050228235959",Timestamp.padEndDateStr("200502"));
		assertEquals("padEnd","20040229235959",Timestamp.padEndDateStr("200402"));
		assertEquals("padEnd","20030228235959",Timestamp.padEndDateStr("200302"));

		assertEquals("padEnd","19960229235959",Timestamp.padEndDateStr("199602"));
		assertEquals("padStart","19960201000000",Timestamp.padStartDateStr("199602"));
		
		assertEquals("padStart","19960101000000",Timestamp.padStartDateStr("19960"));
		assertEquals("padEnd","19960930235959",Timestamp.padEndDateStr("19960"));
		
		assertEquals("padStart","19961001000000",Timestamp.padStartDateStr("19961"));
		assertEquals("padEnd","19961231235959",Timestamp.padEndDateStr("19961"));

		assertEquals("padStart","19961001000000",Timestamp.padStartDateStr("19962"));
		assertEquals("padEnd","19961231235959",Timestamp.padEndDateStr("19962"));
		
		assertEquals("padStart","19960101000050",Timestamp.padStartDateStr("19960101000060"));
		assertEquals("padEnd","19960101000050",Timestamp.padEndDateStr("19960101000060"));
		

	}
	/**
	 * 
	 */
	public void testConstructors() {
		int sse = 1147986348;
		String dateSpec = "20060518210548";
		assertEquals("bad fromSSe",dateSpec,Timestamp.fromSse(sse).getDateStr());
	}
}
