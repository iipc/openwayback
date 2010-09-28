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
	 * @param filter ObjectFilter responsible for excluding content
	 */
	public ExternalExcluder(ObjectFilter<CaptureSearchResult> filter) {
		this.filter = filter;
	}
	/**
	 * @param urlString String URL that should be checked for blocking.
	 * @param timestamp String 14-digit timestamp to check for blocking.
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
	 * @param configPath String path to local Sprint XML configuration. This
	 *        Spring config file must include a bean with id "excluder-factory"
	 *        that implements 
	 *        org.archive.wayback.accesscontrol.ExclusionFilterFactory
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
