package org.archive.wayback.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.WMRequest;

public class ResourceResults {
	ArrayList results = null;

	public ResourceResults() {
		super();
		this.results = new ArrayList();
	}

	public boolean isEmpty() {
		return results.isEmpty();
	}

	public void addResourceResult(final ResourceResult result) {
		results.add(result);
	}

	public int getNumResults() {
		return results.size();
	}

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

	public Iterator iterator() {
		return results.iterator();
	}

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

	public Timestamp firstTimestamp() {
		if (results.isEmpty()) {
			return null;
		}
		ResourceResult first = (ResourceResult) results.get(0);
		return first.getTimestamp();
	}

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
		// TODO Auto-generated method stub

	}

}
