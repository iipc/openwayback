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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.graph.Graph;
import org.archive.wayback.util.graph.GraphEncoder;
import org.archive.wayback.util.graph.RegionGraphElement;
import org.archive.wayback.util.partition.Partition;
import org.archive.wayback.util.partition.PartitionSize;
import org.archive.wayback.util.partition.Partitioner;
import org.archive.wayback.util.url.UrlOperations;

/**
 * Support bean for rendering capture calendar.
 * <p>Despite name, this class is generally useful for different calendar styles.</p>
 * <p>Initialize with {@link UIResults} that is set up with {@link CaptureSearchResults}
 * and {@link WaybackRequest}.</p>
 * <p>Caveat: Although fields are all public for historical reasons, please use getters for
 * reading their values.  Field access-level will be changed in the future.</p>
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
	public String lastResultReplayUrl;
	
	private TimeZone calendarTimeZone = TimeZone.getTimeZone("UTC");


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
	/**
	 * default constructor for use in JSP.
	 * be sure to set {@link UIResults}.
	 */
	public BubbleCalendarData() {
	}

	public BubbleCalendarData(UIResults results) {
		setResults(results);
	}

	public void setResults(UIResults results) {
		this.results = results;
		init();
	}

	protected void init() {
		CaptureSearchResults cResults = results.getCaptureResults();
		WaybackRequest wbRequest = results.getWbRequest();
		String searchUrl =
			UrlOperations.stripDefaultPortFromUrl(wbRequest.getRequestUrl());

		searchUrlForHTML = StringEscapeUtils.escapeHtml(searchUrl);
		searchUrlForJS = StringEscapeUtils.escapeJavaScript(searchUrl);
		firstResultDate = cResults.getFirstResultDate();
		firstResultReplayUrl = StringEscapeUtils.escapeHtml(results.resultToReplayUrl(cResults.getResults().getFirst()));
		lastResultDate = cResults.getLastResultDate();
		lastResultReplayUrl = StringEscapeUtils.escapeHtml(results.resultToReplayUrl(cResults.getResults().getLast()));
		Date searchStartDate = wbRequest.getStartDate();
		Date searchEndDate = wbRequest.getEndDate();
		months = capturePartitioner.getRange(monthSize, searchStartDate, searchEndDate);
		years =	partitionPartitioner.getRange(yearSize, searchStartDate, searchEndDate);


		// To build the graph, we need to break all the results into 1 month
		// partitions, so partition all the results into the months:
		capturePartitioner.populate(months, cResults.iterator());

		// To fill in the calendar, we need to break the current year into day
		// sized partitions, so first partition those months into years:
		partitionPartitioner.populate(years, months.iterator());
		// find the active year:
		Partition<Partition<CaptureSearchResult>> activeYear = null;
		for (Partition<Partition<CaptureSearchResult>> year : years) {
			if (year.isContainsClosest()) {
				activeYear = year;
				break;
			}
		}
		// if there's no activeYear, something is quite wrong...
		// TODO: check anyways:
		if (activeYear == null) {
			activeYear = years.get(years.size() - 1);
		}
//		String yearStr = fmt.format("{0,date,yyyy}",activeYear.getStart());
//		yearNum = Integer.parseInt(yearStr);
		Calendar cal = Calendar.getInstance(calendarTimeZone);
		cal.setTime(activeYear.getStart());
		yearNum = cal.get(Calendar.YEAR);

		// now unroll the months in the active year into day-sized partitions:
		List<Partition<CaptureSearchResult>> days =
			capturePartitioner.getRange(daySize,
					activeYear.getStart(),activeYear.getEnd());
		for (Partition<CaptureSearchResult> month : activeYear.list()) {
			capturePartitioner.populate(days,month.iterator());
		}
		// finally, spool the days of the current year into 12 month-sized
		// partitions:
		monthsByDay = partitionPartitioner.getRange(monthSize, activeYear.getStart(), activeYear.getEnd());
		partitionPartitioner.populate(monthsByDay, days.iterator());
		dataStartMSSE = years.get(0).getStart().getTime();
		dataEndMSSE = years.get(years.size() - 1).getEnd().getTime();
		numResults = cResults.getMatchingCount();
	}
	public String getYearsGraphString(int imgWidth, int imgHeight) {
		final String yearFormatKey = "PartitionSize.dateHeader.yearGraphLabel";
		Graph yearGraph = PartitionsToGraph.partsOfPartsToGraph(
				years, results.getFormatter(), yearFormatKey,
				imgWidth, imgHeight);
		for (RegionGraphElement rge : yearGraph.getRegions()) {
			if (rge.getData().hasHighlightedValue()) {
//				rge.getData().setHighlightedValue(-1);
			}
		}

		return GraphEncoder.encode(yearGraph);
	}
	/**
	 * @return Calendar for UTC
	 * @deprecated 1.8.1, use {@code Calendar.getInstance(TimeZone.getTimeZone("UTC"))}.
	 */
	public static Calendar getUTCCalendar() {
		return PartitionsToGraph.getUTCCalendar();
	}

	public String getSearchUrlForJS() {
		return searchUrlForJS;
	}

	public String getSearchUrlForHTML() {
		return searchUrlForHTML;
	}

	public Date getFirstResultDate() {
		return firstResultDate;
	}

	public Date getLastResultDate() {
		return lastResultDate;
	}

	public int getYearNum() {
		return yearNum;
	}

	public long getDataStartMSSE() {
		return dataStartMSSE;
	}

	public long getDataEndMSSE() {
		return dataEndMSSE;
	}

	public long getNumResults() {
		return numResults;
	}

	public String getFirstResultReplayUrl() {
		return firstResultReplayUrl;
	}

	public String getLastResultReplayUrl() {
		return lastResultReplayUrl;
	}

	public List<Partition<Partition<CaptureSearchResult>>> getMonthsByDay() {
		return monthsByDay;
	}

	public int getThisYear() {
		return Calendar.getInstance(calendarTimeZone).get(Calendar.YEAR);
	}

	int month;

	/**
	 * Set month for {@link #getCaptureCalendar()}
	 * @param month integer 0 (January) through 11 (December)
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	public Partition<Partition<CaptureSearchResult>> getMonthPartition() {
		return monthsByDay.get(month);
	}

	/**
	 * @return beginning of the month as {@code Date}
	 * for {@code month}
	 */
	public Date getMonthDate() {
		return monthsByDay.get(month).getStart();
	}

	/**
	 * return {@code CaptureSearchResult} partitions for the month grouped
	 * by week.
	 * Call this method after designating desired month with {@link #setMonth(int)}.
	 * Each week group has exactly seven elements, where out-of-month slots are
	 * {@code null}.
	 * @return Weekly list of {@code Partition}s
	 * @exception IllegalStateException if {@code month} is not in 0..11 range.
	 */
	public List<List<Partition<CaptureSearchResult>>> getCaptureCalendar() {
		if (month < 0 || month > 11)
			throw new IllegalStateException("invalid month");
		Partition<Partition<CaptureSearchResult>> curMonth = monthsByDay.get(month);
		List<Partition<CaptureSearchResult>> monthDays = curMonth.list();

		Calendar cal = Calendar.getInstance(calendarTimeZone);
		cal.setTime(curMonth.getStart());
		// DAY_OF_WEEK field has 1 for SUNDAY. Hence this makes week start Sunday.
		int skipDays = cal.get(Calendar.DAY_OF_WEEK) - 1;

		List<List<Partition<CaptureSearchResult>>> weeks = new ArrayList<List<Partition<CaptureSearchResult>>>();
		List<Partition<CaptureSearchResult>> week = new ArrayList<Partition<CaptureSearchResult>>(7);
		for (int i = 0; i < skipDays; i++) {
			week.add(null);
		}
		for (Partition<CaptureSearchResult> p : monthDays) {
			if (week == null)
				week = new ArrayList<Partition<CaptureSearchResult>>(7);
			week.add(p);
			if (week.size() == 7) {
				weeks.add(week);
				week = null;
			}
		}
		if (week != null) {
			while (week.size() < 7)
				week.add(null);
			weeks.add(week);
		}
		return weeks;
	}
 }
