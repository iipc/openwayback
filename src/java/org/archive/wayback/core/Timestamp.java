package org.archive.wayback.core;

import java.text.ParseException;
import java.util.Date;

import org.archive.util.ArchiveUtils;

public class Timestamp {
	private final static String FIRST1_TIMESTAMP = "19960101000000";

	private final static String FIRST2_TIMESTAMP = "20000101000000";

	private final static String LAST1_TIMESTAMP = "19991231235959";

	//	private final static String LAST2_TIMESTAMP = "20311231235959";
	private final static String LAST2_TIMESTAMP = "29991231235959";

	private final static String[] months = { "Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	private String dateStr = null;

	private Date date = null;

	public Timestamp() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static Timestamp parseBefore(final String dateStr)
			throws ParseException {
		Timestamp ts = new Timestamp();
		ts.setDateStr(padStartTimestamp(dateStr));
		return ts;
	}

	public static Timestamp parseAfter(final String dateStr)
			throws ParseException {
		Timestamp ts = new Timestamp();
		ts.setDateStr(padEndTimestamp(dateStr));
		return ts;
	}

	public static Timestamp earliestTimestamp() throws ParseException {
		Timestamp ts = new Timestamp();
		ts.setDateStr(FIRST1_TIMESTAMP);
		return ts;
	}

	public static Timestamp latestTimestamp() throws ParseException {
		Timestamp ts = new Timestamp();
		ts.setDateStr(LAST2_TIMESTAMP);
		return ts;
	}

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
			return LAST2_TIMESTAMP;
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

	public String getDateStr() {
		return dateStr;
	}

	public void setDateStr(String dateStr) throws ParseException {
		date = ArchiveUtils.parse14DigitDate(dateStr);
		this.dateStr = dateStr;
	}

	public int sse() {
		return Math.round(date.getTime() / 1000);
	}

	/**
	 * function that calculates integer milliseconds between this records 
	 * timeStamp and the arguments timeStamp. result is the absolute
	 * number of milliseconds difference.
	 * 
	 * @param String 14 digit UTC representation of another timestamp.
	 * @return int seconds between the argument and this records timestamp.
	 * @throws ParseException if the inputstring was malformed
	 */

	public long absDistanceFromTimestamp(final Timestamp otherTimeStamp)
			throws ParseException {
		return Math.abs(distanceFromTimestamp(otherTimeStamp));
	}

	/**
	 * function that calculates integer milliseconds between this records 
	 * timeStamp and the arguments timeStamp. result is negative if 
	 * this records timeStamp is less than the argument, positive
	 * if it is greater, and 0 if the same.
	 *
	 * @param String 14 digit UTC representation of another timestamp.
	 * @return int seconds between the argument and this records timestamp.
	 * @throws ParseException if the inputstring was malformed
	 */

	public long distanceFromTimestamp(final Timestamp otherTimeStamp)
			throws ParseException {
		Date myDate = ArchiveUtils.parse14DigitDate(dateStr);
		Date otherDate = ArchiveUtils.parse14DigitDate(otherTimeStamp
				.getDateStr());
		return otherDate.getTime() - myDate.getTime();
	}

	public String getYear() {
		return this.dateStr.substring(0, 4);
	}

	public String getMonth() {
		return this.dateStr.substring(4, 6);
	}

	public String getDay() {
		return this.dateStr.substring(6, 8);
	}

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

	public String prettyTime() {
		return dateStr.substring(8, 10) + ":" + dateStr.substring(10, 12) + ":"
				+ dateStr.substring(12, 14);
	}

	public String prettyDateTime() {
		return prettyDate() + " " + prettyTime();
	}

	public Timestamp startOfYear() throws ParseException {
		return parseBefore(dateStr.substring(0, 4));
	}

	public Timestamp startOfMonth() throws ParseException {
		return parseBefore(dateStr.substring(0, 6));
	}

	public Timestamp startOfWeek() throws ParseException {
		String yearMonth = dateStr.substring(0, 6);
		String dayOfMonth = dateStr.substring(6, 8);
		int dom = Integer.parseInt(dayOfMonth);
		int mod = dom % 7;
		dom -= mod;
		String paddedDay = (dom < 10) ? "0" + dom : "" + dom;
		return parseBefore(yearMonth + paddedDay);
	}

	public Timestamp startOfDay() throws ParseException {
		return parseBefore(dateStr.substring(0, 8));
	}

	public Timestamp startOfHour() throws ParseException {
		return parseBefore(dateStr.substring(0, 10));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
//public Date getDate() {
//String[] ids = TimeZone.getAvailableIDs(0);
//if(ids.length < 1) {
//	return null;
//}
//TimeZone gmt = new SimpleTimeZone(0,ids[0]);
//Calendar cal = new GregorianCalendar(gmt);
//int year = Integer.parseInt(dateStr.substring(0,4));
//int month = Integer.parseInt(dateStr.substring(4,2)) - 1;
//int day = Integer.parseInt(dateStr.substring(6,2));
//int hour = Integer.parseInt(dateStr.substring(8,2));
//int min = Integer.parseInt(dateStr.substring(10,2));
//int sec = Integer.parseInt(dateStr.substring(12,2));
//
//cal.set(year,month,day,hour,min,sec);
//return cal.getTime();
//}
//