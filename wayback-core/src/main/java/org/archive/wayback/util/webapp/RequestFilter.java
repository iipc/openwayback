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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.util.MonitoredFileSet;

/**
 * Top-Level integration point between a series of RequestHandler mappings and a
 * generic ServletContext. This filter is assumed to be responsible for matching
 * ALL requests received by the webapp ("*") and uses a RequestMapper to
 * delegate incoming HttpServletRequests to the appropriate RequestHandler, via
 * the doFilter() method.
 * 
 * @author brad
 */
public class RequestFilter implements Filter {
	private static final Logger LOGGER = 
		Logger.getLogger(RequestFilter.class.getName());

	private final static String CONFIG_PATH = "config-path";
	private final static String LOGGING_CONFIG_PATH = "logging-config-path";
	private final static String MONITOR_MS_CONFIG = "monitor-ms";
	private final static String MONITOR_FILES_CONFIG = "monitor-files";

	private UpdateThread thread = null;
	private RequestMapper mapper = null;
	private ServletContext context;
	private String springConfigPath;

	public void init(FilterConfig config) throws ServletException {
		context = config.getServletContext();

		String logConfigPath = context.getInitParameter(LOGGING_CONFIG_PATH);
		if (logConfigPath != null) {
			String resolvedLogPath = context.getRealPath(logConfigPath);
			File logConfigFile = new File(resolvedLogPath);
			if (logConfigFile.exists()) {
				FileInputStream finp = null;
				try {
					finp = new FileInputStream(logConfigFile);
					LogManager.getLogManager().readConfiguration(finp);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (finp != null) {
							finp.close();
						}
					} catch (IOException e) {
						throw new ServletException(e);
					}
				}
			}
		}

		String configPath = context.getInitParameter(CONFIG_PATH);
		if (configPath == null) {
			throw new ServletException("Missing " + CONFIG_PATH + " parameter");
		}
		springConfigPath = context.getRealPath(configPath);

		String monitorFiles = context.getInitParameter(MONITOR_FILES_CONFIG);
		if(monitorFiles == null) {
			// just load once:
			mapper = loadRequestMapper();
		} else {

			// we're in fancy mode: start the background thread to watch
			// our Spring config - it will swap out our mapper when things 
			// change

			String monitorMSString = context.getInitParameter(MONITOR_MS_CONFIG);
			long monitorMS = 10000;
			if(monitorMSString != null) {
				try {
					monitorMS = Long.parseLong(monitorMSString);
				} catch(NumberFormatException e) {
					throw new ServletException("Non int for " + MONITOR_MS_CONFIG);
				}
			}
			String[] monitored = monitorFiles.split(",");
	
			ArrayList<String> monitoredL = new ArrayList<String>();
			for(String monitoredPath : monitored) {
				monitoredL.add(monitoredPath);
			}
			thread = new UpdateThread(this, monitorMS, monitoredL);

			// TODO: should we force initial load of a mapper?
			//       it means incoming requests will block until we're ready..
			//       if we don't the thread will immediately being loading
			//       the Spring config, and will swap it in when it's ready
			thread.reloadMapper();
			thread.start();
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		boolean handled = false;
		String origThreadName = Thread.currentThread().getName();
		try {
			if (request instanceof HttpServletRequest) {
				if (response instanceof HttpServletResponse) {
					if(mapper != null) {
						handled = mapper.handleRequest(
								(HttpServletRequest) request,
								(HttpServletResponse) response);
						
					}
				}
			}
		} finally {
			Thread.currentThread().setName(origThreadName);
		}
		if (!handled) {
			chain.doFilter(request, response);
		}
	}

	public void destroy() {
		LOGGER.info("Shutdown starting.");
		if(thread != null) {
			thread.interrupt();
		}
		if(mapper != null) {
			mapper.shutdown();
		}
		LOGGER.info("Shutdown complete.");
	}


	private RequestMapper loadRequestMapper() {
		LOGGER.info("Initializing Spring config at: " + springConfigPath);
		RequestMapper newMapper = SpringReader.readSpringConfig(springConfigPath, context);
		LOGGER.info("Initialized Spring config at: " + springConfigPath);
		return newMapper;
	}

	/**
	 * @return the mapper
	 */
	public RequestMapper getMapper() {
		return mapper;
	}

	/**
	 * @param mapper the mapper to set
	 */
	public void setMapper(RequestMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Thread that repeatedly checks a set of Spring config files. If any
	 * change, then a new RequestMapper is created from them, which is then
	 * swapped in on the containing RequestFilter. The old one if present is
	 * shut down.
	 *  
	 * @author Brad Tofel
	 */
	private class UpdateThread extends Thread {
		/**
		 * object which merges CDX files with the BDBResourceIndex
		 */
		private RequestFilter filter = null;
		private long runInterval;

		private MonitoredFileSet fileSet;
		private MonitoredFileSet.FileState activeState;

		/**
		 * @param filter the RequestFilter we will update
		 * @param runInterval number of MS bewtween checks
		 * @param monitored List of files to check Mod Time to trigger reload
		 */
		public UpdateThread(RequestFilter filter, 
				long runInterval, List<String> monitored) {
			
			super("RequestFilter.UpdateThread");
			super.setDaemon(true);
			this.filter = filter;
			this.runInterval = runInterval;

			fileSet = new MonitoredFileSet(monitored);
			activeState = null;
		}
		
		public void reloadMapper() {

			MonitoredFileSet.FileState startState = fileSet.getFileState();

			RequestMapper mapper = filter.loadRequestMapper();

			if(fileSet.isChanged(startState)) {
				// erk.. files changed during the operation.. update nothing..
				LOGGER.warning("Files changed during Spring reload... discarding..");
				mapper.shutdown();

			} else {
				LOGGER.warning("Loaded RequestMapper.");
				RequestMapper oldMapper = filter.getMapper();
				filter.setMapper(mapper);
				if(oldMapper != null) {
					// shut it down (cross fingers first)
					LOGGER.warning("Shutting Down old RequestMapper.");
					oldMapper.shutdown();
				}
				activeState = startState;
			}
		}

		public void run() {
			LOGGER.info("RequestFilter.UpdateThread is alive.");
			while (true) {
				try {
					if((activeState == null) || fileSet.isChanged(activeState)) {
						reloadMapper();
					}
					sleep(runInterval);
				} catch (InterruptedException e) {
					LOGGER.info("Shutting Down.");
					return;
				}
			}
		}
	}
}
