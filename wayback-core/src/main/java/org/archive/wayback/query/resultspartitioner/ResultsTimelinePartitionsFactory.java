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

import java.util.ArrayList;
import java.util.Calendar;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.Timestamp;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 * @deprecated use org.archive.wayback.util.parition.*
 */
public class ResultsTimelinePartitionsFactory {

	private static int NUM_HOUR_PARTITIONS = 12;
	private static int NUM_DAY_PARTITIONS = 15;
	private static int NUM_MONTH_PARTITIONS = 12;
	private static int NUM_TWO_MONTH_PARTITIONS = 16;
	private static int NUM_YEAR_PARTITIONS = 10;
	
	// These are sort of "ball park" figures. Should be using calendars
	// for better accuracy..
	private static int MAX_HOUR_SECONDS = 2 * 60 * 60 * NUM_HOUR_PARTITIONS;
	private static int MAX_DAY_SECONDS = 2 * 60 * 60 * 24 * NUM_DAY_PARTITIONS;
	private static int MAX_MONTH_SECONDS = 2 * 60 * 60 * 24 * 30 * 
		NUM_MONTH_PARTITIONS;
	private static int MAX_TWO_MONTH_SECONDS = 2 * 60 * 60 * 24 * 2* 30 * 
		NUM_TWO_MONTH_PARTITIONS;
	//private static int MAX_YEAR_SECONDS = 60 * 60 * 24 * 365 * NUM_YEAR_PARTITIONS;
	
	
	private static HourResultsPartitioner hourRP = new HourResultsPartitioner();
	private static DayResultsPartitioner dayRP = new DayResultsPartitioner();
	private static MonthResultsPartitioner monthRP = new MonthResultsPartitioner();
	private static TwoMonthTimelineResultsPartitioner twoMonthRP = new TwoMonthTimelineResultsPartitioner();
	private static YearResultsPartitioner yearRP = new YearResultsPartitioner();
	
	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList<ResultsPartition> getHour(CaptureSearchResults results,
			WaybackRequest wbRequest) {
		return get(hourRP,NUM_HOUR_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList<ResultsPartition> getDay(CaptureSearchResults results,
			WaybackRequest wbRequest) {
		return get(dayRP,NUM_DAY_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList<ResultsPartition> getMonth(CaptureSearchResults results,
			WaybackRequest wbRequest) {
		return get(monthRP,NUM_MONTH_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList<ResultsPartition> getTwoMonth(CaptureSearchResults results,
			WaybackRequest wbRequest) {
		return get(twoMonthRP,NUM_TWO_MONTH_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList<ResultsPartition> getYear(CaptureSearchResults results,
			WaybackRequest wbRequest) {
		return get(yearRP,NUM_YEAR_PARTITIONS,results,wbRequest);
	}

	/**
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList<ResultsPartition> getAuto(CaptureSearchResults results,
			WaybackRequest wbRequest) {
		int first = Timestamp.parseBefore(results.getFirstResultTimestamp()).sse();
		int last = Timestamp.parseAfter(results.getLastResultTimestamp()).sse();
		int diff = last - first;
		if(diff < MAX_HOUR_SECONDS) {
			return getHour(results,wbRequest);
		} else if(diff < MAX_DAY_SECONDS) {
			return getDay(results,wbRequest);			
		} else if(diff < MAX_MONTH_SECONDS) {
			return getMonth(results,wbRequest);
		} else if(diff < MAX_TWO_MONTH_SECONDS) {
			return getTwoMonth(results,wbRequest);
		}
		return getYear(results,wbRequest);			
	}

	/**
	 * @param results
	 * @return String Constant of minimum resolution that will hold the results
	 */
	public static String getMinResolution(CaptureSearchResults results) {
		int first = Timestamp.parseBefore(results.getFirstResultTimestamp()).sse();
		int last = Timestamp.parseAfter(results.getLastResultTimestamp()).sse();
		int diff = last - first;
		if(diff < MAX_HOUR_SECONDS) {
			return WaybackRequest.REQUEST_RESOLUTION_HOURS;
		} else if(diff < MAX_DAY_SECONDS) {
			return WaybackRequest.REQUEST_RESOLUTION_DAYS;
		} else if(diff < MAX_MONTH_SECONDS) {
			return WaybackRequest.REQUEST_RESOLUTION_MONTHS;
		} else if(diff < MAX_TWO_MONTH_SECONDS) {
			return WaybackRequest.REQUEST_RESOLUTION_TWO_MONTHS;
		}
		return WaybackRequest.REQUEST_RESOLUTION_YEARS;
	}
	
	private static ArrayList<ResultsPartition> get(ResultsPartitioner 
			partitioner, int partitionCount, CaptureSearchResults results, 
			WaybackRequest wbRequest) {
		
		ArrayList<ResultsPartition> partitions = 
			new ArrayList<ResultsPartition>();

		int i; // counter
		int totalPartitions = (partitionCount * 2) + 1; // total # of partitions

		// first calculate the "center" based on the exact request date:
		String reqDate = results.getFilter(WaybackRequest.REQUEST_EXACT_DATE);
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
