/* ExclusionFilterFactoryFactory
 *
 * $Id$
 *
 * Created on 8:17:48 PM Mar 5, 2007.
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
package org.archive.wayback.resourceindex;

import java.util.Properties;

import org.archive.wayback.accesscontrol.robotstxt.RobotExclusionFilterFactory;
import org.archive.wayback.accesscontrol.staticmap.StaticMapExclusionFilterFactory;
import org.archive.wayback.exception.ConfigurationException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ExclusionFilterFactoryFactory {
	private static final String FACTORY_TYPE = "exclusion.factorytype";
	private static final String FACTORY_TYPE_STATIC_MAP = "static-map";
	private static final String FACTORY_TYPE_ROBOT_EXCLUSION = "robot-cache";
	private static final String FACTORY_TYPE_ROBOT_PLUS_MAP = "robot-plus-map";
	
	/**
	 * @param p
	 * @return the ExclusionFilterFactory, or null if none is configured.
	 * @throws ConfigurationException
	 */
	public static ExclusionFilterFactory get(Properties p) 
		throws ConfigurationException {
		
		ExclusionFilterFactory factory = null;
		String type = (String) p.getProperty(FACTORY_TYPE);
		if(type == null) {
			return factory;
		}
		if(type.equals(FACTORY_TYPE_STATIC_MAP)) {
			factory = new StaticMapExclusionFilterFactory();
//			factory.init(p);
		} else if(type.equals(FACTORY_TYPE_ROBOT_EXCLUSION)){
			factory = new RobotExclusionFilterFactory();
//			factory.init(p);
		} else if(type.equals(FACTORY_TYPE_ROBOT_PLUS_MAP)) {
			CompositeExclusionFilterFactory composite = null;
			composite = new CompositeExclusionFilterFactory();
			ExclusionFilterFactory robot = new RobotExclusionFilterFactory();
			ExclusionFilterFactory staticMap = new StaticMapExclusionFilterFactory();
//			robot.init(p);
//			staticMap.init(p);
			composite.addFactory(staticMap);
			composite.addFactory(robot);
			factory = composite;
		}
		return factory;
	}
}
