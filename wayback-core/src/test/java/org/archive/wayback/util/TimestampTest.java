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

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.archive.wayback.util.Timestamp;

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

		String curYear = String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.YEAR));
		
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
