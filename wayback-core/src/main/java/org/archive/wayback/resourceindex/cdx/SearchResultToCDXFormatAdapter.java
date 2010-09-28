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
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;

public class SearchResultToCDXFormatAdapter implements
		Adapter<CaptureSearchResult, String> {

	private CDXFormat cdx = null;

	public SearchResultToCDXFormatAdapter(CDXFormat cdx) {
		this.cdx = cdx;
	}

	public String adapt(CaptureSearchResult o) {
		return cdx.serializeResult(o);
	}
	public static Iterator<String> adapt(Iterator<CaptureSearchResult> input,
			CDXFormat cdx) {
		SearchResultToCDXFormatAdapter adapter =
			new SearchResultToCDXFormatAdapter(cdx);
		return new AdaptedIterator<CaptureSearchResult,String>(input,adapter);
	}
}
