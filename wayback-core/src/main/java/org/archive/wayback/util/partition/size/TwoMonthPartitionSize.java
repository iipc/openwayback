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
