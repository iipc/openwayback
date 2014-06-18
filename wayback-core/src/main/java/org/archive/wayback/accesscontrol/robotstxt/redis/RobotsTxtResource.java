package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.archive.wayback.core.Resource;

public class RobotsTxtResource extends Resource {
	
	protected String contents;
	
	public RobotsTxtResource(String contents) {
		this.contents = contents;
		
		// Skip BOM
		if (!contents.isEmpty() && (contents.charAt(0) == ('\uFEFF'))) {
			contents = contents.substring(1);
		}
		
		this.setInputStream(new ByteArrayInputStream(contents.getBytes()));
	}
	
	@Override
	public void close() throws IOException {
	}

	@Override
	public int getStatusCode() {
		return 200;
	}

	@Override
	public long getRecordLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, String> getHttpHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getContents() {
		return contents;
	}
}
