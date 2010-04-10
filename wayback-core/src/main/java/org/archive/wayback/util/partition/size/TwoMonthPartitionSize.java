package org.archive.wayback.util.partition.size;

import java.util.Calendar;
import java.util.TimeZone;

import org.archive.wayback.util.partition.PartitionSize;

/**
 * PartitionSize which aligns on two Month partitions
 * @author brad
 *
 */
public class TwoMonthPartitionSize implements PartitionSize {

	public String name() {
		return PartitionSize.TWO_MONTH_NAME;
	}

	public long intervalMS() {
		return MS_IN_TWO_MONTH;
	}

	public void alignStart(Calendar in) {
		in.set(Calendar.DAY_OF_MONTH,1);
		in.set(Calendar.HOUR_OF_DAY,0);
		in.set(Calendar.MINUTE,0);
		in.set(Calendar.SECOND,0);
		in.set(Calendar.MILLISECOND, 0);
	}

	public Calendar increment(Calendar start, int offset) {
		
		Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		end.setTime(start.getTime());
		end.add(Calendar.MONTH,2 * offset);
		return end;
	}
}
