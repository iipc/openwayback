/* GraphElement
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
public interface GraphElement {
	/**
	 * Draw the element onto a Graphics2D.
	 * 
	 * @param g2d the Graphics2D onto which the element should be drawn.
	 */
	public void draw(Graphics2D g2d);
}
