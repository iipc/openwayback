/* InPagePresenceReplayUI
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.ippreplayui;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.rawreplayui.RawReplayUI;

/**
 * ReplayUI that inserts a DIV HTML tag at the end of a returned HTML document.
 * Unused at present -- presently a proof of concept second ReplayUI
 * implementation.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class InPagePresenceReplayUI extends RawReplayUI {

	/**
	 * Constructor
	 */
	public InPagePresenceReplayUI() {
		super();
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
		insertIPP(sbuffer, result, results);
		response.setHeader("Content-Length", "" + sbuffer.length());
		ServletOutputStream out = response.getOutputStream();
		out.print(new String(sbuffer));
	}

	private void insertIPP(StringBuffer page, ResourceResult result,
			ResourceResults results) {

		int idx = findIPPInsertPoint(page);
		String ippInsert = makeIPPInsert(result, results);
		page.insert(idx, ippInsert);
	}

	private int findIPPInsertPoint(StringBuffer page) {
		return page.length();
	}

	private String makeIPPInsert(ResourceResult result, ResourceResults results) {
		StringBuffer ippInsert = new StringBuffer();
		ippInsert.append("<DIV NAME=\"iawm_ipp\">");
		ippInsert.append("IPP");
		ippInsert.append("</DIV>");
		return ippInsert.toString();
	}
}
