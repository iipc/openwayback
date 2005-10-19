/* ResourceResults
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
import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.WMRequest;

/**
 * Slightly more than an ArrayList of ResourceResult objects..
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class ResourceResults {
	ArrayList results = null;

	/**
	 * Constructor
	 */
	public ResourceResults() {
		super();
		this.results = new ArrayList();
	}

	/**
	 * @return true if no ResourceResult objects, false otherwise.
	 */
	public boolean isEmpty() {
		return results.isEmpty();
	}

	/**
	 * @param result
	 *            ResourceResult to add to this set
	 */
	public void addResourceResult(final ResourceResult result) {
		results.add(result);
	}

	/**
	 * @return number of ResourceResult objects contained in this set
	 */
	public int getNumResults() {
		return results.size();
	}

	/**
	 * @param wmRequest
	 * @return the temporally closest ResourceResult object contained in this
	 *         set to the exactTimestamp of the WMRequest argument.
	 */
	public ResourceResult getClosest(final WMRequest wmRequest) {
		ResourceResult closest = null;
		long closestDistance = 0;
		ResourceResult cur = null;
		Timestamp wantTimestamp = wmRequest.getExactTimestamp();

		Iterator itr = results.iterator();
		while (itr.hasNext()) {
			cur = (ResourceResult) itr.next();
			long curDistance;
			try {
				curDistance = cur.getTimestamp().absDistanceFromTimestamp(
						wantTimestamp);
			} catch (ParseException e) {
				continue;
			}
			if ((closest == null) || (curDistance < closestDistance)) {
				closest = cur;
				closestDistance = curDistance;
			}
		}
		return closest;
	}

	/**
	 * @return an Iterator that contains the ResourceResult objects
	 */
	public Iterator iterator() {
		return results.iterator();
	}

	/**
	 * unused presently, possibly useful in advanced QueryUI column
	 * generation...
	 * 
	 * @return Arraylist of String years included in this set.
	 */
	public ArrayList getYears() {
		ArrayList years = new ArrayList();
		String lastYear = "";
		Iterator itr = results.iterator();
		while (itr.hasNext()) {
			ResourceResult cur = (ResourceResult) itr.next();
			String curYear = cur.getTimestamp().getYear();
			if (!curYear.equals(lastYear)) {
				years.add(curYear);
				lastYear = curYear;
			}
		}
		return years;
	}

	/**
	 * unused presently, possibly useful in advanced QueryUI column
	 * generation...
	 * 
	 * @param year
	 * @return ArrayList of ResourceResult objects within the year argument.
	 */
	public ArrayList resultsInYear(String year) {
		ArrayList resultsToReturn = new ArrayList();
		Iterator itr = results.iterator();
		while (itr.hasNext()) {
			ResourceResult cur = (ResourceResult) itr.next();
			if (cur.getTimestamp().getYear().equals(year)) {
				resultsToReturn.add(cur);
			}
		}
		return resultsToReturn;
	}

	/**
	 * @return the earliest Timestamp among all ResourceResult objects in this
	 *         set.
	 */
	public Timestamp firstTimestamp() {
		if (results.isEmpty()) {
			return null;
		}
		ResourceResult first = (ResourceResult) results.get(0);
		return first.getTimestamp();
	}

	/**
	 * @return the latest Timestamp among all ResourceResult objects in this
	 *         set.
	 */
	public Timestamp lastTimestamp() {
		if (results.isEmpty()) {
			return null;
		}
		ResourceResult last = (ResourceResult) results.get(results.size() - 1);
		return last.getTimestamp();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
