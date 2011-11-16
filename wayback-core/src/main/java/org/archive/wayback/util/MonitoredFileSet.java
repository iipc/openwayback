package org.archive.wayback.util;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MonitoredFileSet {
	List<String> files;
	
	public MonitoredFileSet(List<String> files) {
		this.files = files;
	}
	public boolean isChanged(FileState fileState) {
		FileState currentFileState = getFileState();
		return currentFileState.isChanged(fileState);
	}
	public FileState getFileState() {
		FileState fileState = new FileState();
		
		for(String path : files) {
			File file = new File(path);
			if(file.isFile()) {
				fileState.put(path, new Date(file.lastModified()));
			} else {
				fileState.put(path, null);
			}
		}
		return fileState;
	}
	
	public class FileState extends HashMap<String,Date> {
		public boolean isChanged(FileState other) {
			for(String path : keySet()) {
				if(other.containsKey(path)) {
					Date otherDate = other.get(path);
					Date thisDate = get(path);
					if((otherDate == null) && (thisDate == null)) {
						// treat both missing as the same..
						continue;
					}
					if(!otherDate.equals(thisDate)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
