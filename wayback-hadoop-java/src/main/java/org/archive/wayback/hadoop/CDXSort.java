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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.URIException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;

import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import org.archive.wayback.util.ByteOp;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

public class CDXSort extends Configured implements Tool {
	private RunningJob jobResult = null;

	static int printUsage() {
		System.out.println("cdxsort <split> <input> <output>");
		ToolRunner.printGenericCommandUsage(System.out);
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

		boolean compressOutput = false;
		boolean dereferenceInputs = false;
		boolean canonicalize = false;
		boolean funkyInput = false;

		JobConf jobConf = new JobConf(getConf(), CDXSort.class);
		jobConf.setJobName("cdxsort");

		jobConf.setMapperClass(IdentityMapper.class);
		jobConf.setReducerClass(IdentityReducer.class);

		JobClient client = new JobClient(jobConf);
		ClusterStatus cluster = client.getClusterStatus();

		List<String> otherArgs = new ArrayList<String>();

		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-m".equals(args[i])) {
					jobConf.setNumMapTasks(Integer.parseInt(args[++i]));
				} else if ("--compress-output".equals(args[i])) {
					compressOutput = true;
				} else if ("--funky-input".equals(args[i])) {
					funkyInput = true;
				} else if ("--dereference-inputs".equals(args[i])) {
					dereferenceInputs = true;
				} else if ("--canonicalize".equals(args[i])) {
					canonicalize = true;
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

		String splitPath = otherArgs.get(0);
		String inputPath = otherArgs.get(1);
		String outputPath = otherArgs.get(2);

		// load the split file, find and set the number of reduces
		AlphaPartitioner partitioner = new AlphaPartitioner();
		File localSplitFile = new File(splitPath);
		FileInputStream fis = new FileInputStream(localSplitFile);
		InputStreamReader isr = new InputStreamReader(fis,ByteOp.UTF8);
		BufferedReader bis = new BufferedReader(isr);
//		try {
//			partitioner.loadBoundaries(bis);
//		} catch (IOException except) {
//			System.err.println("ERROR: Problem loading file " + splitPath);
//			return printUsage(); // exits
//		}
//		jobConf.setNumReduceTasks(partitioner.getNumPartitions());
//
//		// copy the split file into the FS, add to the DistributedCache:
////		AlphaPartitioner.setPartitionFile(jobConf, localSplitFile);
//		AlphaPartitioner.setSplitCache(jobConf, localSplitFile);
//		System.err.println("uploaded split file to FS and DistributedCache");
//
//		// Set job configs:
//		jobConf.setInputFormat(TextInputFormat.class);
//
//		jobConf.setOutputFormat(TextOutputFormat.class);
//		if (canonicalize) {
//			jobConf.setMapperClass(CDXCanonicalizerMapClass.class);
//		} else {
//			jobConf.setMapperClass(CDXMapClass.class);
//		}
//		jobConf.setOutputKeyClass(Text.class);
//		jobConf.setOutputValueClass(Text.class);
//		jobConf.set("mapred.textoutputformat.separator", " ");
//		jobConf.setPartitionerClass(AlphaPartitioner.class);

		int inputCount = 0;
		// Set job input:
		if (dereferenceInputs) {

			// SO SLOW... can't add one at a time...
//			FileReader is2 = new FileReader(new File(inputPath));
//			BufferedReader bis2 = new BufferedReader(is2);
//			while (true) {
//				String line = bis2.readLine();
//				if (line == null) {
//					break;
//				}
//				FileInputFormat.addInputPath(jobConf, new Path(line));
//				inputCount++;
//				System.err.println("Added path(" + inputCount + "): " + line);
//			}

			
			
			// PASS 2:
//			FileReader is2 = new FileReader(new File(inputPath));
//			BufferedReader bis2 = new BufferedReader(is2);
//			ArrayList<String> list = new ArrayList<String>();
//			
//			while (true) {
//				String line = bis2.readLine();
//				if (line == null) {
//					break;
//				}
//				list.add(line);
//				inputCount++;
//			}
//			Path arr[] = new Path[list.size()];
//			for(int i=0; i < list.size(); i++) {
//				arr[i] = new Path(list.get(i));
//			}
//			FileInputFormat.setInputPaths(jobConf, arr);

			// PASS 3:
			if(funkyInput) {
				jobConf.setMapperClass(FunkyDeReffingCDXCanonicalizerMapClass.class);
			} else {
				jobConf.setMapperClass(DeReffingCDXCanonicalizerMapClass.class);
			}
			FileInputFormat.setInputPaths(jobConf, new Path(inputPath));
			inputCount = 1;

			
		} else {
			FileInputFormat.setInputPaths(jobConf, new Path(inputPath));
			inputCount = 1;
		}
		
		// Set job output:
		FileOutputFormat.setOutputPath(jobConf, new Path(outputPath));

		if (compressOutput) {
			FileOutputFormat.setCompressOutput(jobConf, true);
			FileOutputFormat.setOutputCompressorClass(jobConf, GzipCodec.class);
		}

//		System.out.println("Running on " + cluster.getTaskTrackers()
//				+ " nodes, processing " + inputCount + " files/directories"
//				+ " into " + outputPath + " with "
//				+ partitioner.getNumPartitions() + " reduces.");
		Date startTime = new Date();
		System.out.println("Job started: " + startTime);
		jobResult = JobClient.runJob(jobConf);
		Date end_time = new Date();
		System.out.println("Job ended: " + end_time);
		System.out.println("The job took "
				+ (end_time.getTime() - startTime.getTime()) / 1000
				+ " seconds.");
		return 0;
	}

