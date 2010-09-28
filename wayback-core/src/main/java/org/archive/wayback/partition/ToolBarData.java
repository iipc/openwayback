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
import java.util.Iterator;
import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.util.graph.Graph;
import org.archive.wayback.util.graph.GraphEncoder;
import org.archive.wayback.util.partition.Partition;
import org.archive.wayback.util.partition.PartitionSize;
import org.archive.wayback.util.partition.Partitioner;


/**
 * @author brad
 *
 */
public class ToolBarData {
	
	private UIResults uiResults;
	private StringFormatter fmt;
	
	/** Latest Result one year before current, or null */
	public CaptureSearchResult yearPrevResult;
	/** Latest Result one month before current, or null */
	public CaptureSearchResult monthPrevResult;
	/** Latest Result before current, or null */
	public CaptureSearchResult prevResult;
	/** Earliest Result after current, or null */
	public CaptureSearchResult nextResult;
	/** Earliest Result one month after current, or null */
	public CaptureSearchResult monthNextResult;
	/** Earliest Result one year after current, or null */
	public CaptureSearchResult yearNextResult;

	/** current result being shown */
	public CaptureSearchResult curResult;
	/** the CaptureSearchResults object from the ResourceIndex. */
	public CaptureSearchResults results;
	/** List<Part<Part<CResult>>> for years*/
	public List<Partition<Partition<CaptureSearchResult>>> yearPartitions;
	/** List<Part<CResult>> for months*/
	public List<Partition<CaptureSearchResult>> monthPartitions;
	
	private static final PartitionSize yearSize = Partitioner.yearSize;
	private static final PartitionSize monthSize = Partitioner.monthSize;

	private static final CaptureSearchResultPartitionMap monthMap = 
		new CaptureSearchResultPartitionMap();
	private static final PartitionPartitionMap yearMap = 
		new PartitionPartitionMap();
	
	private static final Partitioner<Partition<CaptureSearchResult>> 
		yearPartitioner = 
			new Partitioner<Partition<CaptureSearchResult>>(yearMap);
	private static final Partitioner<CaptureSearchResult> monthPartitioner = 
		new Partitioner<CaptureSearchResult>(monthMap);

	/**
	 * @param uiResults the UIResults holding replay info
	 */
	public ToolBarData(UIResults uiResults) {
		this.uiResults = uiResults;
		fmt = uiResults.getWbRequest().getFormatter();
		results = uiResults.getCaptureResults();
		curResult = uiResults.getResult();
		findRelativeLinks();
		Date firstDate = uiResults.getWbRequest().getStartDate();
		Date lastDate = uiResults.getWbRequest().getEndDate();

		yearPartitions = yearPartitioner.getRange(yearSize,firstDate,lastDate);

		Date firstYearDate = yearPartitions.get(0).getStart();
		Date lastYearDate = yearPartitions.get(yearPartitions.size()-1).getEnd();

		monthPartitions =
			monthPartitioner.getRange(monthSize,firstYearDate,lastYearDate);

		Iterator<CaptureSearchResult> it = results.iterator();

		monthPartitioner.populate(monthPartitions,it);
		yearPartitioner.populate(yearPartitions,monthPartitions.iterator());
	
	}

	/**
	 * @param formatKey String template for format Dates
	 * @param width pixel width of resulting graph
	 * @param height pixel height of resulting graph
	 * @return String argument which will generate a graph for the results
	 */
	public String computeGraphString(String formatKey, int width, int height) {
		Graph graph = PartitionsToGraph.partsOfPartsToGraph(yearPartitions,
				fmt,formatKey,width,height);
		return GraphEncoder.encode(graph);

	}
	/**
	 * @param result Restul to draw replay URL for
	 * @return String absolute URL that will replay result
	 */
	public String makeReplayURL(CaptureSearchResult result) {
		return fmt.escapeHtml(uiResults.getURIConverter().makeReplayURI(
				result.getCaptureTimestamp(), result.getOriginalUrl()));
	}
	
	/**
	 * @return the total number of results
	 */
	public long getResultCount() {
		return uiResults.getCaptureResults().getReturnedCount();
	}
	/**
	 * @return the Date of the first capture in the result set
	 */
	public Date getFirstResultDate() {
		return uiResults.getCaptureResults().getFirstResultDate();
	}
	/**
	 * @return the Date of the last capture in the result set
	 */
	public Date getLastResultDate() {
		return uiResults.getCaptureResults().getLastResultDate();
	}
	

	private static Date addDateField(Date date, int field, int amt) {
		Calendar c = PartitionsToGraph.getUTCCalendar();
		c.setTime(date);
		c.add(field, amt);
		return c.getTime();
	}	
	/**
	 * Increment a Date object by +/- some years
	 * @param date Date to +/- some years from
	 * @param amt number of years to add/remove
	 * @return new Date object offset by the indicated years
	 */
	public static Date addYear(Date date, int amt) {
		return addDateField(date,Calendar.YEAR,amt);
	}
	/**
	 * Increment a Date object by +/- some months
	 * @param date Date to +/- some months from
	 * @param amt number of months to add/remove
	 * @return new Date object offset by the indicated months
	 */
	public static Date addMonth(Date date, int amt) {
		return addDateField(date,Calendar.MONTH,amt);
	}
	/**
	 * Increment a Date object by +/- some days
	 * @param date Date to +/- some days from
	 * @param amt number of days to add/remove
	 * @return new Date object offset by the indicated days
	 */
	public static Date addDay(Date date, int amt) {
		return addDateField(date,Calendar.DATE,amt);
	}
	private void findRelativeLinks() {
		Date cur = curResult.getCaptureDate();
		
		Date minYear = addYear(cur,-1);
		Date minMonth = addMonth(cur,-1);
		Date addYear = addYear(cur,1);
		Date addMonth = addMonth(cur,1);
		Iterator<CaptureSearchResult> itr = results.iterator();
		while(itr.hasNext()) {
			CaptureSearchResult result = itr.next();
			Date d = result.getCaptureDate();
			if(d.compareTo(cur) < 0) {
				prevResult = result;
				if(d.compareTo(minMonth) < 0) {
					monthPrevResult = result;
				}
				if(d.compareTo(minYear) < 0 ) {
					yearPrevResult = result;
				}
			} else if(d.compareTo(cur) > 0) {
				if(nextResult == null) {
					nextResult = result;
				}
				if(d.compareTo(addYear) > 0) {
					if(yearNextResult == null) {
						yearNextResult = result;
					}
				}
				if(d.compareTo(addMonth) > 0) {
					if(monthNextResult == null) {
						monthNextResult = result;
					}
				}
			}
		}
	}
}
