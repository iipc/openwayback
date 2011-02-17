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
package org.archive.wayback.hadoop;

import java.io.IOException;

import org.apache.commons.httpclient.URIException;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 * @author brad
 *
 */
public class CDXCanonicalizingMapper extends Mapper<Object, Text, Text, Text> 
implements Configurable {
	
	private static String MODE_CONFIG_NAME = "cdx.map.mode";
	public static int MODE_GLOBAL = 0;
	public static int MODE_FULL = 1;
	
	
	private Configuration conf;
	private int mode = MODE_GLOBAL;
	private Text key = new Text();
	private Text remainder = new Text();
	private String delim = " ";
	StringBuilder sb = new StringBuilder();
	
	public void map(Object y, Text value, Context context)
			throws IOException, InterruptedException {
		if(mode == MODE_GLOBAL) {
			mapGlobal(y,value,context);
		} else {
			mapFull(y,value,context);
		}
	}

	private static int SHA1_DIGITS = 3;
	AggressiveUrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
	private StringBuilder ksb = new StringBuilder();
	private StringBuilder vsb = new StringBuilder();
	private int i1 = 0;
	private int i2 = 0;
	private int i3 = 0;
	private int i4 = 0;
	
	private void mapGlobal(Object y, Text value, Context context)
	throws IOException, InterruptedException {
		String s = value.toString();

		String parts[] = s.split(delim);
		if(parts.length == 10) {
			if(!parts[9].contains("A")) {
				ksb.setLength(0);
				vsb.setLength(0);
				try {
					ksb.append(canonicalizer.urlStringToKey(parts[0])).append(" ");
					ksb.append(parts[1]); // date
					vsb.append(parts[0]).append(delim); // orig_url
					vsb.append(parts[3]).append(delim); // MIME
					vsb.append(parts[4]).append(delim); // HTTP_CODE
					vsb.append(parts[5].substring(0, SHA1_DIGITS)).append(" "); // SHA1
					vsb.append(parts[6]).append(delim); // redirect
					vsb.append(parts[7]).append(delim); // start_offset
					vsb.append(parts[8]).append(".arc.gz"); // arc_prefix
					key.set(ksb.toString());
					remainder.set(vsb.toString());
					context.write(key, remainder);
				} catch (URIException e) {
					System.err.println("Failed Canonicalize:("+parts[0]+
							") in ("+parts[8]+"):("+parts[7]+")");
				}
			}
		} else {
			System.err.println("Funky: Problem with line("+s+")");
		}
	
	}
	private void mapFull(Object y, Text value, Context context)
	throws IOException, InterruptedException {
		String s = value.toString();
		if(s.startsWith(" CDX ")) {
			return;
		}
		boolean problems = true;
		i1 = s.indexOf(delim);
		if(i1 > 0) {
			i2 = s.indexOf(delim, i1 + 1);
			if(i2 > 0) {
				i3 = s.indexOf(delim, i2 + 1);
				if(i3 > 0) {
					i4 = s.lastIndexOf(delim);
					if(i4 > i3) {
						try {
							ksb.setLength(0);
							ksb.append(canonicalizer.urlStringToKey(s.substring(i2 + 1, i3)));
							ksb.append(s.substring(i1,i4));
							key.set(ksb.toString());
							remainder.set(s.substring(i4+1));
							context.write(key, remainder);
							problems = false;
						} catch(URIException e) {
							// just eat it.. problems will be true.
						}
					}
				}
			}
		}
		if(problems) {
			System.err.println("CDX-Can: Problem with line("+s+")");
		}
	}

//	private void mapOld(Object y, Text value, Context context)
//	throws IOException, InterruptedException {
//		String parts[] = value.toString().split(delim);
//		// lets assume key is field 1-2:
//		sb.setLength(0);
//		sb.append(parts[0]).append(delim).append(parts[1]);
//		key.set(sb.toString());
//		remainder.set(join(delim,parts,2));
//		context.write(key, remainder);
//	}
//	
//	private String join(String delim, String parts[], int start) {
//		sb.setLength(0);
//		int count = parts.length -1;
//		for(int i = start; i < count; i++) {
//			sb.append(parts[i]).append(delim);
//		}
//		sb.append(parts[count]);
//		return sb.toString();
//	}

	/**
	 * @param conf Configuration for the Job
	 * @param mode String mode to use, one of MODE_GLOBAL, MODE_FULL
	 */
	public static void setMapMode(Configuration conf, int mode) {
		conf.setInt(MODE_CONFIG_NAME, mode);
	}
	
	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		mode = conf.getInt(MODE_CONFIG_NAME, MODE_FULL);
		delim = conf.get(CDXSortDriver.TEXT_OUTPUT_DELIM_CONFIG,delim);
	}
}
