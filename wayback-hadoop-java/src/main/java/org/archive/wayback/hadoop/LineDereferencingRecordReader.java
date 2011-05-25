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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.archive.wayback.util.ByteOp;

/**
 * RecordReader which reads pointers to actual files from an internal 
 * LineRecordReader, producing a LineRecordReader for the files pointed to by
 * the actual input.
 * 
 * @author brad
 *
 */
public class LineDereferencingRecordReader extends RecordReader<Text, Text>{
	LineRecordReader internal = new LineRecordReader();
	
	
	protected static final String FORCE_COMPRESSED_FLAG = "line-reref.force-compressed";
	
	FileSystem fileSystem = null;
	Text key = null;
	Text value = null;
	BufferedReader curReader = null;
	String curFile = null;
	long curLine = 0;
	float progress = 0.0f;
	boolean forceCompressed = false;
	public static void forceCompressed(Configuration conf) {
		conf.setBoolean(FORCE_COMPRESSED_FLAG, true);
	}
	
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		forceCompressed = conf.getBoolean(FORCE_COMPRESSED_FLAG, false);
		FileSplit fileSplit = (FileSplit) split;
		fileSystem = fileSplit.getPath().getFileSystem(conf);
		internal.initialize(split, context);
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if(key == null) {
			key = new Text();
		}
		if(value == null) {
			value = new Text();
		}
		while(true) {
			if(curReader == null) {
				// are there more?
				if(internal.nextKeyValue()) {
					progress = internal.getProgress();
					curFile = internal.getCurrentValue().toString();
					Path path = new Path(curFile);
					InputStream is = fileSystem.open(path);
					// TODO: use the real Codec stuff..
					if(forceCompressed || curFile.endsWith(".gz")) {
//						is = new GZIPInputStream(is);
						is = new MultiMemberGZIPInputStream(is);
					}
					curReader = new BufferedReader(new InputStreamReader(is,ByteOp.UTF8));
					
				} else {
					// all done:
					return false;
				}
			}
			// try to read another line:
			String nextLine = curReader.readLine();
			if(nextLine != null) {
				key.set(curFile+":"+curLine);
				value.set(nextLine);
				curLine++;
				return true;
			}
			curReader = null;
			curFile = null;
			curLine = 0;
		}
	}

	@Override
	public Text getCurrentKey() throws IOException,
			InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return progress;
	}

	@Override
	public void close() throws IOException {
		internal.close();
	}
}
