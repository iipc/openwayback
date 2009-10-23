/* SearchResultToCDXLineAdapter
 *
 * $Id$
 *
 * Created on 3:22:15 PM Jul 26, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
