package org.archive.wayback.accesscontrol.robotstxt.redis;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.util.webapp.AbstractRequestHandler;

//TODO: Add a proper jsp/view for this
//This is a simple prototype of the update-robots mechanism

public class UpdateRobotsRequestHandler extends AbstractRequestHandler {
	
	protected final static String HTTP_PREFIX = "http://";
	protected final static String WWW_PREFIX = "www.";
	protected final static String HTTP_WWW_PREFIX = HTTP_PREFIX + WWW_PREFIX;
	protected final static String ROBOT_SUFFIX = "/robots.txt";
	
	private RedisRobotsCache robotsCache;
	
	// Minimum time (secs) between subsequent forced updates
	// Default: off for now
	private int minUpdateTime = 0;

	public int getMinUpdateTime() {
		return minUpdateTime;
	}

	public void setMinUpdateTime(int minUpdateTime) {
		this.minUpdateTime = minUpdateTime;
	}

	public RedisRobotsCache getRobotsCache() {
		return robotsCache;
	}

	public void setRobotsCache(RedisRobotsCache robotsCache) {
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
			
			RobotsContext context = robotsCache.forceUpdate(url, minUpdateTime);
			
			if (context == null) {
				writer.println("<p>Error Updating Robots (see logs)</p>");
				return true;
			}
			
			boolean sameRobots = (context.current != null) && (context.getNewRobots() != null) && (context.current.equals(context.getNewRobots()));
						
			if (!sameRobots) {
				writer.println("<b>UPDATED Robots</b>");
				writer.println("<p><i>Old Robots:</i></p>");
				writer.println("<pre>" + context.current + "</pre>");
				writer.println("<p><i>NEW Updated Robots:</i></p>");
			} else {
				writer.println("<b>Robots Unchanged</b>");
				writer.println("<p><i>Current Robots:</i></p>");
			}
			
			writer.print("<pre>");
			
			if (context.getNewRobots() == null) {
				switch (context.getStatus()) {
				case RobotsContext.LIVE_HOST_ERROR:
					writer.print("Unknown Host Error");
					break;
					
				case RobotsContext.LIVE_TIMEOUT_ERROR:
					writer.print("Connection Timed Out Error");
					break;					
					
				default:
					writer.print("Error: " + context.getStatus());
				}

				writer.print(" (" + RedisRobotsCache.ROBOTS_TOKEN_ERROR + context.getStatus() + ")");
			} else {
				writer.print(context.getNewRobots());
			}
			writer.println("</pre>");
		}
		
		writer.println("<p><i>Current Time: " + new Date().toString() + "</p></body></html>");
		return true;
	}
	
}
