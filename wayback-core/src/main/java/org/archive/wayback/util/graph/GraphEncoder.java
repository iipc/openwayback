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

/**
 * @author brad
 *
 */
public class GraphEncoder {
	private static String DELIM = "_";
	private static String REGION_DELIM = ":";
	
	/**
	 * convert a String-encoded graph into a usable Graph object, using 
	 * default GraphConfiguration
	 * @param encodedGraph String encoded graph, as returned by getEncoded()
	 * @param noMonth if true, disable the month highlight color
	 * @return a Graph, ready to use
	 * @throws GraphEncodingException if there were problems with the encoded 
	 * data
	 */
	public static Graph decode(String encodedGraph, boolean noMonth) 
	throws GraphEncodingException {
		GraphConfiguration config = new GraphConfiguration();
		if(noMonth) {
			config.valueHighlightColor = config.valueColor;
		}
		return decode(encodedGraph, config);
	}
	/**
	 * convert a String-encoded graph into a usable Graph object, using 
	 * the provided GraphConfiguration.
	 * @param encodedGraph String encoded graph, as returned by getEncoded()
	 * @param config the GraphConfiguration to use
	 * @return a Graph, ready to use
	 * @throws GraphEncodingException if there were problems with the encoded 
	 * data
	 */
	public static Graph decode(String encodedGraph, GraphConfiguration config) 
	throws GraphEncodingException {
		// encoded = "800_35_REGIONDATA_REGIONDATA_REGIONDATA_REGIONDATA_..."
		String parts[] = encodedGraph.split(DELIM);
		int numRegions = parts.length - 2;
		if(parts.length < 1) {
			throw new GraphEncodingException("No regions defined!");
		}
		int width;
		int height;
		try {
			width = Integer.parseInt(parts[0]);
		} catch(NumberFormatException e) {
			throw new GraphEncodingException("Bad integer width:" + parts[0]);
		}
		try {
			height = Integer.parseInt(parts[1]);
		} catch(NumberFormatException e) {
			throw new GraphEncodingException("Bad integer width:" + parts[0]);
		}
		RegionData data[] = new RegionData[numRegions];
		for(int i = 0; i < numRegions; i++) {
			// REGIONDATA = "2001:-1:0ab3f70023f902f"
			//               LABEL:ACTIVE_IDX:HEXDATA
			String regionParts[] = parts[i + 2].split(REGION_DELIM);
			if(regionParts.length != 3) {
				throw new GraphEncodingException("Wrong number of parts in " + 
						parts[i+2]);
			}
			int highlightedValue = Integer.parseInt(regionParts[1]);
			int values[] = decodeHex(regionParts[2]);
			data[i] = new RegionData(regionParts[0], highlightedValue, values);
		}
		return new Graph(width, height, data, config);
	}

	/**
	 * Convert a complete Graph into an opaque String that can later be 
	 * re-assembled into a Graph object. Note that GraphConfiguration 
	 * information is NOT encoded into the opaque String.
	 * @param g Graph to encode
	 * @return opaque String which can later be used with decode()
	 */
	public static String encode(Graph g) {
		RegionGraphElement rge[] = g.getRegions();
		RegionData data[] = new RegionData[rge.length];
		for(int i = 0; i < data.length; i++) {
			data[i] = rge[i].getData();
		}
		return encode(g.width, g.height, data);
	}
	
	/**
	 * Convert a Graph fields into an opaque String that can later be 
	 * re-assembled into a Graph object. Note that GraphConfiguration 
	 * information is NOT encoded into the opaque String.
	 * @param width of the Graph
	 * @param height of the Graph
	 * @param data array of RegionData for the graph
	 * @return opaque String which can later be used with decode()
	 */
	public static String encode(int width, int height, RegionData data[]) {
		StringBuilder sb = new StringBuilder();
		sb.append(width).append(DELIM);
		sb.append(height);
		boolean first = false;
		for(RegionData datum : data) {
			if(first) {
				first = false;
			} else {
				sb.append(DELIM);
			}
			sb.append(datum.getLabel()).append(REGION_DELIM);
			sb.append(datum.getHighlightedValue()).append(REGION_DELIM);
			sb.append(encodeHex(datum.getValues()));
		}
		return sb.toString();
	}

	public static String encodeHex(int values[]) {
		StringBuilder sb = new StringBuilder(values.length);
		for(int value : values) {
			if((value > 15) || (value < 0)){
				throw new IllegalArgumentException();
			}
			sb.append(Integer.toHexString(value));
		}
		return sb.toString();
	}

	private static int[] decodeHex(String hexString) {
		int length = hexString.length();
		int values[] = new int[length];
		for(int i = 0; i < length; i++) {
			char c = hexString.charAt(i);
			if(c >= '0') {
				if(c <= '9') {
					values[i] = c - '0';
				} else {
					if(c > 'f') {
						throw new IllegalArgumentException();						
					} else {
						if(c >= 'a') {
							values[i] = c - 'W';
						} else {
							throw new IllegalArgumentException();							
						}
					}
				}
			} else {
				throw new IllegalArgumentException();
			}
		}
		return values;
	}
}
