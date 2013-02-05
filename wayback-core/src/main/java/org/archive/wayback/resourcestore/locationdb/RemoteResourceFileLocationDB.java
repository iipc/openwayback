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
package org.archive.wayback.resourcestore.locationdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.ParameterFormatter;
import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.util.ByteOp;
import org.archive.wayback.util.WrappedCloseableIterator;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteResourceFileLocationDB implements ResourceFileLocationDB {
	private static final Logger LOGGER = Logger.getLogger(RemoteResourceFileLocationDB
			.class.getName());

	private final static String ARC_SUFFIX = ".arc";
	private final static String ARC_GZ_SUFFIX = ".arc.gz";
	private final static String WARC_SUFFIX = ".warc";
	private final static String WARC_GZ_SUFFIX = ".warc.gz";
	private final static String OK_RESPONSE_PREFIX = "OK ";
    private HttpClient client = null;
	
	private String serverUrl = null;

	/**
	 * @param serverUrl
	 */
	public RemoteResourceFileLocationDB(final String serverUrl) {
		super();
		this.serverUrl = serverUrl;
		this.client = new HttpClient();
	}
	
	/**
	 * @return long value representing the current end "mark" of the db log
	 * @throws IOException
	 */
	public long getCurrentMark() throws IOException {
		NameValuePair[] args = {
				new NameValuePair(
						ResourceFileLocationDBServlet.OPERATION_ARGUMENT,
						ResourceFileLocationDBServlet.GETMARK_OPERATION),
		};		
		return Long.parseLong(doGetMethod(args));
	}
	
	/**
	 * @param start
	 * @param end
	 * @return Iterator of file names between marks start and end
	 * @throws IOException
	 */
	public CloseableIterator<String> getNamesBetweenMarks(long start, long end) 
	throws IOException {
		NameValuePair[] args = {
				new NameValuePair(
						ResourceFileLocationDBServlet.OPERATION_ARGUMENT,
						ResourceFileLocationDBServlet.GETRANGE_OPERATION),
				new NameValuePair(
						ResourceFileLocationDBServlet.START_ARGUMENT,
						String.valueOf(start)),
				new NameValuePair(
						ResourceFileLocationDBServlet.END_ARGUMENT,
						String.valueOf(end))
		};		
		return new WrappedCloseableIterator<String>(
				Arrays.asList(doGetMethod(args).split("\n")).iterator());
	}
	
	/**
	 * return an array of String URLs for all known locations of the file
	 * in the DB.
	 * @param name
	 * @return String[] of URLs to arcName
	 * @throws IOException
	 */
	public String[] nameToUrls(final String name) throws IOException {

		NameValuePair[] args = {
				new NameValuePair(
						ResourceFileLocationDBServlet.OPERATION_ARGUMENT,
						ResourceFileLocationDBServlet.LOOKUP_OPERATION),
					
				new NameValuePair(
						ResourceFileLocationDBServlet.NAME_ARGUMENT,
						name)
		};
		String locations = doGetMethod(args);
		if(locations != null) {
			return locations.split("\n");
		}
		return null;
	}
	

	/**
	 * add an Url location for an arcName, unless it already exists
	 * @param name
	 * @param url
	 * @throws IOException
	 */
	public void addNameUrl(final String name, final String url) 
	throws IOException {
		doPostMethod(ResourceFileLocationDBServlet.ADD_OPERATION, name, url);
	}

	/**
	 * remove a single url location for a name, if it exists
	 * @param name
	 * @param url
	 * @throws IOException
	 */
	public void removeNameUrl(final String name, final String url) 
	throws IOException {
		doPostMethod(ResourceFileLocationDBServlet.REMOVE_OPERATION, name, url);
	}
	
	private String doGetMethod(NameValuePair[] data) throws IOException {
		ParameterFormatter formatter = new ParameterFormatter();
		formatter.setAlwaysUseQuotes(false);
		StringBuilder finalUrl = new StringBuilder(serverUrl);
		if(data.length > 0) {
			finalUrl.append("?");
		}
		for(int i = 0; i < data.length; i++) {
			if(i == 0) {
				finalUrl.append("?");
			} else {
				finalUrl.append("&");
			}
			finalUrl.append(formatter.format(data[i]));
		}

		GetMethod method = new GetMethod(finalUrl.toString()); 
		
        int statusCode = client.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Method failed: " + method.getStatusLine());
        }
        String responseString = method.getResponseBodyAsString();
        if(!responseString.startsWith(OK_RESPONSE_PREFIX)) {
        	if(responseString.startsWith(ResourceFileLocationDBServlet.NO_LOCATION_PREFIX)) {
        		return null;
        	}
        	throw new IOException(responseString);
        }
        return responseString.substring(OK_RESPONSE_PREFIX.length()+1); 
	}
	
	private void doPostMethod(final String operation, final String arcName,
			final String arcUrl) 
	throws IOException {
	    PostMethod method = new PostMethod(serverUrl);
        NameValuePair[] data = {
                new NameValuePair(ResourceFileLocationDBServlet.OPERATION_ARGUMENT,
                		operation),
                new NameValuePair(ResourceFileLocationDBServlet.NAME_ARGUMENT,
                   		arcName),
                new NameValuePair(ResourceFileLocationDBServlet.URL_ARGUMENT,
                   		arcUrl)
              };
        method.setRequestBody(data);
        int statusCode = client.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Method failed: " + method.getStatusLine());
        }
        String responseString = method.getResponseBodyAsString();
        if(!responseString.startsWith(OK_RESPONSE_PREFIX)) {
        	throw new IOException(responseString);
        }
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDB#shutdown()
	 */
	public void shutdown() throws IOException {
		// NO-OP
	}

	private static void USAGE(String message) {
		System.err.print("USAGE: " + message + "\n" +
				"\t[lookup|add|remove|sync] ...\n" +
				"\n" +
				"\t lookup LOCATION-DB-URL ARC\n" +
				"\t\temit all known URLs for arc ARC\n" +
				"\n" +
				"\t add LOCATION-DB-URL ARC URL\n" +
				"\t\tinform locationDB that ARC is located at URL\n" +
				"\n" +
				"\t remove LOCATION-DB-URL ARC URL\n" +
				"\t\tremove reference to ARC at URL in locationDB\n" +
				"\n" +
				"\t sync LOCATION-DB-URL DIR DIR-URL\n" +
				"\t\tscan directory DIR, and submit all ARC files therein\n" +
				"\t\tto locationDB at url DIR-URL/ARC\n" +
				"\n" +
				"\t get-mark LOCATION-DB-URL\n" +
				"\t\temit an identifier for the current marker in the \n" +
				"\t\tlocationDB log. These identifiers can be used with the\n" +
				"\t\tmark-range operation.\n" +
				"\n" +
				"\t mark-range LOCATION-DB-URL START END\n" +
				"\t\temit to STDOUT one line with the name of all ARC files\n" +
				"\t\tadded to the locationDB between marks START and END\n" +
				"\n" +
				"\t add-stream LOCATION-DB-URL\n" +
				"\t\tread lines from STDIN formatted like:\n" +
				"\t\t\tNAME<SPACE>URL\n" +
				"\t\tand for each line, inform locationDB that file NAME is\n" +
				"\t\tlocated at URL\n"
				);
		System.exit(2);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 2) {
			USAGE("");
			System.exit(1);
		}
		String operation = args[0];
		String dbUrl = args[1];
		if(!dbUrl.startsWith("http://")) {
			USAGE("URL argument 1 must begin with http://");
		}

		RemoteResourceFileLocationDB locationClient = 
			new RemoteResourceFileLocationDB(dbUrl);
		
		if(operation.equalsIgnoreCase("add-stream")) {
			BufferedReader r = new BufferedReader(
					new InputStreamReader(System.in,ByteOp.UTF8));
			String line;
			try {
				while((line = r.readLine()) != null) {
					String parts[] = line.split(" ");
					if(parts.length != 2) {
						System.err.println("Bad input(" + line + ")");
						System.exit(2);
					}
					locationClient.addNameUrl(parts[0],parts[1]);
					System.out.println("Added\t" + parts[0] + "\t" + parts[1]);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		} else {
			if(args.length < 3) {
				USAGE("");
				System.exit(1);
			}
			String name = args[2];
			if(operation.equalsIgnoreCase("lookup")) {
				if(args.length < 3) {
					USAGE("lookup LOCATION-URL ARC");
				}
				try {
					String[] locations = locationClient.nameToUrls(name);
					if(locations == null) {
						System.err.println("No locations for " + name);
						System.exit(1);
					}
					for(int i=0; i <locations.length; i++) {
						System.out.println(locations[i]);
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
				
			} else if(operation.equalsIgnoreCase("get-mark")) {
				if(args.length != 2) {
					USAGE("get-mark LOCATION-URL");
				}
				try {
					long mark = locationClient.getCurrentMark();
					System.out.println(mark);
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
				
			} else if(operation.equalsIgnoreCase("mark-range")) {
				if(args.length != 4) {
					USAGE("mark-range LOCATION-URL START END");
				}
				long start = Long.parseLong(args[3]);
				long end = Long.parseLong(args[4]);
				try {
					Iterator<String> it = 
						locationClient.getNamesBetweenMarks(start,end);
					while(it.hasNext()) {
						String next = (String) it.next();
						System.out.println(next);
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
				
				
			} else if(operation.equalsIgnoreCase("add")) {
				if(args.length != 4) {
					USAGE("add LOCATION-URL ARC ARC-URL");
				}
				String url = args[3];
				if(!url.startsWith("http://")) {
					USAGE("ARC-URL argument 4 must begin with http://");
				}
				try {
					locationClient.addNameUrl(name,url);
					System.out.println("OK");
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
				
			} else if(operation.equalsIgnoreCase("remove")) {
				
				if(args.length != 4) {
					USAGE("remove LOCATION-URL FILE-NAME FILE-URL");
				}
				String url = args[3];
				if(!url.startsWith("http://")) {
					USAGE("URL argument 4 must begin with http://");
				}
				try {
					locationClient.removeNameUrl(name,url);
					System.out.println("OK");
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
	
			} else if(operation.equalsIgnoreCase("sync")) {
				
				if(args.length != 4) {
					USAGE("sync LOCATION-URL DIR DIR-URL");
				}
				File dir = new File(name);
				String dirUrl = args[3];
				if(!dirUrl.startsWith("http://")) {
					USAGE("DIR-URL argument 4 must begin with http://");
				}
				try {
					if(!dir.isDirectory()) {
						USAGE("DIR " + name + " is not a directory");
					}
					
					FileFilter filter = new FileFilter() {
						public boolean accept(File daFile) {
							return daFile.isFile() && 
							(daFile.getName().endsWith(ARC_SUFFIX) ||
								daFile.getName().endsWith(ARC_GZ_SUFFIX) ||
								daFile.getName().endsWith(WARC_SUFFIX) ||
								daFile.getName().endsWith(WARC_GZ_SUFFIX));
						}
					};
					
					File[] files = dir.listFiles(filter);
					if(files == null) {
						throw new IOException("Directory " + dir.getAbsolutePath() +
								" is not a directory or had an IO error");
					}
					for(int i = 0; i < files.length; i++) {
						File file = files[i];
						String fileName = file.getName();
						String fileUrl = dirUrl + fileName;
						LOGGER.info("Adding location " + fileUrl +
								" for file " + fileName);
						locationClient.addNameUrl(fileName,fileUrl);
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
				
			} else {
				USAGE(" unknown operation " + operation);
			}
		}
	}
}
