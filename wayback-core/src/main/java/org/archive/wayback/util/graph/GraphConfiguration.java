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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

/**
 * @author brad
 *
 */
public class GraphConfiguration {
    final static BasicStroke dashedStroke = new BasicStroke(1.0f, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_MITER, 
            1.0f, new float[] {1.0f}, 0.0f);

    final static BasicStroke solidStroke = new BasicStroke(1.0f, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_MITER, 
            1.0f);

    /**
     * type of BufferedImage to create, specifically the final constructor arg.
     */
    public static int imageType = BufferedImage.TRANSLUCENT;
    
	/**
	 * Main background color for graphs
	 */
	public Color backgroundColor = new Color(255,255,255,90);
//	public Color backgroundColor = Color.white;

	/**
	 * font size for Year/Month labels
	 */
	public int regionFontSize = 9;
	/**
	 * font name for Year/Month labels
	 */
	public String regionFontName = Font.SANS_SERIF;
	/**
	 * font style for Year/Month labels
	 */
	public int regionFontStyle = Font.PLAIN;
	/**
	 * font Color for Year/Month labels
	 */
	public Color regionLabelColor = Color.black;
	/**
	 * top/bottom font padding for Year/Month labels
	 */
	public int fontPadY = 2;
	/**
	 * left font padding for Year/Month labels
	 */
	public int fontPadX = 4;

	/**
	 * color for Year/Month border lines
	 */
	public Color regionBorderColor = new Color(204,204,204,255);
//	public Color regionBorderColor = Color.darkGray;
	
	/**
	 * Stroke for Year/Month border lines
	 */
	public Stroke regionBorderStroke = solidStroke;

	/**
	 * Background color for active/selected Year/Month
	 */
	public Color regionHighlightColor = new Color(255,255,0,90);

	/**
	 * color for non-active/selected graph values
	 */
	public Color valueColor = Color.black;
	/**
	 * color for active/selected graph values ( #ec008c )
	 */
	public Color valueHighlightColor = new Color(236,0,140,255);

	/**
	 * Minimum pixel height for non-zero graph values
	 */
	public int valueMinHeight = 5;


	private Font regionLabelFont = null;
	/**
	 * @return the current Font to use for Month/Year labels, combination of
	 *   regionFontStyle, regionFontSize, regionFontName
	 */
	public Font getRegionLabelFont() {
		if(regionLabelFont == null) {
			regionLabelFont = new Font(regionFontName,
					regionFontStyle,regionFontSize);
		}
		return regionLabelFont;
	}
	/**
	 * Set whatever redneringHints are needed to properly draw the graph, ie.
	 * AntiAliasing, etc.
	 * @param g2d The Graphics2D objects on which the hints should be set.
	 */
	public void setRenderingHints(Graphics2D g2d) {
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
}
