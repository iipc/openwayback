package org.archive.wayback.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

public class MonitoredFileSetTest extends TestCase {

	public void testIsChanged() throws IOException, InterruptedException {
		File f1 = File.createTempFile("file-set-", null);
		File f2 = File.createTempFile("file-set-", null);
		writeFile(f1,"one");
		writeFile(f2,"two");
		f1.deleteOnExit();
		f2.deleteOnExit();
		ArrayList<String> l = new ArrayList<String>();
		l.add(f1.getAbsolutePath());
		l.add(f2.getAbsolutePath());
		
		MonitoredFileSet fs = new MonitoredFileSet(l);
		MonitoredFileSet.FileState s1 = fs.getFileState();
		MonitoredFileSet.FileState s2 = fs.getFileState();
		assertFalse(fs.isChanged(s1));
		assertFalse(fs.isChanged(s2));
		Thread.sleep(1001);
		writeFile(f2,"two2");
		MonitoredFileSet.FileState s3 = fs.getFileState();
		assertTrue(fs.isChanged(s2));
		assertTrue(s3.isChanged(s2));
		Thread.sleep(1001);
		assertTrue(fs.isChanged(s2));
		assertFalse(fs.isChanged(s3));
	}
	private void writeFile(File f, String stuff) throws IOException {
		FileOutputStream fos = new FileOutputStream(f,false);
		fos.write(stuff.getBytes());
		fos.close();
	}
}
