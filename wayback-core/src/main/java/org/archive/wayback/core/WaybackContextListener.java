/* WaybackContextListener
 *
 * $Id$
 *
 * Created on 5:17:59 PM Oct 16, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.core;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.archive.wayback.exception.ConfigurationException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WaybackContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		// attempt to initialize a ResourceStore and a ResourceIndex,
		// so their worker threads can start up...
		
		
		// throwaway WaybackLogic..
		WaybackLogic wayback = new WaybackLogic();
		
		Properties p = new Properties();
		ServletContext sc = sce.getServletContext();
		for (Enumeration e = sc.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, sc.getInitParameter(key));
		}
		wayback.init(p);
		try {
			wayback.getResourceStore();
		} catch (ConfigurationException e) {
			// Just dump the error and try to barrel on...
			e.printStackTrace();
		}
		try {
			wayback.getResourceIndex();
		} catch (ConfigurationException e) {
			// Just dump the error and try to barrel on...
			e.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// TODO: kill threads somehow?
		
		// I think this implies some interface for interogating implementations
		// for threads, which seems like more than we need, if the daemon 
		// threads are just gonna get killed anyways..
	}
}
