/**
 * 
 */
package org.archive.wayback.core;

import java.io.IOException;

import org.archive.io.arc.ARCRecord;

/**
 * @author brad
 *
 */
public class Resource {

	ARCRecord arcRecord = null; // probably this should inherit from ARCRecord...

	public Resource(final ARCRecord rec) {
		super();
		arcRecord = rec;
	}

	public String dumpRaw() throws IOException {
		arcRecord.skipHttpHeader();
		String content = "";

		byte[] outputBuffer = new byte[8 * 1024];
		int read = outputBuffer.length;
		while ((read = arcRecord.read(outputBuffer, 0, outputBuffer.length)) != -1) {
			String tmpString = new String(outputBuffer, 0, read);
			content = content.concat(tmpString);
			//System.out.write(outputBuffer, 0, read);
		}
		//System.out.flush();

		return content;
	}

	public ARCRecord getArcRecord() {
		return arcRecord;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
