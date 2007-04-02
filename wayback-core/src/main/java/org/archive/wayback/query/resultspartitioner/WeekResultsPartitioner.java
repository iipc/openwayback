/* WeekResultsPartitioner
 *
 * $Id$
 *
 * Created on 6:02:47 PM Jan 11, 2006.
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
package org.archive.wayback.query.resultspartitioner;

import java.util.Calendar;

import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WeekResultsPartitioner extends ResultsPartitioner {
	private static int MAX_SECONDS_SPANNED = 60 * 60 * 24 * 7 * 8;
	public int maxSecondsSpanned() {
		return MAX_SECONDS_SPANNED;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.resultspartitioner.ResultsPartitioner#alignStart(java.util.Calendar)
	 */
	protected void alignStart(Calendar start) {
		start.set(Calendar.HOUR_OF_DAY,1);
		start.set(Calendar.MINUTE,1);
		start.set(Calendar.SECOND,1);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resultspartitioner.ResultsPartitioner#endOfPartition(java.util.Calendar)
	 */
	protected Calendar incrementPartition(Calendar start, int count) {
		Calendar end = getCalendar();
		end.setTime(start.getTime());
		end.add(Calendar.DAY_OF_YEAR,7 * count);
		return end;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resultspartitioner.ResultsPartitioner#rangeToTitle(java.util.Calendar, java.util.Calendar)
	 */
	protected String rangeToTitle(Calendar start, Calendar end,
			WaybackRequest wbRequest) {

		Calendar end2 = getCalendar();
		end2.setTime(end.getTime());
		end2.add(Calendar.DAY_OF_YEAR,-1);
		return wbRequest.getFormatter().format("ResultPartitions.twoMonth", 
				start.getTime(),end2.getTime());
	}
}
