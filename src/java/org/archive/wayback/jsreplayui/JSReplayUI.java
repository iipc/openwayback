/* JSReplayUI
 *
 * Created on Oct 25, 2005
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the wayback (crawler.archive.org).
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.jsreplayui;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.rawreplayui.RawReplayUI;

/**
 * ReplayUI that inserts classic Wayback Machine Javascript into pages to
 * rewrite images and anchors for HTML pages.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class JSReplayUI extends RawReplayUI {

	/**
	 * Constructor
	 */
	public JSReplayUI() {
		super();
		// TODO Auto-generated constructor stub
	}

	private boolean isRawReplayResult(ResourceResult result) {
		if (-1 == result.getMimeType().indexOf("text/html")) {
			return true;
		}
		return false;
	}

	public void replayResource(WMRequest wmRequest, ResourceResult result,
			Resource resource, HttpServletRequest request,
			HttpServletResponse response, ResourceResults results)
			throws IOException {

		if (resource == null) {
			throw new IllegalArgumentException("No resource");
		}
		if (result == null) {
			throw new IllegalArgumentException("No result");
		}
		if (isRawReplayResult(result)) {
			super.replayResource(wmRequest, result, resource, request,
					response, results);
			return;
		}

		ARCRecord record = resource.getArcRecord();
		record.skipHttpHeader();
		copyRecordHttpHeader(response, record, true);
		// slurp the whole thing into RAM:
		byte[] bbuffer = new byte[4 * 1024];
		StringBuffer sbuffer = new StringBuffer();
		for (int r = -1; (r = record.read(bbuffer, 0, bbuffer.length)) != -1;) {
			String chunk = new String(bbuffer);
			sbuffer.append(chunk.substring(0, r));
		}

		markUpPage(sbuffer, result, results, request);
		
		response.setHeader("Content-Length", "" + sbuffer.length());
		ServletOutputStream out = response.getOutputStream();
		out.print(new String(sbuffer));
	}

	private void markUpPage(StringBuffer page, ResourceResult result,
			ResourceResults results, HttpServletRequest request) {
		insertBaseTag(page, result, request);
		insertJavascript(page, result, request);
	}

	private void insertBaseTag(StringBuffer page, ResourceResult result,
			HttpServletRequest request) {
		String resultUrl = result.getUrl();
		String baseTag = "<BASE HREF=\"http://" + resultUrl + "\">";
		int insertPoint = page.indexOf("<head>");
		if (-1 == insertPoint) {
			insertPoint = page.indexOf("<HEAD>");
		}
		if (-1 == insertPoint) {
			insertPoint = 0;
		} else {
			insertPoint += 6; // just after the tag
		}
		page.insert(insertPoint, baseTag);
	}

	private void insertJavascript(StringBuffer page, ResourceResult result,
			HttpServletRequest request) {
		String resourceTS = result.getTimestamp().getDateStr();
		String nowTS;
		try {
			nowTS = Timestamp.currentTimestamp().getDateStr();
		} catch (ParseException e) {
			nowTS = "UNKNOWN";
		}
		
		String protocol = "http";
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String context = request.getContextPath();
		String contextPath = protocol + "://" + serverName
				+ (serverPort == 80 ? "" : ":" + serverPort) + context + "/"
				+ result.getTimestamp().getDateStr() + "/";
		
		String scriptInsert = "<SCRIPT language=\"Javascript\">\n"
				+ "<!--\n"
				+ "\n"
				+ "//		 FILE ARCHIVED ON " + resourceTS + " AND RETRIEVED FROM THE\n"
				+ "//		 INTERNET ARCHIVE ON " + nowTS + ".\n"
				+ "//		 JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.\n"
				+ "//\n"
				+ "// ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.\n"
				+ "// SECTION 108(a)(3)).\n"
				+ "\n"
				+ "		   var sWayBackCGI = \"" + contextPath + "\";\n"
				+ "		   \n"
				
				+ "function xResolveUrl(url) {\n"
				+ "   var image = new Image();\n"
				+ "   image.src = url;\n"
				+ "   return image.src;\n"
				+ "}\n"
				+ "function xLateUrl(aCollection, sProp) {\n"
				+ "   var i = 0;\n"
				+ "   for(i = 0; i < aCollection.length; i++) {\n"
				+ "      if (typeof(aCollection[i][sProp]) == \"string\") {\n" 
				+ "       if (aCollection[i][sProp].indexOf(\"mailto:\") == -1 &&\n"
				+ "          aCollection[i][sProp].indexOf(\"javascript:\") == -1) {\n"
				+ "          if(aCollection[i][sProp].indexOf(\"http\") == 0) {\n"
				+ "              aCollection[i][sProp] = sWayBackCGI + aCollection[i][sProp];\n"
				+ "          } else {\n"
				+ "              aCollection[i][sProp] = sWayBackCGI + xResolveUrl(aCollection[i][sProp]);\n"
				+ "          }\n"
				+ "       }\n"
				+ "      }\n"
				+ "   }\n"
				+ "}\n"
				+ "		   \n"
				+ "        xLateUrl(document.getElementsByTagName(\"IMG\"),\"src\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"A\"),\"href\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"AREA\"),\"href\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"OBJECT\"),\"codebase\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"OBJECT\"),\"data\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"APPLET\"),\"codebase\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"APPLET\"),\"archive\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"EMBED\"),\"src\");\n"
				+ "        xLateUrl(document.getElementsByTagName(\"BODY\"),\"background\");\n"
				+ "\n" 
				+ "//		-->\n" 
				+ "\n"
				+ "</SCRIPT>\n";

		int insertPoint = page.indexOf("</body>");
		if (-1 == insertPoint) {
			insertPoint = page.indexOf("</BODY>");
		}
		if (-1 == insertPoint) {
			insertPoint = page.length();
		}
		page.insert(insertPoint, scriptInsert);
	}
}
