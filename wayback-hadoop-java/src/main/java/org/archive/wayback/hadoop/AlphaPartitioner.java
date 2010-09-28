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
