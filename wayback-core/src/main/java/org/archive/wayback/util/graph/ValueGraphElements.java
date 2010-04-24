/* ValueGraphElements
 *
 * $Id$:
 *
 * Created on Apr 9, 2010.
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
