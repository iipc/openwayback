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
package org.archive.wayback.partition;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.util.graph.Graph;
import org.archive.wayback.util.graph.GraphEncoder;
import org.archive.wayback.util.graph.RegionGraphElement;
import org.archive.wayback.util.partition.Partition;
import org.archive.wayback.util.partition.PartitionSize;
import org.archive.wayback.util.partition.Partitioner;
import org.archive.wayback.util.url.UrlOperations;

/**
 * @author brad
 *
 */
public class BubbleCalendarData {
	public UIResults results;
	public String searchUrlForJS;
	public String searchUrlForHTML;
	public Date searchStartDate;
	public Date searchEndDate;
	public Date firstResultDate;
	public Date lastResultDate;
	public int yearNum = 0;
	public long dataStartMSSE;
	public long dataEndMSSE;
	public long numResults = 0;
	public String firstResultReplayUrl;
	

	private List<Partition<CaptureSearchResult>> months; 
	private List<Partition<Partition<CaptureSearchResult>>> years; 
	public List<Partition<Partition<CaptureSearchResult>>> monthsByDay;
	
	static PartitionSize daySize = Partitioner.daySize;
	static PartitionSize monthSize = Partitioner.monthSize;
	static PartitionSize yearSize = Partitioner.yearSize;

	static CaptureSearchResultPartitionMap captureMap = 
		new CaptureSearchResultPartitionMap();
	static PartitionPartitionMap partitionMap = 
		new PartitionPartitionMap();

	static Partitioner<CaptureSearchResult> capturePartitioner = 
		new Partitioner<CaptureSearchResult>(captureMap);

	static Partitioner<Partition<CaptureSearchResult>> partitionPartitioner = 
		new Partitioner<Partition<CaptureSearchResult>>(partitionMap);
	
	public BubbleCalendarData(UIResults results) {
		this.results = results;
		CaptureSearchResults cResults = results.getCaptureResults();
		WaybackRequest wbRequest = results.getWbRequest();
		StringFormatter fmt = wbRequest.getFormatter();
		String searchUrl = 
			UrlOperations.stripDefaultPortFromUrl(wbRequest.getRequestUrl());

		searchUrlForHTML = fmt.escapeHtml(searchUrl);
		searchUrlForJS = fmt.escapeJavaScript(searchUrl);
		firstResultDate = cResults.getFirstResultDate();
		firstResultReplayUrl = fmt.escapeHtml(
				results.resultToReplayUrl(cResults.getResults().get(0)));
		lastResultDate = cResults.getLastResultDate();
		Date searchStartDate = wbRequest.getStartDate();
		Date searchEndDate = wbRequest.getEndDate();
		months = capturePartitioner.getRange(monthSize,searchStartDate,searchEndDate);
		years =	partitionPartitioner.getRange(yearSize,searchStartDate,searchEndDate);

		
		// To build the graph, we need to break all the results into 1 month
		// partitions, so partition all the results into the months:
		capturePartitioner.populate(months,cResults.iterator());
		
		// To fill in the calendar, we need to break the current year into day
		// sized partitions, so first partition those months into years:
		partitionPartitioner.populate(years,months.iterator());
		// find the active year:
		Partition<Partition<CaptureSearchResult>> activeYear = null;
		for(Partition<Partition<CaptureSearchResult>> year : years) {
			if(year.isContainsClosest()) {
				activeYear = year;
				break;
			}
		}
		// if there's no activeYear, something is quite wrong...
		// TODO: check anyways:
		String yearStr = fmt.format("{0,date,yyyy}",activeYear.getStart());
		yearNum = Integer.parseInt(yearStr);

		// now unroll the months in the active year into day-sized partitions:
		List<Partition<CaptureSearchResult>> days = 
			capturePartitioner.getRange(daySize,
					activeYear.getStart(),activeYear.getEnd());
		for(Partition<CaptureSearchResult> month : activeYear.list()) {
			capturePartitioner.populate(days,month.iterator());
		}
		// finally, spool the days of the current year into 12 month-sized
		// partitions:
		monthsByDay = partitionPartitioner.getRange(monthSize, activeYear.getStart(), activeYear.getEnd());
		partitionPartitioner.populate(monthsByDay,days.iterator());
		dataStartMSSE = years.get(0).getStart().getTime();
		dataEndMSSE = years.get(years.size()-1).getEnd().getTime();
		numResults = cResults.getMatchingCount();
	}
	public String getYearsGraphString(int imgWidth, int imgHeight) {
		String yearFormatKey = "PartitionSize.dateHeader.yearGraphLabel";
		Graph yearGraph = PartitionsToGraph.partsOfPartsToGraph(
				years,results.getWbRequest().getFormatter(),yearFormatKey,
				imgWidth,imgHeight);
		for(RegionGraphElement rge : yearGraph.getRegions()) {
			if(rge.getData().hasHighlightedValue()) {
//				rge.getData().setHighlightedValue(-1);
			}
		}

		return GraphEncoder.encode(yearGraph);
	}
	public static Calendar getUTCCalendar() {
		return PartitionsToGraph.getUTCCalendar();
	}
}
