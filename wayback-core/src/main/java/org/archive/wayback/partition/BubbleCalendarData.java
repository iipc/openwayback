/* BubbleCalendarData
 *
 * $Id$:
 *
 * Created on Jun 15, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
