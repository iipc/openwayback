/* ResultsPartitioner
 *
 * $Id$
 *
 * Created on 6:43:42 PM Dec 29, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.query.resultspartitioner;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class ResultsPartitioner {
	
	protected Calendar getCalendar() {
		String[] ids = TimeZone.getAvailableIDs(0);
		if (ids.length < 1) {
			return null;
		}
		TimeZone gmt = new SimpleTimeZone(0, ids[0]);
		return new GregorianCalendar(gmt);		
	}
	
	protected Calendar dateStrToCalendar(String dateStr) {
		return Timestamp.dateStrToCalendar(dateStr);
	}

	/**
	 * @return the maximum seconds viewable within this partition type.
	 */
	public abstract int maxSecondsSpanned();
	
	protected abstract void alignStart(Calendar start);

	protected abstract Calendar incrementPartition(Calendar start, int count);

	protected abstract String rangeToTitle(Calendar start, Calendar end,
			WaybackRequest wbRequest);
}
