/* Graph
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

/**
 * @author brad
 *
 */
public class Graph extends RectangularGraphElement {

	private RegionGraphElement regions[] = null;
	private GraphConfiguration config = null;

	/**
	 * @param width the width of the graph
	 * @param height the height of the graph
	 * @param data the values to draw in the graph
	 * @param config the configuration to use when drawing the graph
	 */
	public Graph(int width, int height, RegionData data[],
			GraphConfiguration config) {
		super(0,0,width,height);
		this.config = config;

		int totalValues = 0;
		int maxValue = -1;
		for(RegionData datum : data) {
			int array[] = datum.getValues();
			totalValues += array.length;
			for(int d : array) {
				if(d > maxValue) {
					maxValue = d;
				}
			}
		}
		int valuesSoFar = 0;
		regions = new RegionGraphElement[data.length];
		for(int i = 0; i < data.length; i++) {
			int vCount = data[i].getValues().length;
			
			int x = Graph.xlateX(width, totalValues, valuesSoFar);
			int w = Graph.xlateX(width, totalValues, valuesSoFar + vCount) - x;
			data[i].setMaxValue(maxValue);
			regions[i] = new RegionGraphElement(x,0,w,height,data[i],config);
			valuesSoFar += vCount;
		}
	}

	/**
	 * @return the RegionGraphElements for the graph
	 */
	public RegionGraphElement[] getRegions() {
		return regions;
	}

	public void draw(Graphics2D g2d) {

		// set up rendering hints:
		config.setRenderingHints(g2d);
		
		// draw background:
		g2d.setColor(config.backgroundColor);
		g2d.fillRect(1, 1, width - 2, height - 2);
		
		for(RegionGraphElement region : regions) {
			region.draw(g2d);
		}

		// draw line below values:
//		int labelHeight = config.regionFontSize + (config.fontPadY * 2);
//		int valuesHeight = (height - labelHeight) + 1;
//
//		g2d.setColor(config.regionBorderColor);
//		g2d.setStroke(config.regionBorderStroke);
//		g2d.drawLine(1, valuesHeight, width - 2, valuesHeight);
	}

	static int xlateX(int w, int c, int i) {
		if(i == 0) {
			return 0;
		} else if(i == c) {
			return w;
		}
		float width = w;
		float count = c;
		float idx = i;
		float x = (idx/count) * width;
		return (int) x;
	}
	
}
