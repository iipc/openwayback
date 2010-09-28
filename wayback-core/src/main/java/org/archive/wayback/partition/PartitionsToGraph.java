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
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.graph.Graph;
import org.archive.wayback.util.graph.GraphConfiguration;
import org.archive.wayback.util.graph.RegionData;
import org.archive.wayback.util.partition.Partition;
import org.archive.wayback.util.partition.PartitionSize;
import org.archive.wayback.util.partition.Partitioner;

/**
 * @author brad
 *
 */
public class PartitionsToGraph {

	public final static int NAV_COUNT        = 9;
	
	public final static int NAV_PREV_YEAR    = 0;
	public final static int NAV_PREV_MONTH   = 1;
	public final static int NAV_PREV_DAY     = 2;
	public final static int NAV_PREV_CAPTURE = 3;
	public final static int NAV_CURRENT      = 4;
	public final static int NAV_NEXT_CAPTURE = 5;
	public final static int NAV_NEXT_DAY     = 6;
	public final static int NAV_NEXT_MONTH   = 7;
	public final static int NAV_NEXT_YEAR    = 8;


	private static final TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");

//	private static String joinInts(int[] a) {
//		StringBuilder sb = new StringBuilder();
//		boolean first = true;
//		for(int i : a) {
//			if(first) {
//				sb.append(i);
//				first = false;
//			} else {
//				sb.append(",").append(i);
//			}
//		}
//		return sb.toString();
//	}
//	private static void printAr(String name, int o[], int n[]) {
//		System.out.format("%s=========\nORIG(%s)\nNORM(%s)\n",
//				name,joinInts(o),joinInts(n));
//	}

	private static int normalizeInt(int input, int localMax, int maxOutput) {
		double ln = Math.log(localMax);
		if(input == 0) {
			return 0;
			
		} else if(input == 1) {
			return 1;
		} else {
			double iln = Math.log(input);
			double pct = iln / ln;
			double num = pct * maxOutput;
			int idx = (int) num;
//			System.out.format("%d - %f - %f - %f - %f : %d\n",
//					input,ln,iln,pct,num,idx);
			if(input < idx) {
				return input;
			} else {
				return idx;
			}
		}
	}

	private static int[] normalizeTo(int input[], int max) {
		int localMax = -1;
		for(int i = 0; i < input.length; i++) {
			if(input[i] > localMax) localMax = input[i];
		}
		if(localMax < max) {
//			printAr("No normalization",input,input);
			return input;
		}
		int normalized[] = new int[input.length];
		double ln = Math.log(localMax);
		for(int i = 0; i < input.length; i++) {
			if(input[i] == 0) {
				normalized[i] = 0;
			} else if(input[i] == 1) {
				normalized[i] = 1;
			} else {
				double iln = Math.log(input[i]);
				double pct = iln / ln;
				double num = pct * max;
				int idx = (int) num;
//				System.out.format("%d - %d - %f - %f - %f - %f : %d\n",
//						i,input[i],ln,iln,pct,num,idx);
				if(input[i] < idx) {
					normalized[i] = input[i];
				} else {
					normalized[i] = idx;
				}
			}
		}
//		printAr("NORMALIZED",input,normalized);
		return normalized;
	}

	public static Calendar getUTCCalendar() {
		return Calendar.getInstance(TZ_UTC);
	}
	
	public static Graph partsOfPartsToGraph(List<Partition<Partition<CaptureSearchResult>>> ppcs,
			StringFormatter formatter, String formatKey, int width, int height) {
		
		Calendar cal = Calendar.getInstance(TZ_UTC);
		// FIRST PASS TO CALCULATE MONTH DOMAIN MAX:
		int maxValue = -1;
		for(Partition<Partition<CaptureSearchResult>> ppc : ppcs) {
			for(Partition<CaptureSearchResult> pc : ppc.list()) {
				if(pc.getTotal() > maxValue) {
					maxValue = pc.getTotal();
				}
			}
		}

		RegionData data[] = new RegionData[ppcs.size()];
		for(int y = 0; y < ppcs.size(); y++) {
			int activeP = -1;
			Partition<Partition<CaptureSearchResult>> ppc = ppcs.get(y);
			String label = formatter.format(formatKey, ppc.getStart());
//			cal.setTime(ppc.getStart());
//			String label = String.valueOf(cal.get(Calendar.YEAR));
			List<Partition<CaptureSearchResult>> pcs = ppc.list();
			int count = pcs.size();
			int values[] = new int[count];
			for(int m = 0; m < count; m++) {
				Partition<CaptureSearchResult> pc = pcs.get(m);
				values[m] = normalizeInt(pc.getTotal(), maxValue, 15);
				if(pc.isContainsClosest()) {
					activeP = m;
				}
			}
			data[y] = new RegionData(label, activeP, values);
		}
		GraphConfiguration config = new GraphConfiguration();
		return new Graph(width, height, data, config);
	}

