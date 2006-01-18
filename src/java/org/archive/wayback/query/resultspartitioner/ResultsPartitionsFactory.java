/* ResultsPartitionsFactory
 *
 * $Id$
 *
 * Created on 4:45:03 PM Jan 11, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResults;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResultsPartitionsFactory {

	static ResultsPartitioner partitioners[] = {new HourResultsPartitioner(),
			new DayResultsPartitioner(),
			new WeekResultsPartitioner(),
			new MonthResultsPartitioner(),
			new TwoMonthResultsPartitioner(),
			new YearResultsPartitioner() };
	
	/**
	 * Determine the correct ResultsPartitioner to use given the SearchResults
	 * search range, and use that to break the SearchResults into partitions.
	 * @param results
	 * @return ArrayList of ResultsPartition objects
	 */
	public static ArrayList get(SearchResults results) {
		String rsd = results.getFilter(WaybackConstants.REQUEST_START_DATE);
		String red = results.getFilter(WaybackConstants.REQUEST_END_DATE);
		Date startDate = new Date();
		Date endDate = new Date();
		try {
			startDate = ArchiveUtils.parse14DigitDate(rsd);
			endDate = ArchiveUtils.parse14DigitDate(red);
		} catch (ParseException e) {
			// TODO: better..
			e.printStackTrace();
		}
		long msSpanned = endDate.getTime() - startDate.getTime();
		int secsSpanned = (int) Math.ceil(msSpanned / 1000);

		ResultsPartitioner partitioner = null;
		for(int i = 0; i < partitioners.length; i++) {
			partitioner = partitioners[i];
			if(partitioner.maxSecondsSpanned() >= secsSpanned) {
				break;
			}
		}
		return partitioner.createPartitions(results);
	}
}
