package org.archive.wayback.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class HTTPImportJob extends Configured implements Tool {
	Configuration conf = null;
	public final static String HTTP_IMPORT_TARGET = "http-import.target";
	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		
	}


	
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "http-import");
		Configuration conf = job.getConfiguration();
		job.setJarByClass(HTTPImportJob.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(HTTPImportMapper.class);

		int i = 0;
		int numMaps = 10;
		while(i < args.length -1) {
			if(args[i].equals("-m")) {
				i++;
				numMaps = Integer.parseInt(args[i]);
				i++;
			} else {
				break;
			}
		}
		if(args.length - 3 != i) {
			throw new IllegalArgumentException("wrong number of args...");
		}
		Path inputPath = new Path(args[i]);
		Path outputPath = new Path(args[i+1]);
		Path targetPath = new Path(args[i+2]);

		TextInputFormat.addInputPath(job, inputPath);
		FileOutputFormat.setOutputPath(job, outputPath);
		conf.set(HTTP_IMPORT_TARGET, targetPath.toString());

		conf.setBoolean("mapred.map.tasks.speculative.execution", false);
		
		FileSystem fs = inputPath.getFileSystem(conf);
		FileStatus inputStatus = fs.getFileStatus(inputPath);
		long inputLen = inputStatus.getLen();
		long bytesPerMap = (int) inputLen / numMaps;

		FileInputFormat.setMaxInputSplitSize(job, bytesPerMap);

		
		return (job.waitForCompletion(true) ? 0 : 1);
	}
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HTTPImportJob(), args);
		System.exit(res);
	}

}
