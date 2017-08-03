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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

/**
 * Top-Level integration point between a series of RequestHandler mappings and a
 * generic ServletContext. This filter is assumed to be responsible for matching
 * ALL requests received by the webapp ("*") and uses a RequestMapper to
 * delegate incoming HttpServletRequests to the appropriate RequestHandler, via
 * the doFilter() method.
 *
 * There are a few parameters that can be configured through {@code init-param}.
 * context parameters specified with {@code context-param} are also consulted
 * for backward compatibility. If {@init-param} and {@code context-param} name
 * the same parameter, {@code init-param} prevails.
 *
 * Parameters:
 * <dl>
 * <dt>{@code configPath}</dt>
 * <dd>context-relative path of primary Spring configuration file (required)</dd>
 * <dt>{@code loggingConfigPath}</dt>
 * <dd>context-relative path of configuration file for java logging</dd>
 * <dt>{@code monitorFiles</dt>
 * <dd>files to be monitored for triggering application context reload</dt>
 * <dt>{@code monitorMs}</dt>
 * <dd>interval for checking files listed in {@code monitorFiles} (milliseconds)</dd>
 * </dl>
 * For backward compatibility, parameter names with hyphen as word separator
 * (ex. {@code config-path} for {@code configPath}) are also supported.
 *
 * Parameters are defined as bean property, and configured dynamically through
 * FilterConfigPropertyValues. If you want to add more parameters, simply add bean
 * property. Parameter values will be automatically converted to property's type.
 *
 * @author brad
 */
public class RequestFilter implements Filter {
	private static final Logger LOGGER =
		Logger.getLogger(RequestFilter.class.getName());

