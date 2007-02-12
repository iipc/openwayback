/* ResultsTimelinePartitionFactory
 *
 * $Id$
 *
 * Created on 4:10:53 PM Apr 12, 2006.
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

import java.util.ArrayList;
import java.util.Calendar;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResultsTimelinePartitionsFactory {

	private static int NUM_HOUR_PARTITIONS = 12;
	private static int NUM_DAY_PARTITIONS = 15;
	private static int NUM_MONTH_PARTITIONS = 12;
	private static int NUM_YEAR_PARTITIONS = 10;
	
	// These are sort of "ball park" figures. Should be using calendars
	// for better accuracy..
	private static int MAX_HOUR_SECONDS = 60 * 60 * NUM_HOUR_PARTITIONS;
	private static int MAX_DAY_SECONDS = 60 * 60 * 24 * NUM_DAY_PARTITIONS;
	private static int MAX_MONTH_SECONDS = 60 * 60 * 24 * 30 * NUM_MONTH_PARTITIONS;
	//private static int MAX_YEAR_SECONDS = 60 * 60 * 24 * 365 * NUM_YEAR_PARTITIONS;
	
	
	private static HourResultsPartitioner hourRP = new HourResultsPartitioner();
	private static DayResultsPartitioner dayRP = new DayResultsPartitioner();
	private static MonthResultsPartitioner monthRP = new MonthResultsPartitioner();
	private static YearResultsPartitioner yearRP = new YearResultsPartitioner();
	
	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList getHour(SearchResults results,
			WaybackRequest wbRequest) {
		return get(hourRP,NUM_HOUR_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList getDay(SearchResults results,
			WaybackRequest wbRequest) {
		return get(dayRP,NUM_DAY_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList getMonth(SearchResults results,
			WaybackRequest wbRequest) {
		return get(monthRP,NUM_MONTH_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList getYear(SearchResults results,
			WaybackRequest wbRequest) {
		return get(yearRP,NUM_YEAR_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList getAuto(SearchResults results,
			WaybackRequest wbRequest) {
		int first = Timestamp.parseBefore(results.getFirstResultDate()).sse();
		int last = Timestamp.parseAfter(results.getLastResultDate()).sse();
		int diff = last - first;
		if(diff < MAX_HOUR_SECONDS) {
			return getHour(results,wbRequest);
		} else if(diff < MAX_DAY_SECONDS) {
			return getDay(results,wbRequest);			
		} else if(diff < MAX_MONTH_SECONDS) {
			return getMonth(results,wbRequest);
		}
		return getYear(results,wbRequest);			
	}

	/**
	 * @param results
	 * @return String Constant of minimum resolution that will hold the results
	 */
	public static String getMinResolution(SearchResults results) {
		int first = Timestamp.parseBefore(results.getFirstResultDate()).sse();
		int last = Timestamp.parseAfter(results.getLastResultDate()).sse();
		int diff = last - first;
		if(diff < MAX_HOUR_SECONDS) {
			return WaybackConstants.REQUEST_RESOLUTION_HOURS;
		} else if(diff < MAX_DAY_SECONDS) {
			return WaybackConstants.REQUEST_RESOLUTION_DAYS;
		} else if(diff < MAX_MONTH_SECONDS) {
			return WaybackConstants.REQUEST_RESOLUTION_MONTHS;
		}
		return WaybackConstants.REQUEST_RESOLUTION_YEARS;
	}
	
	private static ArrayList get(ResultsPartitioner partitioner,
			int partitionCount, SearchResults results, 
			WaybackRequest wbRequest) {
		
		ArrayList partitions = new ArrayList();

		int i; // counter
		int totalPartitions = (partitionCount * 2) + 1; // total # of partitions

		// first calculate the "center" based on the exact request date:
		String reqDate = results.getFilter(WaybackConstants.REQUEST_EXACT_DATE);
		Calendar centerCal = partitioner.dateStrToCalendar(reqDate);
		partitioner.alignStart(centerCal);
		
		// now "decrement" to the first partition:
		Calendar startCal = partitioner.incrementPartition(centerCal,
				partitionCount * -1);
		
		// now "increment", adding as many as needed:
		Calendar endCal = partitioner.incrementPartition(startCal,1);
		for(i = 0; i < totalPartitions; i++) {
			String startDateStr = ArchiveUtils.get14DigitDate(startCal
					.getTime());
			String endDateStr = ArchiveUtils.get14DigitDate(endCal.getTime());
			String title = partitioner.rangeToTitle(startCal,endCal,wbRequest);
			ResultsPartition partition = new ResultsPartition(startDateStr,
					endDateStr, title);
			partition.filter(results);
			partitions.add(partition);

			startCal = endCal;
			endCal = partitioner.incrementPartition(startCal,1);
		}

		return partitions;
	}
}
