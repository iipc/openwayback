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
package org.archive.wayback.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.archive.util.ArchiveUtils;


/**
 * Represents a moment in time as a 14-digit string, and interally as a Date.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class Timestamp {

	private final static String LOWER_TIMESTAMP_LIMIT = "10000000000000";
	private final static String UPPER_TIMESTAMP_LIMIT = "29991939295959";
	private final static String YEAR_LOWER_LIMIT      = "1996";
	private final static String YEAR_UPPER_LIMIT      = 
		String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.YEAR));
	private final static String MONTH_LOWER_LIMIT     = "01";
	private final static String MONTH_UPPER_LIMIT     = "12";
	private final static String DAY_LOWER_LIMIT       = "01";
	private final static String HOUR_UPPER_LIMIT      = "23";
	private final static String HOUR_LOWER_LIMIT      = "00";
	private final static String MINUTE_UPPER_LIMIT    = "59";
	private final static String MINUTE_LOWER_LIMIT    = "00";
	private final static String SECOND_UPPER_LIMIT    = "59";
	private final static String SECOND_LOWER_LIMIT    = "00";
	
	private final static int SSE_1996                 = 820454400;

	private final static String[] months = { "Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	private final static String DAYS_IN_MONTH[][];
	private final static int DIM_START_YEAR = 1972;
	private final static int DIM_END_YEAR = 2032;
	static {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.clear();
		int years = DIM_END_YEAR - DIM_START_YEAR;
		DAYS_IN_MONTH = new String[years][12];
		for(int y = 0; y < years; y++) {
			for(int m = 0; m < 12; m++) {
				cal.set(Calendar.YEAR,DIM_START_YEAR + y);
				cal.set(Calendar.MONTH,m);
				cal.set(Calendar.DAY_OF_MONTH,1);
				int calV = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				String maxDayOfMonth = String.valueOf(calV);
				if(maxDayOfMonth.length() == 1) {
					maxDayOfMonth = "0" + maxDayOfMonth;
				}
				DAYS_IN_MONTH[y][m] = maxDayOfMonth;
			}
		}
	}
	private static String getDaysInMonthBound(int year, int month) {
		return DAYS_IN_MONTH[year - DIM_START_YEAR][month];
	}
	
	private String dateStr = null;
	private Date date = null;

	/**
	 * Constructor
	 */
	public Timestamp() {
	}
	
	/**
	 * Construct and initialize structure from a 14-digit String timestamp. If
	 * the argument is too short, or specifies an invalid timestamp, cleanup
	 * will be attempted to create the earliest legal timestamp given the input.
	 * @param dateStr from which to set date
	 */
	public Timestamp(final String dateStr) {
		setDate(dateStrToDate(dateStr));
	}

	/**
	 * Construct and initialize structure from an integer number of seconds
	 * since the epoch.
	 * @param sse SecondsSinceEpoch
	 */
	public Timestamp(final int sse) {
		setSse(sse);
	}

	/**
	 * Construct and initialize structure from an Date
	 * @param date from which date should be set
	 */
	public Timestamp(final Date date) {
		setDate(date);
	}

	/**
	 * set internal structure using Date argument
	 * @param date from which date should be set
	 */
	public void setDate(final Date date) {
		this.date = (Date) date.clone();
		dateStr = ArchiveUtils.get14DigitDate(date);
	}
	
	
	/**
	 * @return Date for this Timestamp
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * set internal structure using seconds since the epoch integer argument
	 * @param sse SecondsSinceEpoch
	 */
	public void setSse(final int sse) {
		setDate(new Date(((long)sse) * 1000));
	}
	
	/**
	 * initialize interal data structures for this Timestamp from the 14-digit
	 * argument. Will clean up timestamp as needed to yield the ealiest
	 * possible timestamp given the possible partial or wrong argument.
	 * 
	 * @param dateStr containing the timestamp
	 */
	public void setDateStr(String dateStr) {
		setDate(dateStrToDate(dateStr));
	}

	/**
	 * @return the 14-digit String representation of this Timestamp.
	 */

	public String getDateStr() {
		return dateStr;
	}

	/**
	 * @return the integer number of seconds since epoch represented by this
	 *         Timestamp.
	 */
	public int sse() {
		return Math.round(date.getTime() / 1000);
	}

	/**
	 * function that calculates integer seconds between this records
	 * timeStamp and the arguments timeStamp. result is the absolute number of
	 * seconds difference.
	 * 
	 * @param otherTimeStamp to compare
	 * @return int absolute seconds between the argument and this records
	 *         timestamp.
	 */
	public int absDistanceFromTimestamp(final Timestamp otherTimeStamp) {
		return Math.abs(distanceFromTimestamp(otherTimeStamp));
	}

	/**
	 * function that calculates integer seconds between this records
	 * timeStamp and the arguments timeStamp. result is negative if this records
	 * timeStamp is less than the argument, positive if it is greater, and 0 if
	 * the same.
	 * 
	 * @param otherTimeStamp to compare
	 * @return int milliseconds
	 */
	public int distanceFromTimestamp(final Timestamp otherTimeStamp) {
		return otherTimeStamp.sse() - sse();
	}

	/**
	 * @return the year portion(first 4 digits) of this Timestamp
	 */
	public String getYear() {
		return this.dateStr.substring(0, 4);
	}

	/**
	 * @return the month portion(digits 5-6) of this Timestamp
	 */
	public String getMonth() {
		return this.dateStr.substring(4, 6);
	}

	/**
	 * @return the day portion(digits 7-8) of this Timestamp
	 */
	public String getDay() {
		return this.dateStr.substring(6, 8);
	}

	/**
	 * @return user friendly String representation of the date of this
	 *         Timestamp. eg: "Jan 13, 1999"
	 */
	public String prettyDate() {
		String year = dateStr.substring(0, 4);
		String month = dateStr.substring(4, 6);
		String day = dateStr.substring(6, 8);
		int monthInt = Integer.parseInt(month) - 1;
		String prettyMonth = "UNK";
		if ((monthInt >= 0) && (monthInt < months.length)) {
			prettyMonth = months[monthInt];
		}
		return prettyMonth + " " + day + ", " + year;
	}

	/**
	 * @return user friendly String representation of the Time of this
	 *         Timestamp.
	 */
	public String prettyTime() {
		return dateStr.substring(8, 10) + ":" + dateStr.substring(10, 12) + ":"
				+ dateStr.substring(12, 14);
	}

	/**
	 * @return user friendly String representation of the Date and Time of this
	 *         Timestamp.
	 */
	public String prettyDateTime() {
		return prettyDate() + " " + prettyTime();
	}

	/*
	 * 
	 * ALL STATIC METHOD BELOW HERE:
	 * =============================
	 * 
	 */
	
	
	private static Calendar getCalendar() {
		String[] ids = TimeZone.getAvailableIDs(0);
		if (ids.length < 1) {
			return null;
		}
		TimeZone gmt = new SimpleTimeZone(0, ids[0]);
		return new GregorianCalendar(gmt);		
	}

	/**
	 * @param dateStr up to 14 digit String representing date
	 * @return a GMT Calendar object, set to the date represented
	 */
	public static Calendar dateStrToCalendar(final String dateStr) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		String paddedDateStr = padStartDateStr(dateStr);

		int iYear = Integer.parseInt(paddedDateStr.substring(0,4));
		int iMonth = Integer.parseInt(paddedDateStr.substring(4,6));
		int iDay = Integer.parseInt(paddedDateStr.substring(6,8));
		int iHour = Integer.parseInt(paddedDateStr.substring(8,10));
		int iMinute = Integer.parseInt(paddedDateStr.substring(10,12));
		int iSecond = Integer.parseInt(paddedDateStr.substring(12,14));

		cal.set(Calendar.YEAR,iYear);
		cal.set(Calendar.MONTH,iMonth - 1);
		cal.set(Calendar.DAY_OF_MONTH,iDay);
		cal.set(Calendar.HOUR_OF_DAY,iHour);
		cal.set(Calendar.MINUTE,iMinute);
		cal.set(Calendar.SECOND,iSecond);
		cal.set(Calendar.MILLISECOND,0);
		
		return cal;
	}
	/**
	 * cleanup the dateStr argument assuming earliest values, and return a
	 * GMT calendar set to the time described by the dateStr.
	 * 
	 * @param dateStr from which to create Calendar
	 * @return Calendar
	 */
	public static Date dateStrToDate(final String dateStr) {
		
		String paddedDateStr = padStartDateStr(dateStr);
		try {
			return ArchiveUtils.parse14DigitDate(paddedDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			// TODO: This is certainly not the right thing, but padStartDateStr
			// should ensure we *never* get here..
			return new Date(SSE_1996);
		}
	}
	

	private static String padDigits(String input, String min, String max, 
			String missing) {
		if(input == null) {
			input = "";
		}
		StringBuilder finalDigits = new StringBuilder();
		//String finalDigits = "";
		for(int i = 0; i < missing.length(); i++) {
			if(input.length() <= i) {
				finalDigits.append(missing.charAt(i));
			} else {
				char inc = input.charAt(i);
				char maxc = max.charAt(i);
				char minc = min.charAt(i);
				if(inc > maxc) {
					inc = maxc;
				} else if (inc < minc) {
					inc = minc;
				}
				finalDigits.append(inc);
			}
		}
		
		return finalDigits.toString();
	}
	
	private static String boundDigits(final String test, final String min, 
			final String max) {
		if(test.compareTo(min) < 0) {
			return min;
		} else if(test.compareTo(max) > 0) {
			return max;
		}
		return test;
	}

	// check each of YEAR, MONTH, DAY, HOUR, MINUTE, SECOND to make sure they
	// are not too large or too small, factoring in the month, leap years, etc.
	// BUGBUG: Leap second bug here.. How long till someone notices?
	private static String boundTimestamp(String input) {
		StringBuilder boundTimestamp = new StringBuilder();
		
		if (input == null) {
			input = "";
		}
		// MAKE SURE THE YEAR IS WITHIN LEGAL BOUNDARIES:
		boundTimestamp.append(boundDigits(input.substring(0,4),
				YEAR_LOWER_LIMIT,YEAR_UPPER_LIMIT));

		// MAKE SURE THE MONTH IS WITHIN LEGAL BOUNDARIES:
		boundTimestamp.append(boundDigits(input.substring(4,6),
				MONTH_LOWER_LIMIT,MONTH_UPPER_LIMIT));
		
		// NOW DEPENDING ON THE YEAR + MONTH, MAKE SURE THE DAY OF MONTH IS
		// WITHIN LEGAL BOUNDARIES:
		int iYear = Integer.parseInt(boundTimestamp.substring(0,4));
		int iMonth = Integer.parseInt(boundTimestamp.substring(4,6));
		String maxDayOfMonth = getDaysInMonthBound(iYear, iMonth-1);

		boundTimestamp.append(boundDigits(input.substring(6,8),
				DAY_LOWER_LIMIT,maxDayOfMonth));
		
		// MAKE SURE THE HOUR IS WITHIN LEGAL BOUNDARIES:
		boundTimestamp.append(boundDigits(input.substring(8,10),
				HOUR_LOWER_LIMIT,HOUR_UPPER_LIMIT));
		
		// MAKE SURE THE MINUTE IS WITHIN LEGAL BOUNDARIES:
		boundTimestamp.append(boundDigits(input.substring(10,12),
				MINUTE_LOWER_LIMIT,MINUTE_UPPER_LIMIT));
		
		// MAKE SURE THE SECOND IS WITHIN LEGAL BOUNDARIES:
		boundTimestamp.append(boundDigits(input.substring(12,14),
				SECOND_LOWER_LIMIT,SECOND_UPPER_LIMIT));

		return boundTimestamp.toString();		
	}

	/**
	 * clean up timestamp argument assuming latest possible values for missing 
	 * or bogus digits.
	 * @param timestamp String
	 * @return String
	 */
	public static String padEndDateStr(String timestamp) {
		return boundTimestamp(padDigits(timestamp,LOWER_TIMESTAMP_LIMIT,
				UPPER_TIMESTAMP_LIMIT,UPPER_TIMESTAMP_LIMIT));
	}

	/**
	 * clean up timestamp argument assuming earliest possible values for missing
	 * or bogus digits.
	 * @param timestamp String
	 * @return String
	 */
	public static String padStartDateStr(String timestamp) {
		return boundTimestamp(padDigits(timestamp,LOWER_TIMESTAMP_LIMIT,
				UPPER_TIMESTAMP_LIMIT,LOWER_TIMESTAMP_LIMIT));
	}

	/**
	 * @param dateStr containing timestamp
	 * @return Timestamp object representing the earliest date represented by
	 *         the (possibly) partial digit-string argument.
	 */
	public static Timestamp parseBefore(final String dateStr) {
		return new Timestamp(padStartDateStr(dateStr));
	}

	/**
	 * @param dateStr containing timestamp
	 * @return Timestamp object representing the latest date represented by the
	 *         (possibly) partial digit-string argument.
	 */
	public static Timestamp parseAfter(final String dateStr) {
		return new Timestamp(padEndDateStr(dateStr));
	}

	/**
	 * @param sse SecondsSinceEpoch
	 * @return Timestamp object representing the seconds since epoch argument.
	 */
	public static Timestamp fromSse(final int sse) {
		//String dateStr = ArchiveUtils.get14DigitDate(sse * 1000);
		return new Timestamp(sse);
	}

	/**
	 * @return Timestamp object representing the current date.
	 */
	public static Timestamp currentTimestamp() {
		return new Timestamp(new Date());
	}
    
	/**
	 * @return Timestamp object representing the latest possible date.
	 */
	public static Timestamp latestTimestamp() {
		return currentTimestamp();
	}

	/**
	 * @return Timestamp object representing the earliest possible date.
	 */
	public static Timestamp earliestTimestamp() {
		return new Timestamp(SSE_1996);
	}
}
