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
package org.archive.wayback.hadoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class AlphaPartitioner extends Partitioner<Text, Text> implements
		Configurable {
	private static String CONFIG_SPLIT_PATH_NAME = "alphapartitioner.path";
	private String boundaries[] = new String[0];

	Configuration conf;

	@Override
	public int getPartition(Text key, Text value, int numPartitions) {
		String keyS = key.toString();
		int loc = Arrays.binarySearch(boundaries, keyS);
		if (loc < 0) {
			loc = (loc * -1) - 2;
			if (loc < 0) {
				loc = 0;
			}
		}
		return loc;
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		String partitionPath = getPartitionPath(conf);
		String numReduceTasks = conf.get("mapred.reduce.tasks");
		System.err.println("Num configured reduce tasks:" + numReduceTasks);
		try {
			URI uri = new URI(partitionPath);
			FileSystem fs = FileSystem.get(uri, conf);
			Path p = new Path(partitionPath);
			loadBoundaries(new BufferedReader(new InputStreamReader(fs.open(p))));
		} catch (IOException e) {
			// TODO: ugh. how to handle?
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param conf Configuration for the Job
	 * @param path hdfs:// URI pointing to the split file
	 */
	public static void setPartitionPath(Configuration conf, String path) {
		conf.set(CONFIG_SPLIT_PATH_NAME, path);
	}

	/**
	 * @param conf Configuration for the Job
	 * @return the hdfs:// URI for the split file configured for this job
	 */
	public static String getPartitionPath(Configuration conf) {
		return conf.get(CONFIG_SPLIT_PATH_NAME);
	}

	private void loadBoundaries(BufferedReader bis) throws IOException {
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
	}
}
