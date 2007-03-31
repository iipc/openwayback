/* AlphaPartitioner
 *
 * $Id$
 *
 * Created on 6:08:33 PM Mar 29, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.indexer.hadoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AlphaPartitioner implements Partitioner {
	String boundaries[] = new String[0];

	public void configure(JobConf job) {
		String pathStr = job.get("alpha.splitfile", "/tmp/split.txt");
		System.err.println("Using split " + pathStr);
		Path p = new Path(pathStr);
		FileSystem fs;
		try {
			fs = FileSystem.get(new URI(pathStr), job);
			FSDataInputStream in = fs.open(p);
			InputStreamReader is = new InputStreamReader(in);
			BufferedReader bis = new BufferedReader(is);
			ArrayList<String> l = new ArrayList<String>();
			while (true) {
				String line = bis.readLine();
				if (line == null) {
					break;
				}
				l.add(line);
			}
			boundaries = l.toArray(boundaries);
			Arrays.sort(boundaries);
			System.err.println("Loaded and Sorted split " + pathStr);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the number of partitions in the configuration file. This is also
	 * the number of reduce tasks in the job.
	 */
	public int getNumPartitions() {
		return boundaries.length;
	}

	/** Use {@link Object#hashCode()} to partition. 
	 * @param key 
	 * @param value 
	 * @param numReduceTasks 
	 * @return int partition index for key*/
	public int getPartition(WritableComparable key, Writable value,
			int numReduceTasks) {
		Text t = (Text) key;
		String keyS = t.toString();
		int loc = Arrays.binarySearch(boundaries, keyS);
		if (loc < 0) {
			loc = (loc * -1) - 2;
			if (loc < 0) {
				loc = 0;
			}
		}
		return loc;
	}

}
