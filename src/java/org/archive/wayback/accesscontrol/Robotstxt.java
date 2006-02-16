/* Robotstxt
 *
 * $Id$
 *
 * Created on 4:31:36 PM Feb 13, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

/**
 * Copied/Stolen from Heritrix code tree -- not easily exported and only one
 * simple function, but it is less than ideal.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class Robotstxt {
    /**
     * Populate the disallows Map based on robots.txt content in BufferReader.
     * Also, seems to populate the userAgents LinkedList with all found
     * user agents.
     * 
     * @param reader
     * @param userAgents
     * @param disallows
     * @return boolean, true indicates a valid robots.txt document
     * @throws IOException
     */
    public static boolean parse(BufferedReader reader,
            final LinkedList userAgents, final Map disallows)
    throws IOException {
        boolean hasErrors = false;
        String read;
        ArrayList current = null;
        String catchall = null;
        while (reader != null) {
            do {
                read = reader.readLine();
                // Skip comments & blanks
            } while ((read != null) && ((read = read.trim()).startsWith("#") ||
                read.length() == 0));
            if (read == null) {
                reader.close();
                reader = null;
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
                        current = new ArrayList();
                    }
                    if (ua.equals("*")) {
                        ua = "";
                        catchall = ua;
                    } else {
                        userAgents.addLast(ua);
                    }
                    disallows.put(ua, current);
                    continue;
                }
                if (read.matches("(?i)Disallow:.*")) {
                    if (current == null) {
                        // buggy robots.txt
                        hasErrors = true;
                        continue;
                    }
                    String path = read.substring(9).trim();
                    current.add(path);
                    continue;
                }
                // unknown line; do nothing for now
            }
        }

        if (catchall != null) {
            userAgents.addLast(catchall);
        }
        return hasErrors;
    }

}
