/* JSPExecRule
 *
 * $Id$
 *
 * Created on 12:36:59 PM Nov 5, 2009.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
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
package org.archive.wayback.replay.html.rules;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;

import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.htmlparser.Node;

public class JSPExecRule {
	private String jspPath = null;
	
	public void emit(ReplayParseContext context, Node node) throws ServletException, IOException {
		JSPExecutor jspExec = context.getJspExec();
		if(jspExec != null) {
			OutputStream os = context.getOutputStream();
			if(os != null) {
				String jspResult = jspExec.jspToString(jspPath);
				byte[] bytes = null;
				try {
					bytes = jspResult.getBytes(context.getOutputCharset());
				} catch(UnsupportedEncodingException e) {
					e.printStackTrace();
					bytes = jspResult.getBytes();
				}
				os.write(bytes);
			}
		}
	}

	/**
	 * @return the jspPath
	 */
	public String getJspPath() {
		return jspPath;
	}

	/**
	 * @param jspPath the jspPath to set
	 */
	public void setJspPath(String jspPath) {
		this.jspPath = jspPath;
	}
	
}