	/**
	 * Mapper which reads a canonicalized CDX line, splitting into: key - URL +
	 * timestamp val - everything else
	 * 
	 * @author brad
	 * @version $Date$, $Revision$
	 */
	public static class CDXMapClass extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {

		private Text outKey = new Text();
		private Text outValue = new Text();

		public void map(LongWritable lineNumber, Text line,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			 String tmp = line.toString();
			 int i1 = tmp.lastIndexOf(' ');
			 if(i1 > 0) {
				 outKey.set(tmp.substring(0,i1));
				 outValue.set(tmp.substring(i1+1));
				 output.collect(outKey, outValue);
			 } else {
				 System.err.println("Problem with line(" + tmp + ")");
			 }
			 
//			 output.collect(line, outValue);
			// reporter.setStatus("Running");
		}
	}
	public static class FunkyDeReffingCDXCanonicalizerMapClass extends DeReffingCDXCanonicalizerMapClass {
		protected Mapper<LongWritable, Text, Text, Text> getInner() {
			return new FunkyCDXCanonicalizerMapClass();
		}
	}
	public static class DeReffingCDXCanonicalizerMapClass extends MapReduceBase
			implements Mapper<LongWritable, Text, Text, Text> {

		protected Mapper<LongWritable, Text, Text, Text> getInner() {
			return new CDXCanonicalizerMapClass();
		}
		/* (non-Javadoc)
		 * @see org.apache.hadoop.mapred.Mapper#map(java.lang.Object, java.lang.Object, org.apache.hadoop.mapred.OutputCollector, org.apache.hadoop.mapred.Reporter)
		 */
		public void map(LongWritable lineNo, Text urlText,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			LongWritable lw = new LongWritable();
			Text tmp = new Text();
//			CDXCanonicalizerMapClass inner = new CDXCanonicalizerMapClass();
			Mapper<LongWritable, Text, Text, Text> inner = getInner();
			// arg 1 is a URL

			String urlString = urlText.toString();
			InputStream is = null;
			FileSystem fs = null;
			if(urlString.startsWith("http://")) {
				URL u = new URL(urlString.toString());
				System.err.println("Openning URL stream for:" + urlString);
				is = u.openStream();
			} else {
				System.err.println("Creating default Filesystem for:" + urlString);

				fs = FileSystem.get(new Configuration(true));
				Path p = new Path(urlString);
//				FSDataInputStream fsdis = fs.open(p);
				is = fs.open(p);

			}
			if(urlString.endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is,ByteOp.UTF8));
				String tmpS = null;
				long line = 0;
				while((tmpS = br.readLine()) != null) {
					lw.set(line++);
					tmp.set(tmpS);
					inner.map(lw, tmp, output, reporter);
				}
				is.close();
				if(fs != null) {
					fs.close();
				}
			} catch (IOException e) {
				System.err.println("IOException with url:" + urlString);
				e.printStackTrace();
				throw e;
			}
		}
		
	}
	/**
	 * Mapper which reads an identity CDX line, outputting: key - canonicalized
	 * original URL + timestamp val - everything else
	 * 
	 * @author brad
	 * @version $Date$, $Revision$
	 */
	public static class CDXCanonicalizerMapClass extends MapReduceBase
			implements Mapper<LongWritable, Text, Text, Text> {

		private Text outKey = new Text();
		private Text outValue = new Text();
		AggressiveUrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
		private StringBuilder ksb = new StringBuilder();

		private int i1 = 0;
		private int i2 = 0;
		private int i3 = 0;
		private int i4 = 0;

		public void map(LongWritable lineNumber, Text line,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String s = line.toString();

			boolean problems = true;
			i1 = s.indexOf(' ');
			if(i1 > 0) {
				i2 = s.indexOf(' ', i1 + 1);
				if(i2 > 0) {
					i3 = s.indexOf(' ', i2 + 1);
					if(i3 > 0) {
						i4 = s.lastIndexOf(' ');
						if(i4 > i3) {
							try {
								ksb.setLength(0);
								ksb.append(canonicalizer.urlStringToKey(s.substring(i2 + 1, i3)));
								ksb.append(s.substring(i1,i4));
								outKey.set(ksb.toString());
								outValue.set(s.substring(i4+1));
								output.collect(outKey, outValue);
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
	}

	/**
	 * Mapper which reads an identity Funky format CDX line, outputting: 
	 *   key - canonicalized original URL + timestamp
	 *   val - everything else
	 * 
	 * input lines are a hybrid format:
	 * 
	 *   ORIG_URL
	 *   DATE
	 *   '-' (literal)
	 *   MIME
	 *   HTTP_CODE
	 *   SHA1
	 *   REDIRECT
	 *   START_OFFSET
	 *   ARC_PREFIX (sans .arc.gz)
	 *   ROBOT_FLAG (combo of AIF - no: Archive,Index,Follow, or '-' if none)
	 *  
	 *   Ex:
	 *   http://www.myow.de:80/news_show.php? 20061126032815 - text/html 200 DVKFPTOJGCLT3G5GUVLCETHLFO3222JM - 91098929 foo A
	 *   
	 *	Need to:
	 *	. replace col 3 with orig url
	 *  . replace col 1 with canonicalized orig url
	 *  . replace SHA1 with first 4 digits of SHA1
	 *  . append .arc.gz to ARC_PREFIX
	 *  . omit lines with ROBOT_FLAG containing 'A'
	 *  . remove last column
	 * 
	 * @author brad
	 * @version $Date$, $Revision$
	 */
	public static class FunkyCDXCanonicalizerMapClass extends MapReduceBase
			implements Mapper<LongWritable, Text, Text, Text> {

		private static int SHA1_DIGITS = 3;
		private Text outKey = new Text();
		private Text outValue = new Text();
		AggressiveUrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
		private StringBuilder ksb = new StringBuilder();
		private StringBuilder vsb = new StringBuilder();

		private int i1 = 0;
		private int i2 = 0;
		private int i3 = 0;
		private int i4 = 0;

		public void map(LongWritable lineNumber, Text line,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String s = line.toString();

			String parts[] = s.split(" ");
			boolean problems = true;
			if(parts.length == 10) {
				if(!parts[9].contains("A")) {
					ksb.setLength(0);
					vsb.setLength(0);
					try {
						ksb.append(canonicalizer.urlStringToKey(parts[0])).append(" ");
						ksb.append(parts[1]); // date
						vsb.append(parts[0]).append(" "); // orig_url
						vsb.append(parts[3]).append(" "); // MIME
						vsb.append(parts[4]).append(" "); // HTTP_CODE
						vsb.append(parts[5].substring(0, SHA1_DIGITS)).append(" "); // SHA1
						vsb.append(parts[6]).append(" "); // redirect
						vsb.append(parts[7]).append(" "); // start_offset
						vsb.append(parts[8]).append(".arc.gz"); // arc_prefix
						outKey.set(ksb.toString());
						outValue.set(vsb.toString());
						output.collect(outKey, outValue);
					} catch (URIException e) {
						System.err.println("Failed Canonicalize:("+parts[0]+
								") in ("+parts[8]+"):("+parts[7]+")");
					}
				}
			} else {
				System.err.println("Funky: Problem with line("+s+")");
			}
		}
	}	
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new CDXSort(), args);
		System.exit(res);
	}

	/**
	 * Get the last job that was run using this instance.
	 * 
	 * @return the results of the last job that was run
	 */
	public RunningJob getResult() {
		return jobResult;
	}
}
