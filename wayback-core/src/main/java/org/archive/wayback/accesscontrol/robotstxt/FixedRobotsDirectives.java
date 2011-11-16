package org.archive.wayback.accesscontrol.robotstxt;

public class FixedRobotsDirectives extends RobotsDirectives {
	private boolean result;
	public FixedRobotsDirectives(boolean result) {
		this.result = result;
	}
	public boolean allows(String path) {
		return result;
	}
}
