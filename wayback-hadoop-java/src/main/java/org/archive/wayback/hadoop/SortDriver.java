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
	      pgd.addClass("cdxsort", CDXSort.class, 
	                   "A map/reduce program that counts the words in the input files.");
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
