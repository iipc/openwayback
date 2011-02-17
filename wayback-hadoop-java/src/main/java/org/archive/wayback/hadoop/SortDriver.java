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
package org.archive.wayback.hadoop;

import org.apache.hadoop.util.ProgramDriver;

public class SortDriver {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    int exitCode = -1;
	    ProgramDriver pgd = new ProgramDriver();
	    try {
		      pgd.addClass("cdxsort", CDXSortDriver.class, 
              "A map/reduce program that canonicalizes and provides a total order sort into multiple CDX files");
		      pgd.addClass("http-import", HTTPImportJob.class, 
              "A map/reduce program that imports a bunch of URLs into an HDFS directory");
	      pgd.driver(args);
	      // Success
	      exitCode = 0;
	    }
	    catch(Throwable e){
	      e.printStackTrace();
	    }
	    
	    System.exit(exitCode);
	}
}