	/**
	 * PropertyValues filled from FilterConfig init parameters.
	 * parameter names are converted to camel-case (ex. "config-path" becomes "configPath")
	 * (inspired by org.springframework.web.filter.GenericFilterBean; we cannot simply
	 * reuse GenericFilterBean because of parameter names)
	 */
	private static class FilterConfigPropertyValues extends MutablePropertyValues {
		private static String camelCase(String s) {
			if (s.indexOf('-') < 0)
				return s;
			StringBuilder sb = new StringBuilder();
			char[] chars = s.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == '-') {
					if (++i < chars.length) {
						sb.append(Character.toUpperCase(chars[i]));
					}
				} else {
					sb.append(chars[i]);
				}
			}
			return sb.toString();
		}

		public FilterConfigPropertyValues(FilterConfig config) {
			// populate from context parameters for backward compatibility
			addContextParams(config.getServletContext());

			@SuppressWarnings("unchecked")
			Enumeration<String> en = config.getInitParameterNames();
			while (en.hasMoreElements()) {
				String name = en.nextElement();
				String value = config.getInitParameter(name);
				addPropertyValue(camelCase(name), value);
			}
		}

		/**
		 * read context init-parameters for backward compatibility.
		 * @param context servlet context
		 */
		public void addContextParams(ServletContext context) {
			@SuppressWarnings("unchecked")
			Enumeration<String> en = context.getInitParameterNames();
			while (en.hasMoreElements()) {
				String name = en.nextElement();
				String value = context.getInitParameter(name);
				String propname = camelCase(name);
				if (!contains(propname)) {
					addPropertyValue(propname, value);
				}
			}
		}
	}

	private UpdateThread thread = null;
	private RequestMapper mapper = null;
	private ServletContext context;
	// configPath translated into real path
	private String springConfigPath;

	private ApplicationContext appContext;

	// configuration parameters initialized from {@code init-param}s.
	private String configPath;
	private String loggingConfigPath;
	private long monitorMs = 10000;
	private String monitorFiles;

	/**
	 * Context relative path of Spring application configuration file.
	 * Required.
	 * @param springConfigPath
	 */
	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	public String getConfigPath() {
		return configPath;
	}
	/**
	 * Context relative path of Java Logging configuration file.
	 * @param loggingConfigPath
	 */
	public void setLoggingConfigPath(String loggingConfigPath) {
		this.loggingConfigPath = loggingConfigPath;
	}
	public String getLoggingConfigPath() {
		return loggingConfigPath;
	}
	/**
	 * Time interval in milliseconds for checking files listed in {@code montiorFiles}.
	 * @param monitorMs
	 */
	public void setMonitorMs(int monitorMs) {
		this.monitorMs = monitorMs;
	}
	public long getMonitorMs() {
		return monitorMs;
	}
	/**
	 * A comma-separated list of file system paths to check for context reload.
	 * @param monitorFiles
	 */
	public void setMonitorFiles(String monitorFiles) {
		this.monitorFiles = monitorFiles;
	}
	public String getMonitorFiles() {
		return monitorFiles;
	}

	public void init(FilterConfig config) throws ServletException {
		context = config.getServletContext();
		try {
			FilterConfigPropertyValues pvs = new FilterConfigPropertyValues(config);
			BeanWrapper bw = PropertyAccessorFactory
				.forBeanPropertyAccess(this);
			// add custom editors here if necessary.
			bw.setPropertyValues(pvs, true);
		} catch (BeansException ex) {
			throw new NestedServletException("failed to set bean properties: " +
					ex.getMessage(), ex);
		}

//		String logConfigPath = context.getInitParameter(LOGGING_CONFIG_PATH);
		if (loggingConfigPath != null) {
			String resolvedLogPath = context.getRealPath(loggingConfigPath);
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

		if (configPath == null) {
			throw new ServletException("Missing configPath parameter");
		}
		springConfigPath = context.getRealPath(configPath);

		if (monitorFiles == null) {
			// just load once:
			mapper = loadRequestMapper();
		} else {
			// we're in fancy mode: start the background thread to watch
			// our Spring config - it will swap out our mapper when things
			// change
			String[] monitored = monitorFiles.split(",");

			thread = new UpdateThread(this, monitorMs, Arrays.asList(monitored));

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
			if (request instanceof HttpServletRequest &&
					response instanceof HttpServletResponse) {
				if (mapper != null) {
					handled = mapper.handleRequest((HttpServletRequest)request,
						(HttpServletResponse)response);
				}
			}
		} finally {
			Thread.currentThread().setName(origThreadName);
		}
		if (!handled) {
			chain.doFilter(request, response);
		}
	}

	protected static void shutdownContext(ApplicationContext context) {
		if (context != null) {
			Map<String, ShutdownListener> listeners = context.getBeansOfType(
				ShutdownListener.class, false, false);
			for (Entry<String, ShutdownListener> e : listeners.entrySet()) {
				try {
					e.getValue().shutdown();
				} catch (Exception ex) {
					LOGGER.log(Level.SEVERE,
						"failed shutdown bean \"" + e.getKey() + "\"", ex);
				}
			}
		}
	}

	public void destroy() {
		LOGGER.info("Shutdown starting.");
		if (thread != null) {
			thread.interrupt();
		}
		shutdownContext(appContext);
		LOGGER.info("Shutdown complete.");
	}

	@SuppressWarnings("deprecation")
	private RequestMapper loadRequestMapper() {
		LOGGER.info("Initializing Spring config at: " + springConfigPath);

		appContext = new FileSystemXmlApplicationContext("file:" + springConfigPath);
		// save ApplicationContext in SpringReader.currentContext for backward compatibility.
		SpringReader.currentContext = appContext;
		// publish ApplicationContext to Spring-compatible ServletContext attribute.
		// so that it can be retrieved with WebApplicationContextUtils#getWebApplicationContext(ServletContext).
		context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);

		Map<String, RequestHandler> handlers = appContext.getBeansOfType(RequestHandler.class, false, false);
		RequestMapper newMapper = new RequestMapper(handlers.values(), context);

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

			ApplicationContext oldAppContext = appContext;
			RequestMapper mapper = filter.loadRequestMapper();

			if (fileSet.isChanged(startState)) {
				// erk.. files changed during the operation.. update nothing..
				LOGGER.warning("Files changed during Spring reload... discarding..");
				shutdownContext(appContext);
			} else {
				LOGGER.warning("Loaded RequestMapper.");
				filter.setMapper(mapper);
				if (oldAppContext != null) {
					LOGGER.warning("Shutting Down old ApplicationContext.");
					shutdownContext(oldAppContext);
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
