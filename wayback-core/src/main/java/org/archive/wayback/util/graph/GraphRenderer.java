/* GraphRenderer
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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * @author brad
 *
 */
public class GraphRenderer {
	/**
	 * appropriate Content-Type HTTP header value for graph image content
	 * produced by render(OutputStream,Graph)
	 */
	public final static String RENDERED_IMAGE_MIME = "image/png"; 
	/**
	 * Create both an HTML AREA map and an HTML IMG for a graph, using provided
	 * href targets, and titles within the AREA map.
	 * @param graph Graph to draw
	 * @param mapName name of HTML AREA map
	 * @param imgUrl URL to rendered Graph, see GraphEncoder.encode()
	 * @param targets URL href targets for the years in the Graph
	 * @param titles titles for the years in the graph.
	 * @return HTML String for the resulting AREA and IMG
	 */
	public static String renderHTML(Graph graph, String mapName, String imgUrl, 
			String targets[], String titles[]) {

		StringBuilder sb = new StringBuilder();
		sb.append("<map name=\"").append(mapName).append("\">");
		RegionGraphElement rge[] = graph.getRegions();
		int count = rge.length;
		for(int i = 0; i < count; i++) {
			if(targets[i] != null) {
				Rectangle r = rge[i].getBoundingRectangle();
				sb.append("<area href=\"").append(targets[i]).append("\"");
				if(titles[i] != null) {
					sb.append(" title=\"").append(titles[i]).append("\"");
				}
				sb.append(" shape=\"rect\" coords=\"");
				sb.append(r.x).append(",");
				sb.append(r.y).append(",");
				sb.append(r.x+r.width).append(",");
				sb.append(r.y+r.height).append("\" border=\"1\" />");
			}
		}
		sb.append("</map>");
		sb.append("<image src=\"").append(imgUrl).append("\"");
		sb.append(" border=\"0\" width=\"").append(graph.width).append("\"");
		sb.append(" height=\"").append(graph.height).append("\"");
		sb.append(" usemap=\"#").append(mapName).append("\" />");

		return sb.toString();
	}
		
	/**
	 * Send a PNG format byte stream for the argument Graph to the provided
	 * OutputStream  
	 * @param target OutputStream to write PNG format bytes
	 * @param graph Graph to send to the target
	 * @throws IOException for usual reasons.
	 */
	public void render(OutputStream target, Graph graph) throws IOException {
		
		BufferedImage bi = 
			new BufferedImage(graph.width, graph.height, 
					GraphConfiguration.imageType);
		Graphics2D g2d = bi.createGraphics();
		graph.draw(g2d);
	    ImageIO.write(bi, "png", target);
	}

}
