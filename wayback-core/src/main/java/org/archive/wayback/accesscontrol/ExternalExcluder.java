/* ExternalExcluder
 *
 * $Id$
 *
 * Created on 2:33:37 PM Aug 21, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.Timestamp;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Class which simplifies usage of wayback robots and static map exclusion
 * policies and software in external applications.
 * 
 * Uses Spring to construct an ExclusionFilterFactory which handles requests.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ExternalExcluder {
	private static ExclusionFilterFactory factory = null;
	private ObjectFilter<CaptureSearchResult> filter = null;
	private final static String CONFIG_ID = "excluder-factory";
	/**
	 * @param filter
	 */
	public ExternalExcluder(ObjectFilter<CaptureSearchResult> filter) {
		this.filter = filter;
	}
	/**
	 * @param urlString
	 * @param timestamp
	 * @return true if the url-timestamp should not be shown to end users
	 */
	public boolean isExcluded(String urlString, String timestamp) {
		CaptureSearchResult sr = new CaptureSearchResult();

		sr.setOriginalUrl(urlString);
		sr.setCaptureTimestamp(Timestamp.parseBefore(timestamp).getDateStr());
		
		int ruling = filter.filterObject(sr);
		return (ruling != ObjectFilter.FILTER_INCLUDE);
	}
	private static synchronized ExclusionFilterFactory getFactory(String 
			configPath) {
		if(factory != null) {
			return factory;
		}
		Resource resource = new FileSystemResource(configPath);
		XmlBeanFactory xmlFactory = new XmlBeanFactory(resource);
		factory = (ExclusionFilterFactory) xmlFactory.getBean(CONFIG_ID);
		return factory;
	}
	
	/**
	 * @param configPath
	 * @return an excluder fully configured via the XML Spring configuration 
	 * at configPath 
	 */
	public static ExternalExcluder getExcluder(String configPath) {
		return new ExternalExcluder(getFactory(configPath).get());
	}
	/**
	 * shutdown underlying resources.
	 */
	public static synchronized void shutdown() {
		if(factory != null) {
			factory.shutdown();
		}
	}
}
