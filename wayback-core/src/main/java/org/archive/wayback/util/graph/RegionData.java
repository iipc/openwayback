/* RegionData
 *
 * $Id$:
 *
 * Created on Apr 14, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.util.graph;

/**
 * Containing object for data associated with one region (month/year/etc) in the
 * graph, including the:
 *   label
 *   highlighted value index
 *   int array of values to graph within this region
 *   the global max int value across all values in the overall graph
 * @author brad
 *
 */
public class RegionData {
	private String label = null;
	private int highlightedValue = -1;
	private int values[] = null;
	private int maxValue = -1;
	/**
	 * @param label the text label to draw for this region
	 * @param highlightedValue the index of the value to "highlight" or -1, if
	 * no values should be highlighted. Note that highlighting a value in a 
	 * region causes the entire region to get a background highlight, also
	 * @param values int array of raw values, each between 0 and 15.
	 */
	public RegionData(String label, int highlightedValue, int values[]) {
		this.label = label;
		this.highlightedValue = highlightedValue;
		this.values = values;
	}
	/**
	 * @return the String label for this region
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @return the index of the highlighted value in this region, or -1 if none
	 * are highlighted
	 */
	public int getHighlightedValue() {
		return highlightedValue;
	}
	/**
	 * @return the raw array of values for this region
	 */
	public int[] getValues() {
		return values;
	}
	/**
	 * @return the global graph maximum value, used for normalizing values to
	 * ensure the values use the entire Y axis.
	 */
	public int getMaxValue() {
		return maxValue;
	}
	
	/**
	 * @param maxValue the global graph maximum value, used for normalizing 
	 * values to ensure the values use the entire Y axis.
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	/**
	 * @return true if one of the values in this region is highlighted
	 */
	public boolean hasHighlightedValue() {
		return (highlightedValue != -1);
	}
}
