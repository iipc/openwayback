/* RobotRules
 *
 * $Id$
 *
 * Created on 2:51:20 PM Mar 12, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.accesscontrol.robotstxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class which parses a robots.txt file, storing the rules contained therein,
 * and then allows for testing if path/userAgent tuples are blocked by those
 * rules.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RobotRules {
	
	private static final long serialVersionUID = 2917420727021840982L;
	private static final Logger LOGGER = Logger.getLogger(RobotRules.class
			.getName());
	/**
	 * Special name for User-agent which matches all values
	 */
	public static final String GLOBAL_USER_AGENT = "*";

	private boolean bSyntaxErrors = false;
	private HashMap<String, ArrayList<String>> rules = 
		new HashMap<String, ArrayList<String>>();

	private LinkedList<String> userAgents = new LinkedList<String>();

	/**
	 * @return true if the robots.txt file looked suspicious, currently meaning
	 * we found a Disallow rule that was not preceded by a "User-agent:" line
	 */
	public boolean hasSyntaxErrors() {
		return bSyntaxErrors;
	}
	
	/**
	 * @return a List of all UserAgents Found in the Robots.txt document
	 */
	public List<String> getUserAgentsFound() {
		return userAgents;
	}
	
	/**
	 * Read rules from InputStream argument into this RobotRules, as a 
	 * side-effect, sets the bSyntaxErrors property.
	 * 
	 * @param is
	 * @throws IOException 
	 */
	public void parse(InputStream is) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(InputStream) is));
        String read;
        ArrayList<String> current = null;
        while (br != null) {
            do {
                read = br.readLine();
                // Skip comments & blanks
            } while ((read != null) && ((read = read.trim()).startsWith("#") ||
                read.length() == 0));
            if (read == null) {
            	br.close();
            	br = null;
            } else {
                int commentIndex = read.indexOf("#");
                if (commentIndex > -1) {
                    // Strip trailing comment
                    read = read.substring(0, commentIndex);
                }
                read = read.trim();
                if (read.matches("(?i)^User-agent:.*")) {
                    String ua = read.substring(11).trim().toLowerCase();
                    if (current == null || current.size() != 0) {
                        // only create new rules-list if necessary
                        // otherwise share with previous user-agent
                        current = new ArrayList<String>();
                    }
                    rules.put(ua, current);
                    LOGGER.fine("Found User-agent(" + ua + ") rules...");
                    continue;
                }
                if (read.matches("(?i)Disallow:.*")) {
                    if (current == null) {
                        // buggy robots.txt
                    	bSyntaxErrors = true;
                        continue;
                    }
                    String path = read.substring(9).trim();
                    current.add(path);
                    continue;
                }
                // unknown line; do nothing for now
                
                // TODO: check for "Allow" lines, and flag a syntax error if 
                //       we encounter any unknown lines?
            }
        }
    }
	
	private boolean blocksPath(String path, String curUA, List<String> uaRules) {
		
		Iterator<String> disItr = uaRules.iterator();
		while (disItr.hasNext()) {
			String disallowedPath = disItr.next();
			if (disallowedPath.length() == 0) {

				LOGGER.fine("UA(" + curUA
						+ ") has empty disallow: Go for it!");
				return false;

			} else {
				LOGGER.fine("UA(" + curUA + ") has ("
						+ disallowedPath + ") blocked...("
						+ disallowedPath.length() + ")");
				if (disallowedPath.equals("/") || path.startsWith(disallowedPath)) {
					LOGGER.fine("THIS APPLIES!!!");
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks first the specified ua UserAgent, if rules are present for it,
	 * and then falls back to using rules for the '*' UserAgent.
	 * 
	 * @param path
	 * @param ua
	 * @return boolean value where true indicates the path is blocked for ua
	 */
	public boolean blocksPathForUA(String path, String ua) {

		if(rules.containsKey(ua.toLowerCase())) {

			return blocksPath(path,ua,rules.get(ua.toLowerCase()));

		} else if(rules.containsKey(GLOBAL_USER_AGENT)) {

			return blocksPath(path,GLOBAL_USER_AGENT,
					rules.get(GLOBAL_USER_AGENT));			
		}
		return false;
	}
}
