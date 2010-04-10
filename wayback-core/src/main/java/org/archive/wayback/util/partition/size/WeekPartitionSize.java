package org.archive.wayback.util.partition.size;

import java.util.Calendar;
import java.util.TimeZone;

import org.archive.wayback.util.partition.PartitionSize;

/**
 * PartitionSize which aligns on one Week partitions
 * @author brad
 *
 */
public class WeekPartitionSize implements PartitionSize {

	public String name() {
		return PartitionSize.WEEK_NAME;
	}

	public long intervalMS() {
		return MS_IN_WEEK;
	}

	public void alignStart(Calendar in) {
		in.set(Calendar.HOUR_OF_DAY,1);
		in.set(Calendar.MINUTE,1);
		in.set(Calendar.SECOND,1);
		in.set(Calendar.MILLISECOND, 0);
	}

	public Calendar increment(Calendar start, int offset) {
		Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		end.setTime(start.getTime());
		end.add(Calendar.DAY_OF_YEAR,7 * offset);
		return end;
	}
}
