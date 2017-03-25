package org.archive.wayback.instantliveweb;

import org.archive.wayback.webapp.ServerRelativeArchivalRedirect;

public class InstaRelativeRedirect extends ServerRelativeArchivalRedirect {
	
	protected String record;
	protected String recordEmbed;

	@Override
    protected String modifyCollection(String collection) {
		if (collection.equals(record)) {
			return recordEmbed;
		}
		
	    return super.modifyCollection(collection);
    }

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = trimSlash(record);
	}

	public String getRecordEmbed() {
		return recordEmbed;
	}

	public void setRecordEmbed(String recordEmbed) {
		this.recordEmbed = trimSlash(recordEmbed);
	}
	
	protected String trimSlash(String str)
	{
		int start = 0, end = str.length();
//		if (str.charAt(0) == '/') {
//			start++;
//		}
		if ((str.length() > 1) && str.charAt(str.length() - 1) == '/') {
			end--;
		}
		return str.substring(start, end);
	}
}
