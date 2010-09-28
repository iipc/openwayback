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
package org.archive.wayback.query.resultspartitioner;

import java.util.Calendar;

import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 * @deprecated use org.archive.wayback.util.parition.*
 */
public class TwoMonthResultsPartitioner extends ResultsPartitioner {
	private static int MAX_SECONDS_SPANNED = 60 * 60 * 24 * 30 * 24;
	public int maxSecondsSpanned() {
		return MAX_SECONDS_SPANNED;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.resultspartitioner.ResultsPartitioner#alignStart(java.util.Calendar)
	 */
	protected void alignStart(Calendar start) {
		start.set(Calendar.DAY_OF_MONTH,1);
		start.set(Calendar.HOUR_OF_DAY,0);
		start.set(Calendar.MINUTE,0);
		start.set(Calendar.SECOND,0);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resultspartitioner.ResultsPartitioner#endOfPartition(java.util.Calendar)
	 */
	protected Calendar incrementPartition(Calendar start, int count) {
		Calendar end = getCalendar();
		end.setTime(start.getTime());
		end.add(Calendar.MONTH,2 * count);
		return end;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resultspartitioner.ResultsPartitioner#rangeToTitle(java.util.Calendar, java.util.Calendar)
	 */
	protected String rangeToTitle(Calendar start, Calendar end,
			WaybackRequest wbRequest) {
		Calendar endMinusSecond = getCalendar();
		endMinusSecond.setTime(end.getTime());
		endMinusSecond.add(Calendar.SECOND,-1);
		return wbRequest.getFormatter().format("ResultPartitions.twoMonth", 
				start.getTime(),endMinusSecond.getTime());
	}
}
