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
import java.util.Date;

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
public class ResultsPartitionsFactory {

	static ResultsPartitioner partitioners[] = {new HourResultsPartitioner(),
			new DayResultsPartitioner(),
			new WeekResultsPartitioner(),
			new MonthResultsPartitioner(),
			new TwoMonthResultsPartitioner(),
			new YearResultsPartitioner() };
	
	/**
	 * Determine the correct ResultsPartitioner to use given the SearchResults
	 * search range, and use that to break the SearchResults into partitions.
	 * @param results
	 * @param wbRequest 
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList<ResultsPartition> get(CaptureSearchResults results,
			WaybackRequest wbRequest) {
		return get(results, wbRequest, null);
	}

	public static ArrayList<ResultsPartition> get(CaptureSearchResults results,
				WaybackRequest wbRequest, ResultsPartitioner defaultPartitioner) {	
		Timestamp startTS = Timestamp.parseBefore(results.getFilter(
				WaybackRequest.REQUEST_START_DATE));
		Timestamp endTS = Timestamp.parseAfter(results.getFilter(
				WaybackRequest.REQUEST_END_DATE));
		
		String rsd = startTS.getDateStr();
		String red = endTS.getDateStr();

		Date startDate = startTS.getDate();
		Date endDate = endTS.getDate();

		long msSpanned = endDate.getTime() - startDate.getTime();
		int secsSpanned = (int) Math.ceil(msSpanned / 1000);

		ResultsPartitioner partitioner = defaultPartitioner;
		
		if (partitioner == null) {
			for(int i = 0; i < partitioners.length; i++) {
				partitioner = partitioners[i];
				if(partitioner.maxSecondsSpanned() >= secsSpanned) {
					break;
				}
			}
		}
		
		// now use the partitioner to initialize and populate the 
		// ResultPartition objects:
		ArrayList<ResultsPartition> partitions = 
			new ArrayList<ResultsPartition>();

		Calendar startCal = partitioner.dateStrToCalendar(rsd);
		Calendar lastCal = partitioner.dateStrToCalendar(red);

		partitioner.alignStart(startCal);
		Calendar endCal = partitioner.incrementPartition(startCal,1);
		while (true) {
			String startDateStr = ArchiveUtils.get14DigitDate(startCal
					.getTime());
			String endDateStr = ArchiveUtils.get14DigitDate(endCal.getTime());
			String title = partitioner.rangeToTitle(startCal, endCal, 
					wbRequest);
			ResultsPartition partition = new ResultsPartition(startDateStr,
					endDateStr, title);
			partition.filter(results);
			partitions.add(partition);

			if (endCal.after(lastCal)) {
				break;
			}
			startCal = endCal;
			endCal = partitioner.incrementPartition(startCal,1);
		}
		return partitions;
	}
}
