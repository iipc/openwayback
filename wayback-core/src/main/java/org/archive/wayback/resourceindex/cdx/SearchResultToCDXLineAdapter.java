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
package org.archive.wayback.resourceindex.cdx;

import java.util.Iterator;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResultToCDXLineAdapter implements 
Adapter<CaptureSearchResult,String>{

	private static int DEFAULT_CAPACITY = 120;
	private final static String DELIMITER = " ";
	private boolean outputRobot = false;
	
	public boolean isOutputRobot() {
		return outputRobot;
	}
	public void setIsOutputRobot(boolean isOutputRobot) {
		this.outputRobot = isOutputRobot;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public String adapt(CaptureSearchResult result) {
		
		StringBuilder sb = new StringBuilder(DEFAULT_CAPACITY);

		sb.append(result.getUrlKey());
		sb.append(DELIMITER);
		sb.append(result.getCaptureTimestamp());
		sb.append(DELIMITER);
		sb.append(result.getOriginalUrl());
		sb.append(DELIMITER);
		sb.append(result.getMimeType());
		sb.append(DELIMITER);
		sb.append(result.getHttpCode());
		sb.append(DELIMITER);
		sb.append(result.getDigest());
		sb.append(DELIMITER);
		sb.append(result.getRedirectUrl());
		sb.append(DELIMITER);
		if(outputRobot) {
			String robotFlags = result.getRobotFlags();
			if(robotFlags == null || robotFlags.equals("")) {
				robotFlags = "-";
			}
			sb.append(robotFlags);
			sb.append(DELIMITER);
		}
		sb.append(result.getOffset());
		sb.append(DELIMITER);
		sb.append(result.getFile());
			
		return sb.toString();
	}
	public static Iterator<String> adapt(Iterator<CaptureSearchResult> input) {
		return adapt(input,false);
	}

	public static Iterator<String> adapt(Iterator<CaptureSearchResult> input,
			boolean isOutputRobot) {
		SearchResultToCDXLineAdapter adapter =
			new SearchResultToCDXLineAdapter();
		adapter.setIsOutputRobot(isOutputRobot);
		return new AdaptedIterator<CaptureSearchResult,String>(input,adapter);
	}
}
