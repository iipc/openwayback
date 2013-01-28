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

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper static methods to implement registration of a RequestHandler with a
 * RequestMapper, based on the beanName() method.
 * 
 * @author brad
 *
 */
public class BeanNameRegistrar {

	private static final Logger LOGGER = Logger.getLogger(
			BeanNameRegistrar.class.getName());

	private static final String PORT_PATTERN_STRING = 
		"([0-9]+):?";
	private static final String PORT_PATH_PATTERN_STRING = 
		"([0-9]+):([0-9a-zA-Z_.-]+)";
	private static final String HOST_PORT_PATTERN_STRING = 
		"([0-9a-z_.-]+):([0-9]+):?";
	private static final String HOST_PORT_PATH_PATTERN_STRING = 
		"([0-9a-z_.-]+):([0-9]+):([0-9a-zA-Z_.-]+)";
	
	private static final String URI_PATTERN_STRING = 
		"(https?://([0-9a-z_.-]+))?(:[0-9]+)?/([0-9a-zA-Z_.-]+)(/.*)";
	
	
	private static final Pattern PORT_PATTERN = 
		Pattern.compile(PORT_PATTERN_STRING);
	private static final Pattern PORT_PATH_PATTERN = 
		Pattern.compile(PORT_PATH_PATTERN_STRING);
	private static final Pattern HOST_PORT_PATTERN = 
		Pattern.compile(HOST_PORT_PATTERN_STRING);
	private static final Pattern HOST_PORT_PATH_PATTERN = 
		Pattern.compile(HOST_PORT_PATH_PATTERN_STRING);
	private static final Pattern URI_PATTERN =
		Pattern.compile(URI_PATTERN_STRING);
	
	/*
	 * matches:
	 *   8080
	 *   8080:
	 */
	private static boolean registerPort(String name, RequestHandler handler, 
			RequestMapper mapper) {
		Matcher m = null;
		m = PORT_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(1)); 
			mapper.addRequestHandler(port, null, null, handler);
			return true;
		}
		return false;
	}
	/*
	 * matches:
	 *   8080:blue
	 *   8080:fish
	 */
	private static boolean registerPortPath(String name, RequestHandler handler, 
			RequestMapper mapper) {
		Matcher m = null;
		m = PORT_PATH_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(1)); 
			mapper.addRequestHandler(port, null, m.group(2), handler);
			return true;
		}
		return false;
	}
	/*
	 * matches:
	 *   localhost.archive.org:8080
	 *   static.localhost.archive.org:8080
	 */
	private static boolean registerHostPort(String name, RequestHandler handler, 
			RequestMapper mapper) {
		Matcher m = null;
		m = HOST_PORT_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(2));
			mapper.addRequestHandler(port, m.group(1), null, handler);
			return true;
		}
		return false;
	}
	/*
	 * matches:
	 *   localhost.archive.org:8080:two
	 *   static.localhost.archive.org:8080:fish
	 */
	private static boolean registerHostPortPath(String name, 
			RequestHandler handler,	RequestMapper mapper) {
		Matcher m = null;
		m = HOST_PORT_PATH_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(2)); 
			mapper.addRequestHandler(port, m.group(1), m.group(3), handler);
			return true;
		}
		return false;
	}
	
	/*
	 * matches:
	 *   http://localhost.archive.org:8080/two
	 *   the port is optional, and need not be part of the URI
	 *   if not included, using the internalPort setting
	 */
	private static boolean registerURIPatternPath(String name, int port,
			RequestHandler handler,	RequestMapper mapper) {
		Matcher m = null;
		m = URI_PATTERN.matcher(name);
		
		if (m.matches()) {
			String host = m.group(2);
			String portString = m.group(3);
			
			if ((portString != null) && portString.startsWith(":")) {
				port = Integer.parseInt(portString.substring(1));
			}
			
			String path = m.group(4);
			
			mapper.addRequestHandler(port, null, path, handler);
			return true;
		}
		return false;
	}
	
	/**
	 * Extract the RequestHandler objects beanName, parse it, and register the
	 * RequestHandler with the RequestMapper according to the beanNames 
	 * semantics.
	 * @param handler The RequestHandler to register
	 * @param mapper the RequestMapper where the RequestHandler should be 
	 * registered.
	 */
	public static void registerHandler(RequestHandler handler, 
			RequestMapper mapper) {
		
		String name = null;
		int internalPort = 8080;
		
		if (handler instanceof AbstractRequestHandler) {
			name = ((AbstractRequestHandler)handler).getAccessPointPath();
			internalPort = ((AbstractRequestHandler)handler).getInternalPort();
		}
		
		if (name == null) {
			name = handler.getBeanName();
		}
		
		if(name != null) {
			if(name.equals(RequestMapper.GLOBAL_PRE_REQUEST_HANDLER)) {
				LOGGER.info("Registering Global-pre request handler:" +
						handler);
				mapper.addGlobalPreRequestHandler(handler);
				
			} else if(name.equals(RequestMapper.GLOBAL_POST_REQUEST_HANDLER)) {
	
				LOGGER.info("Registering Global-post request handler:" + 
						handler);
				mapper.addGlobalPostRequestHandler(handler);
				
			} else {
				try {
	
					boolean registered = 
						registerPort(name, handler, mapper) ||
						registerPortPath(name, handler, mapper) ||
						registerHostPort(name, handler, mapper) ||
						registerHostPortPath(name, handler, mapper) || 
						registerURIPatternPath(name, internalPort, handler, mapper);
	
					if(!registered) {
						LOGGER.severe("Unable to register (" + name + ")");
					}
				} catch(NumberFormatException e) {
					LOGGER.severe("FAILED parseInt(" + name + ")");
				}
			}
		} else {
			LOGGER.info("Unable to register RequestHandler - null beanName");
		}
		if(handler instanceof ShutdownListener) {
			ShutdownListener s = (ShutdownListener) handler;
			mapper.addShutdownListener(s);
		}
	}
}
