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
public class RegionGraphElement extends RectangularGraphElement {
	private RegionData data = null;
	private ValueGraphElements values = null;
	private GraphConfiguration config = null;

	/**
	 * @param x the x coordinate for the region, in the global coordinate space
	 * of the containing Graph
	 * @param y the y coordinate for the region, in the global coordinate space
	 * of the containing Graph
	 * @param width the width of this region, in pixels
	 * @param height the height of this region, in pixels
	 * @param data the data to use for this region
	 * @param config the GraphConfiguration for this Graph
	 */
	public RegionGraphElement(int x, int y, int width, int height,
			RegionData data, GraphConfiguration config) {
		super(x,y,width,height);
//		System.err.format("Created region (%d,%d)-(%d,%d)\n",x,y,width,height);
		this.data = data;
		this.config = config;
//		int labelHeight = config.regionFontSize + (config.fontPadY * 2);
		int labelHeight = 0;
		int valuesHeight = height - labelHeight;
		this.values = new ValueGraphElements(x+1, y+1, width - 1, valuesHeight,
				data.getHighlightedValue(), data.getValues(), 
				data.getMaxValue(), config);
	}
	
	/**
	 * @return the RegionData for this region
	 */
	public RegionData getData() {
		return data;
	}
	
	public void draw(Graphics2D g2d) {
		
		if(data.hasHighlightedValue()) {
			g2d.setColor(config.regionHighlightColor);
			g2d.fillRect(x + 1, y+1, width - 1, height-2);
		}
		
		g2d.setColor(config.regionBorderColor);
		g2d.setStroke(config.regionBorderStroke);
		g2d.drawLine(x, y, x, y + height);

//		int fontY = (y + height) - config.fontPadY;
//		
//		g2d.setColor(config.regionLabelColor);
//		g2d.setFont(config.getRegionLabelFont());
//		g2d.drawString(data.getLabel(), x + config.fontPadX, fontY);
		values.draw(g2d);
	}
}
