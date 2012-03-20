package org.archive.wayback.accesscontrol.robotstxt;

import java.util.concurrent.CountDownLatch;

class RobotsContext
{
	final CountDownLatch latch;
	final String url;
	final String current;
	
	private int status;
	private String newRobots;
	private long created;
	
	RobotsContext(String url, String current)
	{
		this.latch = new CountDownLatch(1);
		this.url = url;
		this.current = current;
		this.created = System.currentTimeMillis();
	}
	
	boolean isValid()
	{
		return (newRobots != null) && (status == RedisRobotsCache.LIVE_OK);
	}
	
	String getNewRobots()
	{
		return newRobots;
	}
	
	void setNewRobots(String newRobots)
	{
		this.newRobots = newRobots;
	}
	
	int getStatus()
	{
		return status;
	}
	
	void setStatus(int status)
	{
		this.status = status;
	}
	
	long getCreated()
	{
		return created;
	}
}