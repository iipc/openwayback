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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.archive.io.WriterPoolSettings;
import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCWriter;
import org.archive.util.ArchiveUtils;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ARCCreator {
	private static Logger LOGGER = Logger.getLogger(ARCCreator.class.getName());

	private static String DEFAULT_PREFIX = "test-arc";
	private HashMap<String,RecordComponents> components = 
		new HashMap<String,RecordComponents>();


	private RecordComponents getRecordComponents(final String key) {
		RecordComponents rc = components.get(key);
		if(rc == null) {
			rc = new RecordComponents(key);
			components.put(key,rc);
		}
		return rc;
	}

	private void addFile(File file) {
		String key = null;
		String name = file.getName();
		
		RecordComponents rc;

		if(!file.isFile()) {
			throw new RuntimeException("file " + file.getAbsolutePath() +
					"is not a regular file");
		}
		
		if(name.endsWith(".meta")) {
			key = name.substring(0,name.length() - 5);
			rc = getRecordComponents(key);
			rc.noteMeta();
		} else if(name.endsWith(".body")) {
			key = name.substring(0,name.length() - 5);
			rc = getRecordComponents(key);
			rc.noteBody();
		} else if(name.endsWith(".sh")) {
			key = name.substring(0,name.length() - 3);
			rc = getRecordComponents(key);
			rc.noteScript();
		} else {
			throw new RuntimeException("No key for file " + 
					file.getAbsolutePath());
		}
	}

	/**
	 * Reads all component files (.meta, .body, .sh) in srcDir, and writes
	 * one or more ARC files in tgtDir with names beginning with prefix.
	 * 
	 * @param srcDir
	 * @param tgtDir
	 * @param prefix
	 * @throws IOException
	 */
	public void directoryToArc(File srcDir, File tgtDir, String prefix) 
	throws IOException {
		
		File target[] = {tgtDir};

		ARCWriter writer = new ARCWriter(new AtomicInteger(),
				getSettings(true,prefix,Arrays.asList(target)));
		File sources[] = srcDir.listFiles();
		LOGGER.info("Found " + sources.length + " files in " + srcDir);
		for(int i = 0; i<sources.length; i++) {
			addFile(sources[i]);
		}
		LOGGER.info("Associated " + sources.length + " files in " + srcDir);

		// sort keys and write them all:
		Object arr[] = components.keySet().toArray();
		Arrays.sort(arr);
		for(int i = 0; i < arr.length; i++) {
			String key = (String) arr[i];
			RecordComponents rc = components.get(key);
			rc.writeRecord(writer,srcDir);
			LOGGER.info("Wrote record keyed " + rc.key);			
		}
		writer.close();
		LOGGER.info("Closed arc file named " + 
				writer.getFile().getAbsolutePath());
	}
	private WriterPoolSettings getSettings(final boolean isCompressed,
			final String prefix, final List<File> arcDirs) {
		return new WriterPoolSettings() {
			public List<File> calcOutputDirs() {
				return arcDirs;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public List getMetadata() {
				return null;
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
		};
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if((args.length < 2) || (args.length > 3)) {
			System.err.println("USAGE: srcDir tgtDir [arc_prefix]");
			System.exit(1);
		}
		File srcDir = new File(args[0]);
		File tgtDir = new File(args[1]);
		String prefix = null;
		if(args.length == 3) {
			prefix = args[2];
		} else {
			prefix = DEFAULT_PREFIX;
		}
		ARCCreator creator = new ARCCreator();
		try {
			creator.directoryToArc(srcDir,tgtDir,prefix);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	
	private class RecordComponents {
		private String key;
		private boolean meta;
		private boolean body;
		private boolean script;
		
		/**
		 * constructor
		 * 
		 * @param key
		 */
		public RecordComponents(final String key) {
			super();
			this.key = key;
		}
		private boolean isComplete() {
			return meta && body && script;
		}
		/**
		 * notes that the .meta file has been seen
		 */
		public void noteMeta()   { meta = true;   }
		/**
		 * notes that the .body file has been seen
		 */
		public void noteBody()   { body = true;   }
		/**
		 * notes that the .sh file has been seen
		 */
		public void noteScript() { script = true; }
		/**
		 * checks that all required files have been seen for this record, then
		 * reads and parses the metafile, then write()s the record on the 
		 * writer.
		 * 
		 * @param writer
		 * @param componentDir
		 * @throws IOException
		 */
		public void writeRecord(ARCWriter writer, File componentDir)
		throws IOException {
			if(!isComplete()) {
				throw new RuntimeException("Missing components for key " + key +
						" in directory " + componentDir.getAbsolutePath());
			}
			File metaFile = new File(componentDir,key + ".meta");
			RandomAccessFile raFile = new RandomAccessFile(metaFile, "r");
			String metaLine = raFile.readLine();
			if (metaLine == null) {
				throw new IOException("No meta info in " + 
						metaFile.getAbsolutePath());
			}
			String metaParts[] = metaLine.split(" ");
			if(metaParts.length != 5) {
				throw new IOException("Should be 5 elements in " + 
						metaFile.getAbsolutePath());				
			}
			String uri = metaParts[0];
			String ip = metaParts[1];
			long fetchTS = 0;
			try {
				fetchTS = ArchiveUtils.parse14DigitDate(metaParts[2]).getTime();
			} catch (ParseException e) {
				throw new IOException("unparseable metaline timestamp in " +
						metaFile.getAbsolutePath());
			}
			String type = metaParts[3];
			int length = Integer.valueOf(metaParts[4]).intValue();
			
			File bodyFile = new File(componentDir,key + ".body");
			if(bodyFile.length() != length) {
				throw new IOException("byte mismatch in meta length and body " +
						"byte size for " + bodyFile.getAbsolutePath());
			}
			FileInputStream fis = new FileInputStream(bodyFile);

			writer.write(uri,type,ip,fetchTS,length,fis);
		}
		
	}
}
