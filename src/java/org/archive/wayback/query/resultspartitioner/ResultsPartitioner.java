/* ResultsPartitioner
 *
 * $Id$
 *
 * Created on 6:43:42 PM Dec 29, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.SearchResults;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class ResultsPartitioner {

	
	protected Calendar getCalendar() {
		String[] ids = TimeZone.getAvailableIDs(0);
		if (ids.length < 1) {
			return null;
		}
		TimeZone gmt = new SimpleTimeZone(0, ids[0]);
		return new GregorianCalendar(gmt);		
	}
	
	private Calendar dateStrToCalendar(String dateStr) {
		return Timestamp.dateStrToCalendar(dateStr);
	}

	/**
	 * @return the maximum seconds viewable within this partition type.
	 */
	public abstract int maxSecondsSpanned();
	
	protected abstract void alignStart(Calendar start);

	protected abstract Calendar endOfPartition(Calendar start);

	protected abstract String rangeToTitle(Calendar start, Calendar end);

	/**
	 * Create as many partitions as needed to hold all SearchResult objects
	 * in argument, and initialize those partitions with the correct
	 * SearchResult objects. 
	 * @param results
	 * @return ArrayList of ResultsPartition objects
	 */
	public ArrayList createPartitions(SearchResults results) {

		ArrayList partitions = new ArrayList();

		String rsd = results.getFilter(WaybackConstants.REQUEST_START_DATE);
		String red = results.getFilter(WaybackConstants.REQUEST_END_DATE);
		Calendar startCal = dateStrToCalendar(rsd);
		Calendar lastCal = dateStrToCalendar(red);

		alignStart(startCal);
		Calendar endCal = endOfPartition(startCal);
		while (true) {
			String startDateStr = ArchiveUtils.get14DigitDate(startCal
					.getTime());
			String endDateStr = ArchiveUtils.get14DigitDate(endCal.getTime());
			String title = rangeToTitle(startCal, endCal);
			ResultsPartition partition = new ResultsPartition(startDateStr,
					endDateStr, title);
			partition.filter(results);
			partitions.add(partition);

			if (endCal.after(lastCal)) {
				break;
			}
			startCal = endCal;
			endCal = endOfPartition(startCal);
		}
		return partitions;
	}
	protected String prettyHourOfDay(Calendar cal) {
		String ampm = cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
		return cal.get(Calendar.HOUR) + " " + ampm;
	}
	
	protected String prettyDayOfMonth(Calendar cal) {
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		String pretty = dayOfMonth + "th";
		if(dayOfMonth == 1) {
			pretty = "1st";
		} else if(dayOfMonth == 2) {
			pretty = "2nd";
		} else if(dayOfMonth == 3) {
			pretty = "3rd";
		}
		return pretty;
	}

	protected String prettyMonth(Calendar cal) {
		int monthNum = cal.get(Calendar.MONTH);
		String month = "";
		// TODO: localization...?
		if(monthNum == Calendar.JANUARY) {
			month = "Jan";
		} else if(monthNum == Calendar.FEBRUARY) {
			month = "Feb";
		} else if(monthNum == Calendar.MARCH) {
			month = "Mar";
		} else if(monthNum == Calendar.APRIL) {
			month = "Apr";
		} else if(monthNum == Calendar.MAY) {
			month = "May";
		} else if(monthNum == Calendar.JUNE) {
			month = "Jun";
		} else if(monthNum == Calendar.JULY) {
			month = "Jul";
		} else if(monthNum == Calendar.AUGUST) {
			month = "Aug";
		} else if(monthNum == Calendar.SEPTEMBER) {
			month = "Sep";
		} else if(monthNum == Calendar.OCTOBER) {
			month = "Oct";
		} else if(monthNum == Calendar.NOVEMBER) {
			month = "Nov";
		} else if(monthNum == Calendar.DECEMBER) {
			month = "Dec";
		} else {
			month = "UNK";
		}
		return month;
	}

	protected String prettyFullMonth(Calendar cal) {
		int monthNum = cal.get(Calendar.MONTH);
		String month = "";
		// TODO: localization...?
		if(monthNum == Calendar.JANUARY) {
			month = "January";
		} else if(monthNum == Calendar.FEBRUARY) {
			month = "February";
		} else if(monthNum == Calendar.MARCH) {
			month = "March";
		} else if(monthNum == Calendar.APRIL) {
			month = "April";
		} else if(monthNum == Calendar.MAY) {
			month = "May";
		} else if(monthNum == Calendar.JUNE) {
			month = "June";
		} else if(monthNum == Calendar.JULY) {
			month = "July";
		} else if(monthNum == Calendar.AUGUST) {
			month = "August";
		} else if(monthNum == Calendar.SEPTEMBER) {
			month = "September";
		} else if(monthNum == Calendar.OCTOBER) {
			month = "October";
		} else if(monthNum == Calendar.NOVEMBER) {
			month = "November";
		} else if(monthNum == Calendar.DECEMBER) {
			month = "December";
		} else {
			month = "UNK";
		}
		return month;
	}
	
	protected String prettyYear(Calendar cal) {
		return "" + cal.get(Calendar.YEAR);
	}

}
