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

import java.util.Calendar;

/**
 * A class which simplifies partitioning Dates based on human logical time 
 * intervals: Day, Week, TwoYear.
 * 
 * Implementing classes provide methods to align Calendars at the start of the
 * nearest Day, Month, Week, etc.
 * 
 * In addition, implementors provide methods to create new Calendars based on
 * their specific alignment size.
 * 
 * @author brad
 *
 */
public interface PartitionSize {

	/**
	 * number of milliseconds in a second.. 
	 */
	public final static long MS_IN_SEC = 1000;
	/**
	 * seconds in a non-leap-second hour
	 */
	public final static long SEC_IN_HOUR = 3600;
	/**
	 * hours in a day: 24
	 */
	public final static long HOUR_IN_DAY = 24;
	/**
	 * days in a 7 day week... what color was his white horse?
	 */
	public final static long DAY_IN_WEEK = 7;
	/**
	 * approximate days in one month, that is, 30 days
	 */
	public final static long DAY_IN_MONTH = 30;
	/**
	 * days in one year, assuming a non-leap year
	 */
	public final static long DAY_IN_YEAR = 365;

	/**
	 * milliseconds in 1 hour (approximate: no leap second accounted for)
	 */
	public final static long MS_IN_HOUR = MS_IN_SEC * SEC_IN_HOUR;
	/**
	 * milliseconds in 1 day (approximate: no leap second accounted for)
	 */
	public final static long MS_IN_DAY = MS_IN_HOUR * HOUR_IN_DAY;
	/**
	 * milliseconds in 7 days (approximate: no leap second accounted for)
	 */
	public final static long MS_IN_WEEK = MS_IN_DAY * DAY_IN_WEEK;
	/**
	 * milliseconds in one month (approximate: no leap day/sec accounted for,
	 * and assumes 30 days in a month)
	 */
	public final static long MS_IN_MONTH = MS_IN_DAY * DAY_IN_MONTH;
	/**
	 * milliseconds in two months (approximate: no leap day/sec accounted for,
	 * and assumes 30 day months)
	 */
	public final static long MS_IN_TWO_MONTH = MS_IN_MONTH * 2;
	/**
	 * milliseconds in one year (approximate: no leap day/sec accounted for)
	 */
	public final static long MS_IN_YEAR = MS_IN_DAY * DAY_IN_YEAR;
	/**
	 * milliseconds in two years (approximate: no leap day/sec accounted for)
	 */
	public final static long MS_IN_TWO_YEAR = MS_IN_YEAR * 2;
	
	/**
	 * 
	 */
	public final static String HOUR_NAME = "hour";
	/**
	 * 
	 */
	public final static String DAY_NAME = "day";
	/**
	 * 
	 */
	public final static String WEEK_NAME = "week";
	/**
	 * 
	 */
	public final static String MONTH_NAME = "month";
	/**
	 * 
	 */
	public final static String TWO_MONTH_NAME = "twomonth";
	/**
	 * 
	 */
	public final static String YEAR_NAME = "year";
	/**
	 * 
	 */
	public final static String TWO_YEAR_NAME = "twoyear";

	/**
	 * Align the calendar argument to the start of the interval covered by
	 * this size. Calling this method on a DayPartitionSize will align the
	 * Calendar to the beginning of the Day in which the Calendar's Date object
	 * falls within. 
	 * @param in Calendar object which has internal Date set
	 */
	public void alignStart(Calendar in);

	/**
	 * Create a new Calendar object, aligned relative to the Calendar argument,
	 * either forward or backward some number of partitions.
	 * @param start the returned Calendar will be aligned one day, week, month, 
	 * etc. ahead or behind of this Calendar argument. 
	 * @param offset the relative distance to move the returned calendar 
	 * relative to the argument Calendar.
	 * @return a new Calendar aligned relative to the start Calendar.
	 */
	public Calendar increment(Calendar start, int offset);

	/**
	 * @return the estimated number of milliseconds covered by this 
	 * PartitionSize. Note that this is estimated because of different number of
	 * days in a month, leap days, leap seconds, etc.
	 */
	public long intervalMS();

	/**
	 * @return the name of this PartitionSize. Likely useful for localized 
	 * lookup of human readable text from a properties file.
	 */
	public String name();
}
