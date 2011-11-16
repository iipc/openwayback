package org.archive.wayback.accesscontrol.robotstxt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which acts as an aggregation of RobotsDirectives.
 * 
 * If given a host String, will return a list of additional robot URLs that
 * need to be added to the current aggregation.
 * 
 * Allows a user to then add new RobotsDirectives for one or more robot URLs.
 * 
 * Finally, allows the aggregation to be queried to see if any of the
 * directives block a particular path.
 * 
 * 
 * @author brad
 *
 */
public class RobotsDirectiveAggregation {
	private final static Logger LOGGER = 
		Logger.getLogger(RobotsDirectiveAggregation.class.getName());

	private final static String HTTP_PREFIX = "http://";
	private final static String ROBOT_SUFFIX = "/robots.txt";

	private static String WWWN_REGEX = "^www[0-9]+\\.";
	private final static Pattern WWWN_PATTERN = Pattern.compile(WWWN_REGEX);

	private HashMap<String,RobotsDirectives> cache = 
		new HashMap<String, RobotsDirectives>();

	private StringBuilder sb = new StringBuilder();

	private String hostToRobotUrlString(final String host) {
		sb.setLength(0);
		sb.append(HTTP_PREFIX).append(host).append(ROBOT_SUFFIX);
		String robotUrl = sb.toString();
		LOGGER.fine("Adding robot URL:" + robotUrl);
		return robotUrl;
	}
	/*
	 */
	/**
	 * @param resultHost
	 * @return a List of all robots.txt urls to attempt for this HOST:
	 * If HOST starts with "www.DOMAIN":
	 * 	   [
	 *        http://HOST/robots.txt,
	 *        http://DOMAIN/robots.txt
	 *     ]
	 * If HOST starts with "www[0-9]+.DOMAIN":
	 *     [
	 *        http://HOST/robots.txt,
	 *        http://www.DOMAIN/robots.txt,
	 *        http://DOMAIN/robots.txt
	 *     ]
	 * Otherwise:
	 *     [
	 *        http://HOST/robots.txt,
	 *        http://www.HOST/robots.txt
	 *     ]
	 */
	List<String> hostToRobotUrlStrings(final String resultHost) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(hostToRobotUrlString(resultHost));
		
		if(resultHost.startsWith("www")) {
			if(resultHost.startsWith("www.")) {
				list.add(hostToRobotUrlString(resultHost.substring(4)));
			} else {
				Matcher m = WWWN_PATTERN.matcher(resultHost);
				if(m.find()) {
					String massagedHost = resultHost.substring(m.end());
					list.add(hostToRobotUrlString("www." + massagedHost));
					list.add(hostToRobotUrlString(massagedHost));
				}
			}
		} else {
			list.add(hostToRobotUrlString("www." + resultHost));			
		}
		return list;
	}

	public List<String> getMissingRobotUrls(String host) {
		ArrayList<String> missing = new ArrayList<String>();
		List<String> needed = hostToRobotUrlStrings(host);
		for(String need : needed) {
			if(!cache.containsKey(need)) {
				missing.add(need);
			}
		}
		return missing;
	}
	public void addDirectives(String url, RobotsDirectives directives) {
		cache.put(url, directives);
	}
	public boolean isBlocked(String path) {
		for(RobotsDirectives directives : cache.values()) {
			if(!directives.allows(path)) {
				return true;
			}
		}
		return false;
	}
}
