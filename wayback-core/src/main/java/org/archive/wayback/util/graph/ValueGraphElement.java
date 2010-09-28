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
