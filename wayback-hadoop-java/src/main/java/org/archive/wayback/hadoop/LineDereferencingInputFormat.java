package org.archive.wayback.hadoop;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/**
 * FileInputFormat subclass which assumes the configured input files are lines
 * containing hdfs:// pointers to the actual Text data.
 * 
 * @author brad
 *
 */
public class LineDereferencingInputFormat extends FileInputFormat<Text, Text>{
	TextInputFormat tif = null;

	@Override
	public List<InputSplit> getSplits(JobContext context) throws IOException {
		if(tif == null) {
			tif = new TextInputFormat();
		}
		return tif.getSplits(context);
	}

	@Override
	public RecordReader<Text, Text> createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException,
			InterruptedException {
		return new LineDereferencingRecordReader();
	}
}
