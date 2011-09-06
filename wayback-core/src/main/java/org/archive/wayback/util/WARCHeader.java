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
package org.archive.wayback.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.archive.io.WriterPoolSettings;
import org.archive.io.arc.ARCConstants;
import org.archive.io.warc.WARCWriter;
import org.archive.io.warc.WARCWriterPoolSettings;
import org.archive.uid.RecordIDGenerator;
import org.archive.uid.UUIDGenerator;
import org.archive.util.anvl.ANVLRecord;

public class WARCHeader {
	private void writeHeaderRecord(File target, File fieldsSrc, String id)
	throws IOException {

		WARCWriter writer = null;

		BufferedOutputStream bos =
			new BufferedOutputStream(new FileOutputStream(target));

		FileInputStream is = new FileInputStream(fieldsSrc);
		ANVLRecord ar = ANVLRecord.load(is);

		List<String> metadata = new ArrayList<String>(1);
		metadata.add(ar.toString());

		writer = new WARCWriter(new AtomicInteger(),bos,target,getSettings(true, null, null, metadata));
		// Write a warcinfo record with description about how this WARC
		// was made.
		writer.writeWarcinfoRecord(target.getName(), "Made from "
				+ id + " by "
				+ this.getClass().getName());

	}
	private WARCWriterPoolSettings getSettings(final boolean isCompressed,
			final String prefix, final List<File> arcDirs, final List metadata) {
		return new WARCWriterPoolSettings() {
			public List<File> calcOutputDirs() {
				return arcDirs;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public List getMetadata() {
				return metadata;
			}

			public String getPrefix() {
				return prefix;
			}

			public boolean getCompress() {
				return isCompressed;
			}

			public long getMaxFileSizeBytes() {
				return ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE;
			}

			public String getTemplate() {
				return "${prefix}-${timestamp17}-${serialno}";
			}

			public boolean getFrequentFlushes() {
				return false;
			}

			public int getWriteBufferSize() {
				return 4096;
			}

			public RecordIDGenerator getRecordIDGenerator() {
				return new UUIDGenerator();
			}
		};
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("USAGE: tgtWarc fieldsSrc id");
			System.err.println("\ttgtWarc is the path to the target WARC.gz");
			System.err.println("\tfieldsSrc is the path to the text of the record");
			System.err.println("\t\tmake sure each line is terminated by \\r\\n");
			System.err.println("\t\tand that the file ends with a blank, \\r\\n terminiated line");
			System.err.println("\tid is the XXX in:");
			System.err.println("\t\tContent-Description: Made from XXX by org.archive.wayback.util.WARCHeader");
			System.err.println("\t\tof the header record... header...");
			System.exit(1);
		}
		File target = new File(args[0]);
		File fieldSrc = new File(args[1]);
		String id = args[2];
		WARCHeader header = new WARCHeader();
		try {
			header.writeHeaderRecord(target, fieldSrc, id);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

}