	public static List<Partition<Partition<CaptureSearchResult>>> reversePartsOfParts(List<Partition<Partition<CaptureSearchResult>>> parts) {
		List<Partition<Partition<CaptureSearchResult>>> reversed = 
			new ArrayList<Partition<Partition<CaptureSearchResult>>>();
		for(Partition<Partition<CaptureSearchResult>> p : parts) {
			reversed.add(0, p);
		}
		return reversed;
	}
	public static List<Partition<CaptureSearchResult>> reversePartsOfCaps(List<Partition<CaptureSearchResult>> parts) {
		List<Partition<CaptureSearchResult>> reversed = 
			new ArrayList<Partition<CaptureSearchResult>>();
		for(Partition<CaptureSearchResult> p : parts) {
			reversed.add(0, p);
		}
		return reversed;
	}
	public static List<CaptureSearchResult> reverseCaps(List<CaptureSearchResult> caps) {
		List<CaptureSearchResult> reversed = 
			new ArrayList<CaptureSearchResult>();
		for(CaptureSearchResult cap : caps) {
			reversed.add(0, cap);
		}
		return reversed;
	}
	
	public static List<Partition<Partition<CaptureSearchResult>>> trimPartsOfParts(List<Partition<Partition<CaptureSearchResult>>> parts) {
		int first = -1;
		int last = -1;
		for(int i = 0; i < parts.size(); i++) {
			Partition<Partition<CaptureSearchResult>> p = parts.get(i);
			if(p.getTotal() > 0) {
				if(first == -1) {
					first = i;
				}
				last = i;
			}
		}
		return parts.subList(first, last+1);
	}
	public static List<Partition<CaptureSearchResult>> trimPartsOfCaps(List<Partition<CaptureSearchResult>> parts) {
		int first = -1;
		int last = -1;
		for(int i = 0; i < parts.size(); i++) {
			Partition<CaptureSearchResult> p = parts.get(i);
			if(p.getTotal() > 0) {
				if(first == -1) {
					first = i;
				}
				last = i;
			}
		}
		return parts.subList(first, last+1);
	}
	
	public static Graph convertYearMonth(List<Partition<Partition<CaptureSearchResult>>> years,
			int width, int height) {
		
		Calendar cal = Calendar.getInstance(TZ_UTC);
		// FIRST PASS TO CALCULATE MONTH DOMAIN MAX:
		int maxValue = -1;
		for(Partition<Partition<CaptureSearchResult>> year : years) {
			for(Partition<CaptureSearchResult> month : year.list()) {
				if(month.getTotal() > maxValue) {
					maxValue = month.getTotal();
				}
			}
		}

		RegionData yearRD[] = new RegionData[years.size()];
		for(int y = 0; y < years.size(); y++) {
			int activeMonth = -1;
			Partition<Partition<CaptureSearchResult>> year = years.get(y);
			cal.setTime(year.getStart());
			String label = String.valueOf(cal.get(Calendar.YEAR));
			List<Partition<CaptureSearchResult>> months = year.list();
			if(months.size() != 12) {
				throw new RuntimeException("Not 12 months...");
			}
			int values[] = new int[12];
			for(int m = 0; m < 12; m++) {
				Partition<CaptureSearchResult> month = months.get(m);
				values[m] = normalizeInt(month.getTotal(), maxValue, 15);
				if(month.isContainsClosest()) {
					activeMonth = m;
				}
			}
			yearRD[y] = new RegionData(label, activeMonth, values);
		}
		GraphConfiguration config = new GraphConfiguration();
		return new Graph(width, height, yearRD, config);
	}

	public static String[] getTitles(CaptureSearchResult results[],
			StringFormatter formatter, String property) {
		int len = results.length;
		String urls[] = new String[len];
		for(int i = 0; i < len; i++) {
			String url = null;
			if(results[i] != null) {
				url = formatter.format(property, results[i].getCaptureDate());
			}
			urls[i] = url;
		}
		return urls;
	}
	public static String[] getUrls(CaptureSearchResult results[], String suffix,
			ResultURIConverter c) {
		int len = results.length;
		String urls[] = new String[len];
		for(int i = 0; i < len; i++) {
			String url = null;
			if(results[i] != null) {
				if(suffix == null) {
					url = c.makeReplayURI(results[i].getCaptureTimestamp(),
							results[i].getOriginalUrl());
				} else {
					url = c.makeReplayURI(results[i].getCaptureTimestamp() + 
							suffix, results[i].getOriginalUrl());
				}
			}
			urls[i] = url;
		}
		return urls;
	}
	public static String[] getUrls(CaptureSearchResult results[],
			ResultURIConverter c) {
		return getUrls(results,null,c);
	}

