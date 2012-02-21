package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.archive.wayback.core.Resource;

public class RobotsTxtResource extends Resource {
	
	public RobotsTxtResource(String string)
	{
		this.setInputStream(new ByteArrayInputStream(string.getBytes()));
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getStatusCode() {
		// TODO Auto-generated method stub
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

}
