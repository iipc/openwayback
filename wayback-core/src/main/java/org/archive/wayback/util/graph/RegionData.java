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
	 * sets the highlighted value index, or removes highlight if -1 is used
	 * @param highlightedValue the index of the bar to highlight.
	 */
	public void setHighlightedValue(int highlightedValue) {
		this.highlightedValue = highlightedValue;
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
