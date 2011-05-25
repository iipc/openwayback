package org.archive.wayback.hadoop;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.archive.wayback.util.ByteOp;

public class GZIPRangeLineDereferencingRecordReader extends LineDereferencingRecordReader{
	String curInputLine = null;
	FSDataInputStream fsdis = null;
	long curStart = 0;
	byte[] buffer = null;
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
					curInputLine = internal.getCurrentValue().toString();
					String[] parts = curInputLine.split(" ");
					if(parts.length != 3) {
						throw new IOException("Bad format line(" + curInputLine +")");
					}
					String newFile = parts[0];
					if(fsdis != null) {
						if(!newFile.equals(curFile)) {
							// close old and open new, otherwise we can just
							// do another read on the current one:
							fsdis.close();
							curFile = newFile;
							Path path = new Path(curFile);
							fsdis = fileSystem.open(path);
						}
					} else {
						curFile = newFile;
						Path path = new Path(curFile);
						fsdis = fileSystem.open(path);						
					}
					curFile = parts[0];
					curStart = Long.parseLong(parts[1]);
					int length = Integer.parseInt(parts[2]);
					if(buffer == null) {
						buffer = new byte[length];
					} else if (buffer.length < length) {
						buffer = new byte[length];
					}
					fsdis.read(curStart,buffer,0,length);
					// the whole chunk is now in buffer:
					InputStream is = 
						new GZIPInputStream(new ByteArrayInputStream(buffer,0,length));
					curReader = new BufferedReader(new InputStreamReader(is,ByteOp.UTF8));
					curLine = 0;

				} else {
					// all done:
					return false;
				}
			}
			// try to read another line:
			String nextLine = curReader.readLine();
			if(nextLine != null) {
				key.set(curFile+":"+curStart+":"+curLine);
				value.set(nextLine);
				curLine++;
				return true;
			}
			curReader.close();
			curReader = null;
		}
	}

}
