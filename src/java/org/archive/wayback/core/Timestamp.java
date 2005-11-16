/* Timestamp
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import java.text.ParseException;
import java.util.Date;

import org.archive.util.ArchiveUtils;

/**
 * Represents a moment in time as a 14-digit string, and interally as a Date.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class Timestamp {
	private final static String FIRST1_TIMESTAMP = "19960101000000";

	private final static String FIRST2_TIMESTAMP = "20000101000000";

	private final static String LAST1_TIMESTAMP = "19991231235959";

	// private final static String LAST2_TIMESTAMP = "20311231235959";
	private final static String LAST2_TIMESTAMP = "29991231235959";

	private final static String[] months = { "Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	private String dateStr = null;

	private Date date = null;

	/**
	 * Constructor
	 */
	public Timestamp() {
		super();
	}

	/**
	 * @param dateStr
	 * @return Timestamp object representing the earliest date represented by
	 *         the (possibly) partial digit-string argument.
	 * @throws ParseException
	 */
	public static Timestamp parseBefore(final String dateStr)
			throws ParseException {
		Timestamp ts = new Timestamp();
		ts.setDateStr(padStartTimestamp(dateStr));
		return ts;
	}

	/**
	 * @param dateStr
	 * @return Timestamp object representing the latest date represented by the
	 *         (possibly) partial digit-string argument.
	 * @throws ParseException
	 */
	public static Timestamp parseAfter(final String dateStr)
			throws ParseException {
		Timestamp ts = new Timestamp();
		ts.setDateStr(padEndTimestamp(dateStr));
		return ts;
	}

	/**
	 * @return Timestamp object representing the current date.
	 * @throws ParseException
	 */
	public static Timestamp currentTimestamp() throws ParseException {
		Timestamp ts = new Timestamp();
		ts.date = new Date();
		ts.dateStr = ArchiveUtils.get14DigitDate(ts.date);
		return ts;
	}


	/**
	 * @return Timestamp object representing the earliest possible date.
	 * @throws ParseException
	 */
	public static Timestamp earliestTimestamp() throws ParseException {
		Timestamp ts = new Timestamp();
		ts.setDateStr(FIRST1_TIMESTAMP);
		return ts;
	}

	/**
	 * @return Timestamp object representing the latest possible date.
	 * @throws ParseException
	 */
	public static Timestamp latestTimestamp() throws ParseException {
		return currentTimestamp();
//		Timestamp ts = new Timestamp();
//		ts.setDateStr(LAST2_TIMESTAMP);
//		return ts;
	}

	/**
	 * @param sse
	 * @return Timestamp object representing the seconds since epoch argument.
	 * @throws ParseException
	 */
	public static Timestamp fromSse(final int sse) throws ParseException {
		String dateStr = ArchiveUtils.get14DigitDate(sse * 1000);
		Timestamp ts = new Timestamp();
		ts.setDateStr(dateStr);
		return ts;
	}

	private static String padStartTimestamp(final String input) {
		String first = FIRST1_TIMESTAMP;
		if (input.length() == 0) {
			return FIRST1_TIMESTAMP;
		}
		if (input.length() < 4) {
			if (input.charAt(0) == '2') {
				first = FIRST2_TIMESTAMP;
			}
		}
		return padTimestamp(input, first);
	}

	private static String padEndTimestamp(final String input) {
		String last = LAST1_TIMESTAMP;
		if (input.length() == 0) {
			return ArchiveUtils.get14DigitDate(new Date());
		}
		if (input.length() < 4) {
			if (input.charAt(0) == '2') {
				last = LAST2_TIMESTAMP;
			}
		}
		return padTimestamp(input, last);
	}

	private static String padTimestamp(final String input, final String output) {
		if (input.length() > output.length()) {
			return input;
		}
		return input + output.substring(input.length());
	}

	/**
	 * @return the 14-digit String representation of this Timestamp.
	 */
	public String getDateStr() {
		return dateStr;
	}

	/**
	 * initialize interal data structures for this Timestamp from the 14-digit
	 * argument.
	 * 
	 * @param dateStr
	 * @throws ParseException
	 */
	public void setDateStr(String dateStr) throws ParseException {
		date = ArchiveUtils.parse14DigitDate(dateStr);
		this.dateStr = dateStr;
	}

	/**
	 * @return the integer number of seconds since epoch represented by this
	 *         Timestamp.
	 */
	public int sse() {
		return Math.round(date.getTime() / 1000);
	}

	/**
	 * function that calculates integer milliseconds between this records
	 * timeStamp and the arguments timeStamp. result is the absolute number of
	 * milliseconds difference.
	 * 
	 * @param otherTimeStamp
	 * @return int absolute milliseconds between the argument and this records
	 *         timestamp.
	 * @throws ParseException
	 */
	public long absDistanceFromTimestamp(final Timestamp otherTimeStamp)
			throws ParseException {
		return Math.abs(distanceFromTimestamp(otherTimeStamp));
	}

	/**
	 * function that calculates integer milliseconds between this records
	 * timeStamp and the arguments timeStamp. result is negative if this records
	 * timeStamp is less than the argument, positive if it is greater, and 0 if
	 * the same.
	 * 
	 * @param otherTimeStamp
	 * @return long milliseconds
	 * @throws ParseException
	 */
	public long distanceFromTimestamp(final Timestamp otherTimeStamp)
			throws ParseException {
		Date myDate = ArchiveUtils.parse14DigitDate(dateStr);
		Date otherDate = ArchiveUtils.parse14DigitDate(otherTimeStamp
				.getDateStr());
		return otherDate.getTime() - myDate.getTime();
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
	 * @return user friendly String representation of the Date of this
	 *         Timestamp.
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

	/**
	 * Presently unused, but possibly helpful in complex QueryUI generation.
	 * 
	 * @return Timestamp representing the start of the Year this Timestamp
	 *         occured in.
	 * @throws ParseException
	 */
	public Timestamp startOfYear() throws ParseException {
		return parseBefore(dateStr.substring(0, 4));
	}

	/**
	 * Presently unused, but possibly helpful in complex QueryUI generation.
	 * 
	 * @return Timestamp representing the start of the Month this Timestamp
	 *         occured in.
	 * @throws ParseException
	 */
	public Timestamp startOfMonth() throws ParseException {
		return parseBefore(dateStr.substring(0, 6));
	}

	/**
	 * Presently unused, but possibly helpful in complex QueryUI generation.
	 * 
	 * @return Timestamp representing the start of the Week this Timestamp
	 *         occured in.
	 * @throws ParseException
	 */
	public Timestamp startOfWeek() throws ParseException {
		String yearMonth = dateStr.substring(0, 6);
		String dayOfMonth = dateStr.substring(6, 8);
		int dom = Integer.parseInt(dayOfMonth);
		int mod = dom % 7;
		dom -= mod;
		String paddedDay = (dom < 10) ? "0" + dom : "" + dom;
		return parseBefore(yearMonth + paddedDay);
	}

	/**
	 * Presently unused, but possibly helpful in complex QueryUI generation.
	 * 
	 * @return Timestamp representing the start of the Day this Timestamp
	 *         occured in.
	 * @throws ParseException
	 */
	public Timestamp startOfDay() throws ParseException {
		return parseBefore(dateStr.substring(0, 8));
	}

	/**
	 * Presently unused, but possibly helpful in complex QueryUI generation.
	 * 
	 * @return Timestamp representing the start of the Hour this Timestamp
	 *         occured in.
	 * @throws ParseException
	 */
	public Timestamp startOfHour() throws ParseException {
		return parseBefore(dateStr.substring(0, 10));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
// public Date getDate() {
// String[] ids = TimeZone.getAvailableIDs(0);
// if(ids.length < 1) {
// return null;
// }
// TimeZone gmt = new SimpleTimeZone(0,ids[0]);
// Calendar cal = new GregorianCalendar(gmt);
// int year = Integer.parseInt(dateStr.substring(0,4));
// int month = Integer.parseInt(dateStr.substring(4,2)) - 1;
// int day = Integer.parseInt(dateStr.substring(6,2));
// int hour = Integer.parseInt(dateStr.substring(8,2));
// int min = Integer.parseInt(dateStr.substring(10,2));
// int sec = Integer.parseInt(dateStr.substring(12,2));
//
// cal.set(year,month,day,hour,min,sec);
// return cal.getTime();
// }
//