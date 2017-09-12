package org.archive.wayback.util.webapp;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

public class RequestFilterTest extends TestCase {

	File contextDir = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		contextDir = createTestContext();
	}
	
	protected void tearDown() throws Exception {
		if (contextDir != null && contextDir.exists()) {
			FileUtils.deleteQuietly(contextDir);
		}
	}
	
	protected File createTestContext() throws IOException {
		final File tmp = File.createTempFile("context", ".dir");
		if (!tmp.delete())
			throw new IOException("Failed to delete temp file " + tmp);
		if (!tmp.mkdir())
			throw new IOException("Failed to create temp directory " + tmp);
		return tmp;
	}

	public static class StubFilterConfig implements FilterConfig {
		final Map<String, String> params;
		final ServletContext servletContext;
		public StubFilterConfig() {
			this.params = new HashMap<String, String>();
			this.servletContext = EasyMock.createNiceMock(ServletContext.class);
		}

		@Override
		public String getFilterName() {
			return "RequestFilter";
		}

		@Override
		public ServletContext getServletContext() {
			return servletContext;
		}

		@Override
		public String getInitParameter(String name) {
			return params.get(name);
		}

		@Override
		public Enumeration getInitParameterNames() {
			return Collections.enumeration(params.keySet());
		}
		
	}

	static final String APP_CONFIG = "<?xml version='1.0' encoding='UTF-8'?>\n" +
			"<beans xmlns='http://www.springframework.org/schema/beans'\n" +
			"       xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n" +
			"       xsi:schemaLocation='http://www.springframework.org/schema/beans\n" +
			"           http://www.springframework.org/schema/beans/spring-beans-2.5.xsd'>\n" +
			"</beans>\n";
	
	/**
	 * Test init parameters.
	 * @throws Exception
	 */
	public void testInitParameters() throws Exception {
		StubFilterConfig filterConfig = new StubFilterConfig();
		filterConfig.params.put("configPath", "/WEB-INF/wayback.xml");
		filterConfig.params.put("loggingConfigPath", "/WEB-INF/logging.properties");
		
		final File context = createTestContext();
		final File webinf = new File(context, "WEB-INF");
		if (!webinf.mkdir())
			throw new IOException("failed to create directory " + webinf);
		final File appConfig = new File(webinf, "wayback.xml");
		FileUtils.write(appConfig, APP_CONFIG, "UTF-8");
		
		EasyMock.expect(filterConfig.servletContext.getRealPath(EasyMock.<String>anyObject())).andStubAnswer(new IAnswer<String>() {
			@Override
			public String answer() throws Throwable {
				String relPath = (String)EasyMock.getCurrentArguments()[0];
				if (relPath.startsWith("/"))
					relPath = relPath.substring(1);
				return new File(context, relPath).getAbsolutePath();
			}
		});
		EasyMock.expect(filterConfig.servletContext.getInitParameterNames()).andAnswer(new IAnswer<Enumeration>() {
			public Enumeration answer() throws Throwable {
				return Collections.emptyEnumeration();
			};
		});
		EasyMock.replay(filterConfig.servletContext);
		
		RequestFilter filter = new RequestFilter();
		filter.init(filterConfig);

		assertEquals("/WEB-INF/wayback.xml", filter.getConfigPath());
		assertEquals("/WEB-INF/logging.properties", filter.getLoggingConfigPath());
	}

	/**
	 * Old init parameter names with "-" between words also work.
	 * Parameters may be specified in {@code context-param} as well.
	 * @throws Exception
	 */
	public void testInitParametersCompat() throws Exception {
		StubFilterConfig filterConfig = new StubFilterConfig();
		filterConfig.params.put("config-path", "/WEB-INF/wayback.xml");
		
		final File context = createTestContext();
		final File webinf = new File(context, "WEB-INF");
		if (!webinf.mkdir())
			throw new IOException("failed to create directory " + webinf);
		final File appConfig = new File(webinf, "wayback.xml");
		FileUtils.write(appConfig, APP_CONFIG, "UTF-8");
		
		EasyMock.expect(filterConfig.servletContext.getRealPath(EasyMock.<String>anyObject())).andStubAnswer(new IAnswer<String>() {
			@Override
			public String answer() throws Throwable {
				String relPath = (String)EasyMock.getCurrentArguments()[0];
				if (relPath.startsWith("/"))
					relPath = relPath.substring(1);
				return new File(context, relPath).getAbsolutePath();
			}
		});
		// logging-config-path is specified in context-param
		final Map<String, String> contextParams = new HashMap<String, String>();
		contextParams.put("logging-config-path", "/WEB-INF/logging.properties");
		EasyMock.expect(filterConfig.servletContext.getInitParameterNames()).andStubAnswer(new IAnswer<Enumeration>() {
			@Override
			public Enumeration answer() throws Throwable {
				return Collections.enumeration(contextParams.keySet());
			}
		});
		EasyMock.expect(filterConfig.servletContext.getInitParameter(EasyMock.<String>anyObject())).andStubAnswer(new IAnswer<String>() {
			@Override
			public String answer() throws Throwable {
				return contextParams.get(EasyMock.getCurrentArguments()[0]);
			}
		});
		EasyMock.replay(filterConfig.servletContext);
		
		RequestFilter filter = new RequestFilter();
		filter.init(filterConfig);

		assertEquals("/WEB-INF/wayback.xml", filter.getConfigPath());
		assertEquals("/WEB-INF/logging.properties", filter.getLoggingConfigPath());
	}
}
