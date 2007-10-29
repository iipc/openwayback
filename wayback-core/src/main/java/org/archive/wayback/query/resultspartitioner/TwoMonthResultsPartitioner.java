/* TwoMonthResultsPartitioner
 *
 * $Id$
 *
 * Created on 5:50:33 PM Jan 12, 2006.
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
		return wbRequest.getFormatter().format("ResultPartitions.month", 
				start.getTime());
	}
}
