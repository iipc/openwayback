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
package org.archive.wayback.util.webapp;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Single static method to read a Spring XML configuration, extract 
 * RequestHandlers, and return a RequestMapper which delegates requests to
 * those RequestHandlers.
 * 
 * @author brad
 *
 */
public class SpringReader {
	private static final Logger LOGGER = Logger.getLogger(
			SpringReader.class.getName());

	/**
	 * Read the single Spring XML configuration file located at the specified
	 * path, performing PropertyPlaceHolder interpolation, extracting all beans
	 * which implement the RequestHandler interface, and construct a 
	 * RequestMapper for those RequestHandlers, on the specified ServletContext. 
	 * @param configPath the path to the Spring XML file containing the 
	 * configuration.
	 * @param servletContext the ServletContext where the RequestHandlers should
	 * be mapped
	 * @return a new ReqeustMapper which delegates requests for the 
	 * ServletContext
	 */
	@SuppressWarnings("unchecked")
	public static RequestMapper readSpringConfig(String configPath,
			ServletContext servletContext) {
		LOGGER.info("Loading from config file " + configPath);
		
		Resource resource = new FileSystemResource(configPath);
		XmlBeanFactory factory = new XmlBeanFactory(resource);
		Map map = factory.getBeansOfType(PropertyPlaceholderConfigurer.class);
		if(map != null) {
			Collection<PropertyPlaceholderConfigurer> macros = map.values();
			for(PropertyPlaceholderConfigurer macro : macros) {
				macro.postProcessBeanFactory(factory);
			}
		}
		LOGGER.info("Pre-instanting Singletons starting");
		factory.preInstantiateSingletons();
		LOGGER.info("Pre-instanting Singletons complete");
		Map<String,RequestHandler> beans = 
			factory.getBeansOfType(RequestHandler.class,false,false);
		return new RequestMapper(beans.values(), servletContext);
	}
}
