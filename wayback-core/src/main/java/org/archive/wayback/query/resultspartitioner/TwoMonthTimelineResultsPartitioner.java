package org.archive.wayback.query.resultspartitioner;

import java.util.Calendar;

import org.archive.wayback.core.WaybackRequest;

public class TwoMonthTimelineResultsPartitioner extends TwoMonthResultsPartitioner {
	protected String rangeToTitle(Calendar start, Calendar end,
			WaybackRequest wbRequest) {
		Calendar endMinusSecond = getCalendar();
		endMinusSecond.setTime(end.getTime());
		endMinusSecond.add(Calendar.SECOND,-1);
		return wbRequest.getFormatter().format("ResultPartitions.month", 
				start.getTime());
	}
}
