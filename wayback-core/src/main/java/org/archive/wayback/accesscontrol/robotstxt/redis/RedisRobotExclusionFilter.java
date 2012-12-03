/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.archive.wayback.accesscontrol.robotstxt.RobotExclusionFilter;
import org.archive.wayback.liveweb.LiveWebCache;

/**
 *
 * @author ilya
 * @version $Date: 2011-07-07 21:47:51 -0700 (Thu, 07 Jul 2011) $, $Revision: 3485 $
 */
public class RedisRobotExclusionFilter extends RobotExclusionFilter {

	public RedisRobotExclusionFilter(LiveWebCache redisCache, String userAgent, boolean cacheFails) {
		super(redisCache, userAgent, cacheFails ? 1 : 0);
	}
	
	protected String hostToRobotUrlString(String host) {
		sb.setLength(0);
		sb.append(HTTP_PREFIX);
		sb.append(host);
		if (host.endsWith(".")) {
			sb.deleteCharAt(HTTP_PREFIX.length() + host.length() - 1);
		}
		sb.append(ROBOT_SUFFIX);
		String robotUrl = sb.toString();
		return robotUrl;
	}
	
	protected List<String> searchResultToRobotUrlStrings(String resultHost) {
		ArrayList<String> list = new ArrayList<String>();
		
		if(resultHost.startsWith("www")) {
			if (resultHost.startsWith("www.")) {
				list.add(hostToRobotUrlString(resultHost));
				list.add(hostToRobotUrlString(resultHost.substring(4)));
			} else {
				Matcher m = WWWN_PATTERN.matcher(resultHost);
				if(m.find()) {
					String massagedHost = resultHost.substring(m.end());
					list.add(hostToRobotUrlString("www." + massagedHost));
					list.add(hostToRobotUrlString(massagedHost));
				}
				list.add(hostToRobotUrlString(resultHost));
			}
		} else {	
			list.add(hostToRobotUrlString("www." + resultHost));
			list.add(hostToRobotUrlString(resultHost));
		}
		return list;
	}
}