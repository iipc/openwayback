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
package org.archive.wayback.util.partition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.archive.wayback.util.partition.size.DayPartitionSize;
import org.archive.wayback.util.partition.size.HourPartitionSize;
import org.archive.wayback.util.partition.size.MonthPartitionSize;
import org.archive.wayback.util.partition.size.TwoMonthPartitionSize;
import org.archive.wayback.util.partition.size.TwoYearPartitionSize;
import org.archive.wayback.util.partition.size.WeekPartitionSize;
import org.archive.wayback.util.partition.size.YearPartitionSize;

/**
 * Class which divides a set of date-related objects into sub-sets by time 
 * ranges.
 * 
 * This class provides methods for:
 * 
 * 1) determining the smallest PartitionSize that can be used to cover a time 
 * range, using at most a set number of partitions
 * 2) creating a List of Partition objects covering a span of time, each having
 * a specified size
 * 3) efficiently populating an iterator of date-related objects into List of 
 * Partition objects 
 * 
 * @author brad
 *
 * @param <T> generic class type to use with this Partitioner
 */
public class Partitioner<T> {

	private static final TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
	private static final Logger LOGGER = Logger.getLogger(
			Partitioner.class.getName());

	private ElementPartitionMap<T> map = null;
	
	/**
	 * PartitionSize based on Hour intervals
	 */
	public static PartitionSize hourSize     = new HourPartitionSize();
	/**
	 * PartitionSize based on Day intervals
	 */
	public static PartitionSize daySize      = new DayPartitionSize();
	/**
	 * PartitionSize based on Week intervals
	 */
	public static PartitionSize weekSize     = new WeekPartitionSize();
	/**
	 * PartitionSize based on Month intervals
	 */
	public static PartitionSize monthSize    = new MonthPartitionSize();
	/**
	 * PartitionSize based on Two Month intervals
	 */
	public static PartitionSize twoMonthSize = new TwoMonthPartitionSize();
	/**
	 * PartitionSize based on Year intervals
	 */
	public static PartitionSize yearSize     = new YearPartitionSize();
	/**
	 * PartitionSize based on Two Year intervals
	 */
	public static PartitionSize twoYearSize  = new TwoYearPartitionSize();

	private static PartitionSize[] sizes = {
			hourSize,
			daySize,
			weekSize,
			monthSize,
			twoMonthSize,
			yearSize,
			twoYearSize
	};

	/**
	 * @param map that converts from the Generic type used in this instance 
	 * to a Date, and adds a Generic type used to a Partition
	 */
	public Partitioner(ElementPartitionMap<T> map) {
		this.map = map;
	}
	/**
	 * Get a PartitionSize object by it's name
	 * @param name of the PartitionSize
	 * @return PartitionSize matching the name, or a TwoYearPartionSize if name 
	 * is unknown
	 */
	public static PartitionSize getSize(String name) {
		for(PartitionSize pa : sizes) {
			if(pa.name().equals(name)) {
				return pa;
			}
		}
		return twoYearSize;
	}
	
	/**
	 * Attempt to find the smallest PartitionSize implementation which, spanning
	 * the range first and last specified, produces at most maxP partitions.
	 * @param first Date of beginning of time range
	 * @param last Date of end of time range
	 * @param maxP maximum number of Partitions to use
	 * @return a PartitionSize object which will divide the range into at most
	 * maxP Partitions
	 */
	public PartitionSize getSize(Date first, Date last, int maxP) {
		long diffMS = last.getTime() - first.getTime();
		for(PartitionSize pa : sizes) {
			long maxMS = maxP * pa.intervalMS();
			if(maxMS > diffMS) {
				return pa;
			}
		}
		return twoYearSize;
	}
	
	private void logDates(String message, Date date1, Date date2) {
		SimpleDateFormat f = new SimpleDateFormat("H:mm:ss:SSS MMM d, yyyy");
		f.setTimeZone(TZ_UTC);
		String pd1 = f.format(date1);
		String pd2 = f.format(date2);
		LOGGER.info(message + ":" + pd1 + " - " + pd2);
	}
	
	/**
	 * Create a List of Partition objects of the specified size, which span the
	 * date range specified.
	 * 
	 * @param size of Partitions to create
	 * @param start Date of beginning of time range to cover
	 * @param end Date of end of time range to cover
	 * @return List of Partitions spanning start and end, sized size, in date-
	 * ascending order.
	 */
	public List<Partition<T>> getRange(PartitionSize size, Date start, 
			Date end) {
//		logDates("Constructing partitions Size(" + size.name() + ")",start,end);
//		Date origStart = new Date(start.getTime());
		List<Partition<T>> partitions = new ArrayList<Partition<T>>();
		Calendar cStart = Calendar.getInstance(TZ_UTC);
		cStart.setTime(start);
		size.alignStart(cStart);
//		logDates("AlignedStart("+size.name()+")",origStart,cStart.getTime());
		Calendar cEnd = size.increment(cStart, 1);
//		logDates("AlignedEnd("+size.name()+")",cStart.getTime(),cEnd.getTime());
		while(cStart.getTime().compareTo(end) < 0) {
			partitions.add(new Partition<T>(cStart.getTime(), cEnd.getTime()));
			cStart = cEnd;
			cEnd = size.increment(cStart, 1);
//			logDates("Incremented("+size.name()+")",
//					cStart.getTime(),cEnd.getTime());
		}
		return partitions;
	}

