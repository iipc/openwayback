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
