package org.archive.wayback.util.partition.size;

import java.util.Calendar;
import java.util.TimeZone;

import org.archive.wayback.util.partition.PartitionSize;

/**
 * PartitionSize which aligns on one Month partitions
 * @author brad
 *
 */
public class MonthPartitionSize implements PartitionSize {

	public String name() {
		return PartitionSize.MONTH_NAME;
	}

	public long intervalMS() {
		return MS_IN_MONTH;
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
		end.add(Calendar.MONTH,1 * offset);
		return end;
	}
}
