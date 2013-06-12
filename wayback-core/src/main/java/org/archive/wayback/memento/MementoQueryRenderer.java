package org.archive.wayback.memento;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.AccessPoint;

public class MementoQueryRenderer implements QueryRenderer, MementoConstants {
	private static final Logger LOG =
		Logger.getLogger(MementoQueryRenderer.class.getName());
	
	private static final String DEFAULT_TIMEGATE_JSP =
		"/WEB-INF/memento/Timegate.jsp";
	private static final String DEFAULT_TIMEMAP_RDF_JSP =
		"/WEB-INF/memento/TimemapRDF.jsp";
	private static final String DEFAULT_TIMEMAP_LINK_JSP = 
		"/WEB-INF/memento/TimemapLink.jsp";
	
	
	private String defaultFormat = FORMAT_RDF;
	private Properties formatMap = null;

	public MementoQueryRenderer() {
		formatMap = new Properties();
		formatMap.put(TIMEGATE, DEFAULT_TIMEGATE_JSP);
		formatMap.put(FORMAT_RDF, DEFAULT_TIMEMAP_RDF_JSP);
		formatMap.put(FORMAT_LINK, DEFAULT_TIMEMAP_LINK_JSP);
	}
	
	public void renderCaptureResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {

		// assume a TimeMap query:
		String format = MementoUtils.getRequestFormat(wbRequest);
		if(format == null) {
			AccessPoint ap = wbRequest.getAccessPoint();
			LOG.warning(String.format("No format for(%s)",
					ap.translateRequestPathQuery(httpRequest)));
			format = defaultFormat;
		}
		String handlerJsp = formatMap.getProperty(format);
		if(handlerJsp == null) {
			AccessPoint ap = wbRequest.getAccessPoint();
			LOG.severe(String.format("No format handler for (%s): (%s)",
					format, ap.translateRequestPathQuery(httpRequest)));
			httpResponse.sendError(500,"No format handler for " + format);
		} else {
			UIResults uiResults = new UIResults(wbRequest,uriConverter,results);
			uiResults.forward(httpRequest, httpResponse, handlerJsp);
		}
	}

	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse response, WaybackRequest wbRequest,
			UrlSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {
		// not valid - error:
		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		response.setContentType("text/plain");
		PrintWriter pw = response.getWriter();
		pw.println("UrlQuery not implemented in Memento");
	}

	/**
	 * @return the defaultFormat
	 */
	public String getDefaultFormat() {
		return defaultFormat;
	}

	/**
	 * @param defaultFormat the defaultFormat to set
	 */
	public void setDefaultFormat(String defaultFormat) {
		this.defaultFormat = defaultFormat;
	}

	/**
	 * @return the formatMap
	 */
	public Properties getFormatMap() {
		return formatMap;
	}

	/**
	 * @param formatMap the formatMap to set
	 */
	public void setFormatMap(Properties formatMap) {
		this.formatMap = formatMap;
	}

}
