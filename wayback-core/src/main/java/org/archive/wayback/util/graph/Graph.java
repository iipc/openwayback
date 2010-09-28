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
