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
package org.archive.wayback.domainprefix;

import java.net.URI;
import java.net.URISyntaxException;

import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.replay.ReplayContext;
import org.archive.wayback.replay.ReplayURLTransformer;

/**
 * ReplayURIConverter for Domain Prefix mode.
 * It encodes <i>datespec</i> and <i>host</i> in hostname, as
 * sub-domain of {@code hostPort}.
 * <p>
 * This class also provides default {@link ReplayURLTransformer} for Domain Prefix mode.
 * </p>
 */
public class DomainPrefixReplayURIConverter implements ReplayURIConverter, ReplayURLTransformer {

	private String hostPort = "localhost:8081";

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		return makeReplayURI(datespec, url, null, URLStyle.ABSOLUTE);
	}

	@Override
	public String makeReplayURI(String datespec, String url, String flags,
			URLStyle style) {
		String replayURI = "";
		try {
			// ?? should this be moved to translate()?
			if (url.contains(hostPort)) {
				return url;
			}
			URI uri = new URI(url);
			StringBuilder sb = new StringBuilder(90);
			sb.append("http://");
			sb.append(datespec).append(".");
			sb.append(uri.getHost()).append(".");
			sb.append(hostPort);
			sb.append(uri.getPath());
			String query = uri.getQuery();
			if (query != null && query.length() > 0) {
				sb.append("?").append(query);
			}
			replayURI = sb.toString();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return replayURI;
	}

	@Override
	public ReplayURLTransformer getURLTransformer() {
		return this;
	}

	@Override
	public String transform(ReplayContext replayContext, String url,
			String contextFlags) {
		String absurl = url;
		try {
			absurl = replayContext.resolve(url);
		} catch (URISyntaxException ex) {
		}
		// TODO: should support URLStyle.PROTOCOL_RELATIVE? SERVER_RELATIVE should
		// work as well.
		return replayContext.makeReplayURI(absurl, null, URLStyle.ABSOLUTE);
	}

	/**
	 * @return the hostPort
	 */
	public String getHostPort() {
		return hostPort;
	}

	/**
	 * @param hostPort the hostPort to set
	 */
	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}

}
