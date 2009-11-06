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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class AlphaPartitioner implements Partitioner<Text, Text> {
	private String boundaries[] = new String[0];
	public static final String DEFAULT_PATH = "_split.txt";
	public static final String SPLIT_PATH_NAME = "alpha-partition.txt";
	public static final String SPLIT_CACHE_NAME = "alpha-partition-cache.txt";
	public static final String CACHE_SPLIT_URI_CONFIG = 
		"alphapartitioner.cachesplituri";
	public static final String CACHE_SPLIT_PATH_CONFIG = 
		"alphapartitioner.cachesplitpath";
	public static final String CACHE_SPLIT_STAMP_CONFIG = 
		"alphapartitioner.cachesplitstamp";

	/**
	 * Called by client prior to launching the job. The File argument is a
	 * split file, which is to be pushed into the FileSystem, and into the
	 * DistributedCache from there, for use by the Map tasks.
	 * @throws URISyntaxException 
	 */
	public static void setPartitionFile(JobConf conf, File f) 
	throws IOException, URISyntaxException {
	
	    FileSystem fs = FileSystem.get(conf);

	    Path fsSplitPath = new Path(SPLIT_PATH_NAME);
	    fs.copyFromLocalFile(new Path(f.getAbsolutePath()), fsSplitPath);

	    String cacheURIString = SPLIT_PATH_NAME + "#" + SPLIT_CACHE_NAME; 
	    DistributedCache.addCacheFile(new URI(cacheURIString), conf);
	    
	    FileStatus fsStat = fs.getFileStatus(fsSplitPath);
	    String mtime = String.valueOf(fsStat.getModificationTime());
	    System.err.println("Files mtime(" + mtime + ")");
	    conf.set(AlphaPartitioner.CACHE_SPLIT_URI_CONFIG,cacheURIString);
	    conf.set(AlphaPartitioner.CACHE_SPLIT_PATH_CONFIG,SPLIT_CACHE_NAME);
	    conf.set(AlphaPartitioner.CACHE_SPLIT_STAMP_CONFIG,mtime);
	}

	public static void setPartitionFileBad(JobConf conf, File f) 
	throws IOException, URISyntaxException {
	
	    FileSystem fs = FileSystem.get(conf);

	    Path fsSplitPath = new Path(SPLIT_PATH_NAME);
	    fs.copyFromLocalFile(new Path(f.getAbsolutePath()), fsSplitPath);

	    String cacheURIString = SPLIT_PATH_NAME + "#" + SPLIT_CACHE_NAME; 
	    DistributedCache.addCacheFile(new URI(cacheURIString), conf);
	    
	    FileStatus fsStat = fs.getFileStatus(fsSplitPath);
	    String mtime = String.valueOf(fsStat.getModificationTime());
	    System.err.println("Files mtime(" + mtime + ")");
	    conf.set(AlphaPartitioner.CACHE_SPLIT_URI_CONFIG,cacheURIString);
	    conf.set(AlphaPartitioner.CACHE_SPLIT_PATH_CONFIG,SPLIT_CACHE_NAME);
	    conf.set(AlphaPartitioner.CACHE_SPLIT_STAMP_CONFIG,mtime);
	}	
	
	/**
	 * Get a BufferedReader on the alphabetic split file stored in the 
	 * DistributedCache
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	private static BufferedReader getPartitionFile(JobConf conf)
	throws IOException, URISyntaxException {

		System.err.println("Loading split partition file...");
		FileSystem fs = FileSystem.getLocal(conf);
		Path[] cacheFiles = DistributedCache.getLocalCacheFiles(conf);
		
		
//		System.err.println("Local FS:"+fs.toString());
//		URI cacheURI = new URI(conf.get(CACHE_SPLIT_URI_CONFIG));
//		System.err.println("CacheURI: " + cacheURI.toString());
//		long mtime = Long.valueOf(conf.get(CACHE_SPLIT_STAMP_CONFIG));
//		System.err.println("Cache split timestamp: " + mtime);
//		Path localSplitPath = DistributedCache.getLocalCache(cacheURI, conf,
//				conf.getLocalPath(conf.getJobLocalDir()), false, mtime,
//				conf.getWorkingDirectory());
//		System.err.println("LocalSplitPath: " + localSplitPath.toString());
//		FSDataInputStream in = fs.open(localSplitPath);
		FSDataInputStream in = fs.open(cacheFiles[0]);
		InputStreamReader is = new InputStreamReader(in);
		return new BufferedReader(is);
	}

	public void configure(JobConf conf) {
		try {
			System.err.println("Loading split file from cache...");
			loadBoundaries(getPartitionFile(conf));
			System.err.println("Loaded and Sorted split file");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void loadBoundaries(BufferedReader bis) throws IOException {
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

	/**
	 * @return the number of partitions in the configuration file. This is also
	 *         the number of reduce tasks in the job.
	 */
	public int getNumPartitions() {
		return boundaries.length;
	}

	/**
	 * @param key
	 * @param value
	 * @param numReduceTasks
	 * @return int partition index for key
	 */
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
}
