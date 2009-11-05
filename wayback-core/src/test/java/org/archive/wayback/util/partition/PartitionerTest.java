package org.archive.wayback.util.partition;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.archive.wayback.util.Timestamp;

import junit.framework.TestCase;
import org.archive.wayback.util.partition.size.*;

public class PartitionerTest extends TestCase {

	public void testGetRangeDay() {
//		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//		Partitioner<Date> p = new Partitioner<Date>(new DayPartitionSize());
//		Date start = Timestamp.parseBefore("20070101").getDate();
//		Date end = Timestamp.parseBefore("200701051").getDate();
//		
//		List<Partition<Date>> l = p.getRange(start, end);
////		for(Partition<Date> pp : l) {
////			System.out.println("Partition(" + dateToTS(pp.getStart())
////					+ ") - (" +	dateToTS(pp.getEnd()) + ")");
////		}
//		assertTrue("P1-1-OK",dateToTS(l.get(0).getStart()).equals("20070101000000"));
//		assertTrue("P1-1-OK",dateToTS(l.get(0).getEnd()).equals("20070102000000"));
//
//		assertTrue("P1-2-OK",dateToTS(l.get(1).getStart()).equals("20070102000000"));
//		assertTrue("P1-2-OK",dateToTS(l.get(1).getEnd()).equals("20070103000000"));
//
//		assertTrue("P1-3-OK",dateToTS(l.get(2).getStart()).equals("20070103000000"));
//		assertTrue("P1-3-OK",dateToTS(l.get(2).getEnd()).equals("20070104000000"));
//
//		assertTrue("P1-4-OK",dateToTS(l.get(3).getStart()).equals("20070104000000"));
//		assertTrue("P1-4-OK",dateToTS(l.get(3).getEnd()).equals("20070105000000"));
//
//		assertTrue("P1-5-OK",dateToTS(l.get(4).getStart()).equals("20070105000000"));
//		assertTrue("P1-5-OK",dateToTS(l.get(4).getEnd()).equals("20070106000000"));
//
//		assertTrue( "Size OK",l.size() == 5);
	}

	public void testGetRangeMonth() {
//		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//		Partitioner<Date> p = new Partitioner<Date>(new MonthPartitionSize());
//		Date start = Timestamp.parseBefore("200611").getDate();
//		Date end = Timestamp.parseBefore("20070505").getDate();
//		
//		List<Partition<Date>> l = p.getRange(start, end);
////		for(Partition<Date> pp : l) {
////			System.out.println("Partition(" + dateToTS(pp.getStart())
////					+ ") - (" +	dateToTS(pp.getEnd()) + ")");
////		}
//		assertTrue( "Size OK",l.size() == 7);
	}
	
	public void testGetRangeYear() {
//		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//		Partitioner<Date> p = new Partitioner<Date>(new YearPartitionSize());
//		Date start = Timestamp.parseBefore("200611").getDate();
//		Date end = Timestamp.parseBefore("20070505").getDate();
//		
//		List<Partition<Date>> l = p.getRange(start, end);
////		for(Partition<Date> pp : l) {
////			System.out.println("Partition(" + dateToTS(pp.getStart())
////					+ ") - (" +	dateToTS(pp.getEnd()) + ")");
////		}
//		assertTrue( "Size OK",l.size() == 2);
	}

	
	private String dateToTS(Date d) {
		return new Timestamp(d).getDateStr();
	}
	
	public void testGetCentered() {
//		Partitioner<Date> p = new Partitioner<Date>(new MonthPartitionSize());
//		Date center = Timestamp.parseBefore("200501").getDate();
//		Date start = Timestamp.parseBefore("200311").getDate();
//		Date end = Timestamp.parseBefore("20070505").getDate();
//		int max = 10;
//		
//		List<Partition<Date>> l = p.getCentered(center, start, end, max);
////		for(Partition<Date> pp : l) {
////			System.out.println("Partition(" + dateToTS(pp.getStart())
////					+ ") - (" +	dateToTS(pp.getEnd()) + ")");
////		}
//		assertTrue( "Size OK",l.size() == 9);
	}

}
