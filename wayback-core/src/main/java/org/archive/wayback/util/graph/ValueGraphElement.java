/* ValueGraphElement
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
public class ValueGraphElement extends RectangularGraphElement {
	boolean highlighted = false;
	GraphConfiguration config = null;
	/**
	 * @param x the rectangles x in the global coordinate space
	 * @param y the rectangles y in the global coordinate space
	 * @param width the rectangles width
	 * @param height the rectangles height
	 * @param highlighted true if this value is highlighted 
	 * @param config reference to the configuration for the graph
	 */
	public ValueGraphElement(int x, int y, int width, int height, 
			boolean highlighted, GraphConfiguration config) {
		super(x, y, width, height);
		
		this.highlighted = highlighted;
		this.config = config;
	}


	public void draw(Graphics2D g2d) {
		g2d.setColor(highlighted ? 
				config.valueHighlightColor : config.valueColor);
		g2d.fillRect(x, y, width, height);
	}
}
