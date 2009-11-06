package org.archive.wayback.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
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
		FileReader is = new FileReader(localSplitFile);
		BufferedReader bis = new BufferedReader(is);
		try {
			partitioner.loadBoundaries(bis);
		} catch (IOException except) {
			System.err.println("ERROR: Problem loading file " + splitPath);
			return printUsage(); // exits
		}
		jobConf.setNumReduceTasks(partitioner.getNumPartitions());

		// copy the split file into the FS, add to the DistributedCache:
		AlphaPartitioner.setPartitionFile(jobConf, localSplitFile);
		System.err.println("uploaded split file to FS and DistributedCache");

		// Set job configs:
		jobConf.setInputFormat(TextInputFormat.class);

		jobConf.setOutputFormat(TextOutputFormat.class);
		if (canonicalize) {
			jobConf.setMapperClass(CDXCanonicalizerMapClass.class);
		} else {
			jobConf.setMapperClass(CDXMapClass.class);
		}
		jobConf.setOutputKeyClass(Text.class);
		jobConf.setOutputValueClass(Text.class);
		jobConf.set("mapred.textoutputformat.separator", " ");
		jobConf.setPartitionerClass(AlphaPartitioner.class);

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

			FileReader is2 = new FileReader(new File(inputPath));
			BufferedReader bis2 = new BufferedReader(is2);
			ArrayList<String> list = new ArrayList<String>();
			
			while (true) {
				String line = bis2.readLine();
				if (line == null) {
					break;
				}
				list.add(line);
				inputCount++;
			}
			Path arr[] = new Path[list.size()];
			for(int i=0; i < list.size(); i++) {
				arr[i] = new Path(list.get(i));
			}
			FileInputFormat.setInputPaths(jobConf, arr);

			
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

		System.out.println("Running on " + cluster.getTaskTrackers()
				+ " nodes, processing " + inputCount + " files/directories"
				+ " into " + outputPath + " with "
				+ partitioner.getNumPartitions() + " reduces.");
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
							ksb.setLength(0);
							ksb.append(canonicalizer.urlStringToKey(s.substring(i2 + 1, i3)));
							ksb.append(s.substring(i1,i4));
							outKey.set(ksb.toString());
							outValue.set(s.substring(i4+1));
							output.collect(outKey, outValue);
							problems = false;
						}
					}
				}
			}
			if(problems) {
				System.err.println("Problem with line("+s+")");
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
