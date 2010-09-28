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

import java.awt.Graphics2D;
import java.util.NoSuchElementException;

/**
 * @author brad
 *
 */
public class ValueGraphElements extends RectangularGraphElement {
	private int values[] = null;
	private int highlightValue = -1;
	private int maxValue = -1;
	private GraphConfiguration config = null;

	/**
	 * @param x the rectangles x in the global coordinate space
	 * @param y the rectangles y in the global coordinate space
	 * @param width the rectangles width
	 * @param height the rectangles height
	 * @param hightlightValue the index of the value to highlight 
	 * @param values array of int values, each must be 0-15
	 * @param maxValue the global maximum value across all elements in the Graph
	 * @param config the configuration for the Graph
	 */
	public ValueGraphElements(int x, int y, int width, int height, 
			int hightlightValue, int values[], int maxValue,
			GraphConfiguration config) {

		super(x, y, width, height);
//		System.err.format("Created VGEs (%d,%d)-(%d,%d)\n",x,y,width,height);

		this.highlightValue = hightlightValue;
		this.values = values;
		this.config = config;
		this.maxValue = maxValue;
	}

	/**
	 * return the i'th ValueGraphElement
	 * @param i the index of the element to return
	 * @return the ValueGraphElement at index i
	 */
	public ValueGraphElement getElement(int i) {
		if((i < 0) || (i >= values.length)) {
			throw new NoSuchElementException();
		}
		int minHeight = config.valueMinHeight;
		
		// normalize height to value between 0 and 1:
		float value = ((float) values[i]) / ((float) maxValue);
		float usableHeight = height - minHeight;
		int valueHeight = (int) (usableHeight * value) + minHeight;
		
		int elX = Graph.xlateX(width, values.length, i);
		int elW = Graph.xlateX(width, values.length, i+1) - elX;
		int elY = height - valueHeight;
		boolean hot = i == highlightValue;
		return new ValueGraphElement(x + elX, y + elY, elW, valueHeight, hot, config);
	}
	
	public void draw(Graphics2D g2d) {
		for(int i = 0; i < values.length; i++) {
			if(values[i] > 0) {
				getElement(i).draw(g2d);
			}
		}
	}

	/**
	 * @return the index of the highlighted value
	 */
	public int getHighlightValue() {
		return highlightValue;
	}

	/**
	 * @return the raw int values for the graph
	 */
	public int[] getRawValues() {
		return values;
	}
	
}
