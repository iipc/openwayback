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
package org.archive.wayback.replay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StringHttpServletResponseWrapper extends HttpServletResponseWrapper {
	private final static String WRAPPED_CHAR_ENCODING = "UTF-8";
	private StringWriter sw = new StringWriter();
	private String origEncoding = null;
	private static final ServletOutputStream FAKE_OUT = new ServletOutputStream() {
                @Override
		public void write(int b) throws IOException {
		}
	}; 
	
	/**
	 * @param response The HttpServletResponse to use as a base to this wrapper.
	 */
	public StringHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
		origEncoding = getCharacterEncoding();
		setCharacterEncoding(WRAPPED_CHAR_ENCODING);
	}

    /**
     *
     * @return A ServletOutputStream that does nothing, ie. has an empty write() method.
     * @throws IOException
     */
    @Override
	public ServletOutputStream getOutputStream() throws IOException {
		return FAKE_OUT; 
	}

    /**
     * This method overrides the default behaviour, and does nothing.
     */
    @Override
	public void reset()
	{
		//do nothing
	}

    /**
     * This method overrides the default behaviour, and does nothing.
     * @throws IOException
     */
    @Override
	public void flushBuffer() throws IOException {
		//do nothing
	}

    /**
     * This method overrides the default behaviour, and does nothing.
     */
    @Override
	public void resetBuffer() {
		//do nothing
	}

    /**
     *
     * @return A PrintWriter for the underlying StringWriter. 
     */
    @Override
	public PrintWriter getWriter() {
		return new PrintWriter(sw);
	}
	/**
	 * @return the underlying StringWriter converted to a String.
	 */
	public String getStringResponse() {
		setCharacterEncoding(origEncoding);
		return sw.toString();
	}
}