	public static CaptureSearchResult[] getResults(
			List<Partition<Partition<CaptureSearchResult>>> years) {

		int count = years.size();
		CaptureSearchResult results[] = new CaptureSearchResult[count];
		for(int i = 0; i < count; i++) {
			Partition<Partition<CaptureSearchResult>> year = years.get(i);
			CaptureSearchResult first = null;
			if(year.getTotal() > 0) {
				for(Partition<CaptureSearchResult> month : year.list()) {
					if(month.getTotal() > 0) {
						first = month.list().get(0);
						break;
					}
				}
			}
			results[i] = first;
		}
		return results;
	}

	

	public static String getFirstUrlMonth(Partition<CaptureSearchResult> month,
			ResultURIConverter c) {
		if(month.getTotal() > 0) {
			CaptureSearchResult first = month.list().get(0);
			return c.makeReplayURI(first.getCaptureTimestamp(),
					first.getOriginalUrl());
		}
		return null;
	}

	public static String getLastUrlMonth(Partition<CaptureSearchResult> month,
			ResultURIConverter c) {
		if(month.getTotal() > 0) {
			CaptureSearchResult last = month.list().get(month.list().size()-1);
			return c.makeReplayURI(last.getCaptureTimestamp(),
					last.getOriginalUrl());
		}
		return null;
	}

	public static String getFirstUrlYear(Partition<Partition<CaptureSearchResult>> year,
			ResultURIConverter c) {
		for(Partition<CaptureSearchResult> month : year.list()) {
			String firstInMonth = getFirstUrlMonth(month,c);
			if(firstInMonth != null) return firstInMonth;
		}
		return null;
	}
	public static String getLastUrlYear(Partition<Partition<CaptureSearchResult>> year,
			ResultURIConverter c) {
		List<Partition<CaptureSearchResult>> months = year.list();
		for(int i = months.size()-1; i >= 0; i--) {
			String firstInMonth = getLastUrlMonth(months.get(i),c);
			if(firstInMonth != null) return firstInMonth;
		}
		return null;
	}
	
	
	public static String[] getNavigatorLinks(
			List<Partition<Partition<CaptureSearchResult>>> years, ResultURIConverter uc) {
		String navs[] = new String[NAV_COUNT];
		for(int i = 0; i < NAV_COUNT; i++) {
			navs[i] = null;
		}
		/*
		 * first traverse the years, grabbing:
		 *  * the one *before*
		 *  * the *current*
		 *  * the one *after*
		 *  
		 * the "active" year:
		 */
		
		Partition<Partition<CaptureSearchResult>> prevYear = null;
		Partition<Partition<CaptureSearchResult>> curYear = null;
		Partition<Partition<CaptureSearchResult>> nextYear = null;
		
		for(Partition<Partition<CaptureSearchResult>> year : years) {
			if(year.isContainsClosest()) {
				curYear = year;
			} else {
				// have we seen the current one?
				if(curYear == null) {
					// no, track this as the "prev", and continue:
					if(year.getTotal() > 0) {
						prevYear = year;
					}
				} else {
					// yes, this is the "next", remember and break:
					if(year.getTotal() > 0) {
						nextYear = year;
						break;
					}
				}
			}
		}
		if(prevYear != null) {
			navs[NAV_PREV_YEAR] = getLastUrlYear(prevYear, uc);
		}
		if(nextYear != null) {
			navs[NAV_NEXT_YEAR] = getFirstUrlYear(nextYear, uc);
		}
		// now on to months:
		List<Partition<CaptureSearchResult>> months = curYear.list();

		Partition<CaptureSearchResult> prevMonth = null;
		Partition<CaptureSearchResult> curMonth = null;
		Partition<CaptureSearchResult> nextMonth = null;

		for(Partition<CaptureSearchResult> month : months) {
			if(month.isContainsClosest()) {
				curMonth = month;
			} else {
				// have we seen the current one?
				if(curMonth == null) {
					// no, track this as the "prev", and continue:
					if(month.getTotal() > 0) {
						prevMonth = month;
					}
				} else {
					// yes, this is the "next", remember and break:
					if(month.getTotal() > 0) {
						nextMonth = month;
						break;
					}
				}
			}
		}
		
		if(prevMonth != null) {
			navs[NAV_PREV_MONTH] = getLastUrlMonth(prevMonth, uc);
		} else {
			// assume whatever we found for prev Year is OK, even if null:
			navs[NAV_PREV_MONTH]  = navs[NAV_PREV_YEAR];
		}
		if(nextMonth != null) {
			navs[NAV_NEXT_MONTH] = getFirstUrlMonth(nextMonth, uc);
		} else {
			// assume whatever we found for next Year is OK, even if null:
			navs[NAV_NEXT_MONTH]  = navs[NAV_NEXT_YEAR];
		}
		
		// OK... now we're down to days... split the current month into days:
		List<Partition<CaptureSearchResult>> days = splitToDays(curMonth);

		Partition<CaptureSearchResult> prevDay = null;
		Partition<CaptureSearchResult> curDay = null;
		Partition<CaptureSearchResult> nextDay = null;
		
		for(Partition<CaptureSearchResult> day : days) {
			if(day.isContainsClosest()) {
				curDay = day;
			} else {
				// have we seen the current one?
				if(curDay == null) {
					// no, track this as the "prev", and continue:
					if(day.getTotal() > 0) {
						prevDay = day;
					}
				} else {
					// yes, this is the "next", remember and break:
					if(day.getTotal() > 0) {
						nextDay = day;
						break;
					}
				}
			}
		}
		
		if(prevDay != null) {
			navs[NAV_PREV_DAY] = getLastUrlMonth(prevDay, uc);
		} else {
			// assume whatever we found for prev Month is OK, even if null:
			navs[NAV_PREV_DAY]  = navs[NAV_PREV_MONTH];
		}
		if(nextDay != null) {
			navs[NAV_NEXT_DAY] = getFirstUrlMonth(nextDay, uc);
		} else {
			// assume whatever we found for next Month is OK, even if null:
			navs[NAV_NEXT_DAY]  = navs[NAV_NEXT_MONTH];
		}	
		
		// FINALLY! We just need the next and prev links:
		CaptureSearchResult prevResult = null;
		CaptureSearchResult curResult = null;
		CaptureSearchResult nextResult = null;
		for(CaptureSearchResult result : curDay.list()) {
			if(result.isClosest()) {
				curResult = result;
			} else {
				// have we seen the current one?
				if(curResult == null) {
					// no, track this as the "prev", and continue:
					prevResult = result;
				} else {
					// yes, this is the "next", remember and break:
					nextResult = result;
					break;
				}
			}
		}
		if(prevResult != null) {
			navs[NAV_PREV_CAPTURE] = 
				uc.makeReplayURI(prevResult.getCaptureTimestamp(), 
						prevResult.getOriginalUrl());
		} else {
			// assume whatever we found for prev Day is OK, even if null:
			navs[NAV_PREV_CAPTURE] = navs[NAV_PREV_DAY];
		}
		if(nextResult != null) {
			navs[NAV_NEXT_CAPTURE] = 
				uc.makeReplayURI(nextResult.getCaptureTimestamp(), 
						nextResult.getOriginalUrl());
		} else {
			// assume whatever we found for prev Day is OK, even if null:
			navs[NAV_NEXT_CAPTURE] = navs[NAV_NEXT_DAY];
		}

		return navs;
	}

