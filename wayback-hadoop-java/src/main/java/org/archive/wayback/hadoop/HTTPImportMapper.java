package org.archive.wayback.hadoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class HTTPImportMapper extends Mapper<LongWritable, Text, Text, Text> {
	public final int BUFSIZ = 4096;
	Path target = null;
	FileSystem filesystem = null;
	Text doneText = null;
	HttpClient client = null;
	public HTTPImportMapper() {
		
	}
	public void init2() {
		System.err.println("Init map...");
	}
	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		String targetString = conf.get(HTTPImportJob.HTTP_IMPORT_TARGET);
		if(targetString == null) {
			throw new IOException("No " + HTTPImportJob.HTTP_IMPORT_TARGET 
					+ " specified");
		}
		target = new Path(targetString);
		filesystem = target.getFileSystem(conf);
		doneText = new Text("Done");
		client = new HttpClient();
	}

	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		String valueS = value.toString();
		String name;
		String url = valueS;
		int idx = valueS.indexOf(' ');
		if(idx == -1) {
			URL tmpUrl = new URL(valueS);
			name = tmpUrl.getPath();
			if(name.contains("/")) {
				name = name.substring(name.lastIndexOf('/')+1);
			}
		} else {
			name = valueS.substring(0,idx);
			url = valueS.substring(idx+1);
		}
		Path thisTarget = new Path(target,name);
		doCopy(url, thisTarget);
		context.write(value, doneText);
	}
	
	private long getURLLengthByHead(String url) throws IOException {
        HeadMethod head = new HeadMethod(url);
        long urlLen = -1;
	    // execute the method and handle any error responses.
        try {
	        int code = client.executeMethod(head);
	        if(code != 200) {
	        	throw new IOException("Non-200 for HEAD:" + url);
	        }
	        urlLen = head.getResponseContentLength();
	        // discard: hope it's really empty (HEAD) and thus small...
	        head.getResponseBody();
        } finally {
        	head.releaseConnection();
        }
        return urlLen;
    }

	private long getPathLength(Path path) throws IOException {
		FileStatus stat = null;
		try {
			stat = filesystem.getFileStatus(path);
			// present.. check by size:
		} catch (FileNotFoundException e) {
			return -1;
		}
		return stat.getLen();
	}

	
	private void doCopy(String url, Path target) throws IOException {
		// Check if the target exists (from previous map)
		long targetLen = getPathLength(target);
		long urlLen = -1;
		if(targetLen > -1) {
			// there's a file in the filesystem already, see if it's the
			// same length:
			urlLen = getURLLengthByHead(url);
			if(urlLen == targetLen) {
				// same size, assume it's done:
				return;
			}
			// diff length, do copy again, first remove old:
			if(!filesystem.delete(target, false)) {
				throw new IOException("Failed to delete old copy");
			}
		}
		// do the copy:
		FSDataOutputStream out = filesystem.create(target, false);
		GetMethod get = new GetMethod(url);
		long copied = 0;
		try {
			int code = client.executeMethod(get);
			if(code != 200) {
				throw new IOException("Non 200 on GET: " + url);
			}
			urlLen = get.getResponseContentLength();
			InputStream in = get.getResponseBodyAsStream();
			byte buffer[] = new byte[BUFSIZ];
		    for(int cbread; (cbread = in.read(buffer)) >= 0; ) {
		    	out.write(buffer, 0, cbread);
		    	copied += cbread;
		    }
		} finally {
			get.releaseConnection();
		    out.close();
		}
		if(copied != urlLen) {
			// ack.. what went wrong?
			throw new IOException("Wrong copy length want(" + urlLen 
					+ ") got(" + copied + ") URL:" + url);
		}
	}
}
