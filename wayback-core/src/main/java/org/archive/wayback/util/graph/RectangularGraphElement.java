/* RectangularGraphElement
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

import java.awt.Rectangle;

/**
 * A GraphElement which allows interrogation of it's bounding java.awt.Rectangle
 * @author brad
 *
 */
public abstract class RectangularGraphElement implements GraphElement {
	protected int x = 0;
	protected int y = 0;
	protected int width = 0;
	protected int height = 0;

	/**
	 * Construct a new RectangularGraphElement with the supplied values
	 * @param x the rectangles x in the global coordinate space
	 * @param y the rectangles y in the global coordinate space
	 * @param width the rectangles width
	 * @param height the rectangles height
	 */
	public RectangularGraphElement(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the java.awt.Rectangle which bounds this GraphElement, in the
	 * coordinate space of the Graph which contains it.
	 */
	public Rectangle getBoundingRectangle() {
		return new Rectangle(x, y, width, height);
	}
}
