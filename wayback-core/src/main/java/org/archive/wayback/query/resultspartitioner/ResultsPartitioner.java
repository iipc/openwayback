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
package org.archive.wayback.query.resultspartitioner;

import java.util.Calendar;
import java.util.TimeZone;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.Timestamp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 * @deprecated use org.archive.wayback.util.parition.*
 */
public abstract class ResultsPartitioner {
	
	protected Calendar getCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("GMT"));		
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
