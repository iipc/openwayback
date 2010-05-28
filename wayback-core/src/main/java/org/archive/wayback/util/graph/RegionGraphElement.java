/* RegionGraphElement
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
