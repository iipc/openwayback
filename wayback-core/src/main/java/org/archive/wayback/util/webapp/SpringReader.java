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
import org.springframework.context.ApplicationContext;

/**
 * Single static method to read a Spring XML configuration, extract 
 * RequestHandlers, and return a RequestMapper which delegates requests to
 * those RequestHandlers.
 * 
 * @deprecated functionality has been moved to RequestFilter#loadRequestMapper().
 * This class is retained only for backward compatibility with existing code accessing
 * {@link #getCurrentContext()}.
 * 
 * @author brad
 *
 */
public class SpringReader {
	
	protected static ApplicationContext currentContext = null;
	
	public static ApplicationContext getCurrentContext() {
		return currentContext;
	}
}
