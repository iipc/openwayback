package org.archive.cdxserver.writer;

import org.archive.cdxserver.format.CDXFormat;
import org.archive.cdxserver.processor.BaseProcessor;
import org.archive.format.cdx.CDXLine;

public abstract class CDXWriter implements BaseProcessor {

	public void setContentType(String contentType) {
		// Do nothing by default
	}

	public boolean isAborted() {
		return false;
	}

	public void printError(String msg) {
		// Do nothing by default
	}

	public void writeMiscLine(String text) {
		// Do nothing by default
	}

	public void setMaxLines(int numLines, String remoteClusterUri) {
		// Do nothing by default
	}

	public void printNumPages(int numPages, boolean printInBody) {
		// Do nothing by default
	}

	public void close() {
		// Do nothing by default
	}

	@Override
	public CDXFormat modifyOutputFormat(CDXFormat format) {
		return format;
	}

	@Override
	public void trackLine(CDXLine line) {
		// TODO Auto-generated method stub
	}

	public void serverError(Exception io) {
		// TODO Auto-generated method stub
	}
}
