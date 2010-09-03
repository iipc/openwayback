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