	/**
	 * @param list
	 * @return
	 */
	private static List<Partition<CaptureSearchResult>> splitToDays(
			Partition<CaptureSearchResult> month) {
		CaptureSearchResultPartitionMap map = 
			new CaptureSearchResultPartitionMap();
		Partitioner<CaptureSearchResult> partitioner = 
			new Partitioner<CaptureSearchResult>(map);
		PartitionSize daySize = Partitioner.daySize;
		List<Partition<CaptureSearchResult>> days = partitioner.getRange(daySize, 
				month.getStart(), month.getEnd());
		partitioner.populate(days, month.iterator());
		return days;
	}

	public static String[] getNavigators(StringFormatter formatter, CaptureSearchResult current) {
		String navs[] = new String[NAV_COUNT];
		navs[NAV_PREV_YEAR]    = formatter.format("graph.prevYear");
		navs[NAV_PREV_MONTH]   = formatter.format("graph.prevMonth");
		navs[NAV_PREV_DAY]     = formatter.format("graph.prevDay");
		navs[NAV_PREV_CAPTURE] = formatter.format("graph.prevCapture");

		navs[NAV_CURRENT] = formatter.format("graph.current", current.getOriginalUrl(),
				current.getCaptureDate());

		navs[NAV_NEXT_CAPTURE] = formatter.format("graph.nextCapture");
		navs[NAV_NEXT_DAY]     = formatter.format("graph.nextDay");
		navs[NAV_NEXT_MONTH]   = formatter.format("graph.nextMonth");
		navs[NAV_NEXT_YEAR]    = formatter.format("graph.nextYear");
		return navs;
	}
}
