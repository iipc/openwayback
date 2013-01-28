package org.archive.wayback.accesscontrol.robotstxt.redis;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.accesscontrol.robotstxt.redis.SimpleRedisRobotsCache.RobotsResult;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

//TODO: Add a proper jsp/view for this
//This is a simple prototype of the update-robots mechanism

public class UpdateRobotsRequestHandler extends AbstractRequestHandler {
	
	protected final static String HTTP_PREFIX = "http://";
	protected final static String WWW_PREFIX = "www.";
	protected final static String HTTP_WWW_PREFIX = HTTP_PREFIX + WWW_PREFIX;
	protected final static String ROBOT_SUFFIX = "/robots.txt";
	
	private SimpleRedisRobotsCache robotsCache;
	
	// Minimum time (secs) between subsequent forced updates
	// Default: off for now
	private int minUpdateTime = 0;

	public int getMinUpdateTime() {
		return minUpdateTime;
	}

	public void setMinUpdateTime(int minUpdateTime) {
		this.minUpdateTime = minUpdateTime;
	}

	public SimpleRedisRobotsCache getRobotsCache() {
		return robotsCache;
	}

	public void setRobotsCache(SimpleRedisRobotsCache robotsCache) {
		this.robotsCache = robotsCache;
	}

	@Override
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {

		String url = this.translateRequestPath(httpRequest);
		PrintWriter writer = httpResponse.getWriter();
		
		httpResponse.setContentType("text/html");
		
		writer.println("<html><body><h2>Wayback Robots Updater</h2>");
		
		if (!url.endsWith(ROBOT_SUFFIX)) {
			writer.println("<p>URL to update (<code>" + url + "</code>) must end in /robots.txt</p>");
		} else if (robotsCache == null) {
			writer.println("No Robots Cache Set");
		} else {
			if (url.startsWith(HTTP_WWW_PREFIX)) {
				//
			} else if (url.startsWith(WWW_PREFIX)) {
				url = HTTP_PREFIX + url;
			} else if (url.startsWith(HTTP_PREFIX)) {
				url = HTTP_WWW_PREFIX + url.substring(7);
			} else {
				url = HTTP_WWW_PREFIX + url;
			}
			
			//RobotsContext context = robotsCache.forceUpdate(url, minUpdateTime);
			RobotsResult result = robotsCache.forceUpdate(url, minUpdateTime, false);
			
			if (result == null) {
				writer.println("<p>Error Updating Robots (see logs)</p>");
				return true;
			}
						
			if (!result.isSameRobots()) {
				writer.println("<b>UPDATED Robots</b>");
				writer.println("<p><i>Old Robots:</i></p>");
				writer.println("<pre>" + result.oldRobots + "</pre>");
				writer.println("<p><i>NEW Updated Robots:</i></p>");
			} else {
				writer.println("<b>Robots Unchanged</b>");
				writer.println("<p><i>Current Robots:</i></p>");
			}
			
			writer.print("<pre>");
			
			if (result.robots != null && result.status == 200) {
				writer.print(result.robots);
			} else {
				writer.print("No Valid Robots Found: Status " + result.status);
			}
			
			writer.println("</pre>");
		}
		
		writer.println("<p><i>Current Time: " + new Date().toString() + "</p></body></html>");
		return true;
	}
	
}