	/**
	 * Add elements from itr into the appropriate partitions. Assumes that 
	 * all elements fit in one of the argument Partitions, that the partitions
	 * are in ascending order by time, and that elements returned from the 
	 * Iterator are in ascending time order.
	 * 
	 * @param partitions to populate with objects
	 * @param itr ascending Iterator of objects to place into the partitions 
	 */
	public void populate(List<Partition<T>> partitions,
			Iterator<T> itr) {
		int idx = 0;
		int size = partitions.size();
		T element = null;
		while(idx < size) {
			Partition<T> partition = partitions.get(idx);
			if(element == null) {
				if(itr.hasNext()) {
					element = itr.next();
				} else {
					// all done
					break;
				}
			}
			// will current result fit in the current partition?
			while(partition.containsDate(map.elementToDate(element))) {
				map.addElementToPartition(element, partition);
				element = null;
				if(itr.hasNext()) {
					element = itr.next();
				} else {
					break;
				}
			}
			idx++;
		}
		if(itr.hasNext()) {
			// eew... Likely bad usage. is this an error?
			LOGGER.warning("Not all elements fit in partitions!");
		}
	}	

	/**
	 * Debugging method
	 * @param partitions to dump
	 */
	public void dumpPartitions(List<Partition<T>> partitions) {
		int i = 0;
		for(Partition<T> partition : partitions) {
			i++;
			logDates("Partition("+i+")", 
					partition.getStart(), partition.getEnd());
		}
	}
	
	/*
	 * 
	 * SOME UNFINISHED/UNTESTED CODE WHICH MAY BE OF INTEREST IN THE FUTURE
	 * FOLLOWS. NONE IS USED FOR NOW:
	 * 
	 */
	
//	/**
//	 * Create a List of Partitions centered at center, extending back in time
//	 * to start, and forward to end. If more than count partitions are required,
//	 * then the edge partitions will be grown until the range is extended to
//	 * start and end, with the edge partitions being non-standard size.
//	 * 
//	 * @param center
//	 * @param start
//	 * @param end
//	 * @param count
//	 * @return
//	 */
//	public List<Partition<T>> getCentered(PartitionSize size, Date center, 
//			Date start, Date end, int count) {
//
//		List<Partition<T>> partitions = new ArrayList<Partition<T>>();
//		Calendar cStart = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//		cStart.setTime(center);
//		size.alignStart(cStart);
//		Calendar cEnd = size.increment(cStart, 1);
//
//		partitions.add(new Partition<T>(cStart.getTime(),cEnd.getTime()));
//
//		int numSides = (count - 1) / 2;
//		// first add those backwards:
//		Partition<T> cur = null;
//		for(int i=1; i <= numSides; i++) {
//			cEnd = cStart;
//			cStart = size.increment(cStart, -1);
//			Date curStart = cStart.getTime();
//			if(i == numSides) {
//				// first partition, maybe make longer:
//				if(curStart.after(start)) {
//					curStart = new Date(start.getTime() - 1000);
//				}
//			}
//			cur = new Partition<T>(curStart, cEnd.getTime());
//			partitions.add(0,cur);
//		}
//
//		// re-align center, and increment:
//		cStart.setTime(center);
//		size.alignStart(cStart);
//		cStart = size.increment(cStart, 1);
//		cEnd = size.increment(cStart, 1);
//
//		for(int i=1; i <= numSides; i++) {
//			Date curEnd = cEnd.getTime();
//			if(i == numSides) {
//				// last partition, maybe make longer:
//				if(curEnd.before(end)) {
//					// end is exclusive, so make 1 MS more:
//					curEnd = end;
//				}
//			}
//			cur = new Partition<T>(cStart.getTime(),curEnd);
//			partitions.add(cur);
//			cStart = cEnd;
//			cEnd = size.increment(cStart, 1);
//		}
//		return partitions;
//	}
	
//	public List<Partition<T>> partitionRange(Date start, Date end, String name) {
	//
//			PartitionSize size = getSize(name);
//			return getRange(size, start, end);
//		}
//		public List<Partition<T>> partitionCentered(Date center, Date start, 
//				Date end, int count, String name) {
	//
//			PartitionSize size = getSize(name);
//			return getCentered(size, center, start, end, count);
//		}
//		public List<Partition<T>> partitionRange(Date start, Date end, int max) {
	//
//			PartitionSize size = getSize(start, end, max);
//			return getRange(size, start, end);
//		}
//		public List<Partition<T>> partitionCentered(Date center, Date start, 
//				Date end, int count) {
	//
//			PartitionSize size = getSize(start,end,count);
//			return getCentered(size, center, start, end, count);
//		}
		
}
