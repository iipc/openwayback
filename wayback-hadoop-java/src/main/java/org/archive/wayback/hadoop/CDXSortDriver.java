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
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.wayback.util.ByteOp;

/**
 * @author brad
 *
 */
public class CDXSortDriver implements Tool {
	Configuration conf = null;
	/**
	 * As hard-coded into the Text RecordWriter
	 */
	public static String TEXT_OUTPUT_DELIM_CONFIG = 
		"mapred.textoutputformat.separator";
	
	private static int countLinesInPath(Path path, Configuration conf) 
	throws IOException {
		FileSystem fs = path.getFileSystem(conf);
		FSDataInputStream is = fs.open(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, ByteOp.UTF8));
		int lineCount = 0;
		while (br.readLine() != null) {
			lineCount++;
		}
		is.close();
		return lineCount;
	}
	
	static int printUsage() {
		System.out.println("cdxsort <split> <input> <output>");
		System.out.println("cdxsort [OPTIONS] <split> <input> <output>");
		System.out.println("\tOPTIONS can be:");
		System.out.println("\t\t-m NUM - try to run with approximately NUM map tasks");
		System.out.println("\t\t--compressed-input - assume input is compressed, even without .gz suffix");
		System.out.println("\t\t--gzip-range - assume input lines are PATH START LENGTH such that a");
		System.out.println("\t\t\t valid gzip record exists in PATH between START and START+LENGTH");
		System.out.println("\t\t\t that contains the records to process");
		System.out.println("\t\t--compress-output - compress output files with GZip");
		System.out.println("\t\t--delimiter DELIM - assume DELIM delimter for input and output, instead of default <SPACE>");
		System.out.println("\t\t--map-global - use the GLOBAL CDX map function, which implies:");
		System.out.println("\t\t\t. extra trailing field indicating HTML meta NOARCHIVE data, which should be omitted, result lines do not include the last field");
		System.out.println("\t\t\t. truncating digest field to 3 digits");
		System.out.println("\t\t\t. column 0 is original URL (identity CDX files)");
		System.out.println();
//		ToolRunner.printGenericCommandUsage(System.out);
		return -1;
	}

	/**
	 * The main driver for sort program. Invoke this method to submit the
	 * map/reduce job.
	 * 
	 * @throws IOException
	 *             When there is communication problems with the job tracker.
	 */
	public int run(String[] args) throws Exception {
		
		String delim = " ";
		
		long desiredMaps = 10;
		boolean compressOutput = false;
		boolean compressedInput = false;
		boolean gzipRange = false;
		List<String> otherArgs = new ArrayList<String>();
		int mapMode = CDXCanonicalizingMapper.MODE_FULL;
		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-m".equals(args[i])) {
					desiredMaps = Integer.parseInt(args[++i]);
				} else if ("--compress-output".equals(args[i])) {
					compressOutput = true;
				} else if ("--compressed-input".equals(args[i])) {
					compressedInput = true;
				} else if ("--gzip-range".equals(args[i])) {
					gzipRange = true;
				} else if ("--delimiter".equals(args[i])) {
					delim = args[++i];
				} else if ("--map-full".equals(args[i])) {
					mapMode = CDXCanonicalizingMapper.MODE_FULL;
				} else if ("--map-global".equals(args[i])) {
					mapMode = CDXCanonicalizingMapper.MODE_GLOBAL;
				} else {
					otherArgs.add(args[i]);
				}
			} catch (NumberFormatException except) {
				System.out.println("ERROR: Integer expected instead of "
						+ args[i]);
				return printUsage();
			} catch (ArrayIndexOutOfBoundsException except) {
				System.out.println("ERROR: Required parameter missing from "
						+ args[i - 1]);
				return printUsage(); // exits
			}
		}

		// Make sure there are exactly 3 parameters left: split input output
		if (otherArgs.size() != 3) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ otherArgs.size() + " instead of 3.");
			return printUsage();
		}
		

		String splitPathString = otherArgs.get(0);
		String inputPathString = otherArgs.get(1);
		String outputPathString = otherArgs.get(2);

		Path splitPath = new Path(splitPathString);
		Path inputPath = new Path(inputPathString);
		Path outputPath = new Path(outputPathString);

		Job job = new Job(getConf(), "cdx-sort");
		Configuration conf = job.getConfiguration();
		job.setJarByClass(CDXSortDriver.class);
		
		job.setMapperClass(CDXCanonicalizingMapper.class);
		
		job.setReducerClass(CDXReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// configure the "map mode"
		CDXCanonicalizingMapper.setMapMode(conf, mapMode);
		
		// set up the delimter:
		conf.set(TEXT_OUTPUT_DELIM_CONFIG, delim);
		
		if (compressOutput) {
			FileOutputFormat.setCompressOutput(job, true);
			FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
		}

		// set up the Partitioner, including number of reduce tasks:
		FileSystem fs = inputPath.getFileSystem(conf);

		int splitCount = countLinesInPath(splitPath, conf);
		System.err.println("Split/Reduce count:" + splitCount);
		job.setNumReduceTasks(splitCount);

		AlphaPartitioner.setPartitionPath(conf, splitPathString);
		job.setPartitionerClass(AlphaPartitioner.class);

		// calculate the byte size to get the correct number of map tasks:
		FileStatus inputStatus = fs.getFileStatus(inputPath);
		long inputLen = inputStatus.getLen();
		long bytesPerMap = (int) inputLen / desiredMaps;

		FileInputFormat.addInputPath(job, inputPath);
		FileInputFormat.setMaxInputSplitSize(job, bytesPerMap);
		if(gzipRange) { 
			job.setInputFormatClass(GZIPRangeLineDereferencingInputFormat.class);			
		} else {
			job.setInputFormatClass(LineDereferencingInputFormat.class);
			if(compressedInput) {
				LineDereferencingRecordReader.forceCompressed(conf);
			}
		}
		FileOutputFormat.setOutputPath(job, outputPath);

		return (job.waitForCompletion(true) ? 0 : 1);
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new CDXSortDriver(), args);
		System.exit(res);
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

}
