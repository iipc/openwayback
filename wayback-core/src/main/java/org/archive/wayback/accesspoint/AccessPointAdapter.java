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

package org.archive.wayback.accesspoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.archive.wayback.ExceptionRenderer;
import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.RequestParser;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.accesscontrol.CompositeExclusionFilterFactory;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.accesscontrol.oracleclient.CustomPolicyOracleFilter;
import org.archive.wayback.accesspoint.proxy.ProxyAccessPoint;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.operator.BooleanOperator;
import org.archive.wayback.webapp.AccessPoint;
import org.archive.wayback.webapp.CustomResultFilterFactory;
import org.archive.wayback.webapp.LiveWebRedirector;
import org.archive.wayback.webapp.WaybackCollection;

public class AccessPointAdapter extends AccessPoint {
	
	private CompositeAccessPoint baseAccessPoint;
	private AccessPointConfig config;
	private ExclusionFilterFactory exclusionFactory;
	private ResultURIConverter cacheUriConverter;
	private Properties props = null;
	
	private boolean switchable = false;
	
	private class DynamicExclusionFactory implements ExclusionFilterFactory
	{
		public ExclusionFilter get()
		{
			return new CustomPolicyOracleFilter(baseAccessPoint.getOracleUrl(), config.getBeanName(), null);
		}
		
		public void shutdown() {

		}		
	}
	
	public AccessPointAdapter(CompositeAccessPoint baseAccessPoint, AccessPointConfig config)
	{
		this.baseAccessPoint = baseAccessPoint;
		this.config = config;
		this.exclusionFactory = null;
		
		this.switchable = true;
		initMergedProps();
	}
	
	public AccessPointAdapter(String accessPointName, CompositeAccessPoint baseAccessPoint)
	{
		this.baseAccessPoint = baseAccessPoint;
		this.exclusionFactory = null;
		this.config = baseAccessPoint.getAccessPointConfigs().getAccessPointConfigs().get(accessPointName);
		
		this.switchable = false;
		initMergedProps();
	}
	
	protected void initMergedProps()
	{
		this.props = new Properties();
		
		//First put the generic ones
		if (baseAccessPoint.getConfigs() != null) {
			props.putAll(baseAccessPoint.getConfigs());
		}
		
		//Now, the custom ones for this config
		if (config.getConfigs() != null) {
			props.putAll(config.getConfigs());
		}
	}
	
	public CompositeAccessPoint getBaseAccessPoint()
	{
		return baseAccessPoint;
	}
	
	public boolean isProxyMode()
	{
		return baseAccessPoint.isProxyEnabled();
	}
	
	public boolean isProxySwitchable()
	{
		return switchable && isProxyMode();
	}
	
	public String getSwitchCollPath()
	{
		return ProxyAccessPoint.SWITCH_COLLECTION_PATH;
	}
	
	public AccessPointConfig getAccessPointConfig()
	{
		return config;
	}
	
	@Override
	public List<String> getFileIncludePrefixes() {
		return config.getFileIncludePrefixes();
	}

	@Override
	public List<String> getFileExcludePrefixes() {
		return config.getFileExcludePrefixes();
	}
	
	@Override
	public Properties getConfigs() {
		return props;
	}
	
	@Override
	public String getAccessPointPath()
	{
		return config.getBeanName();
	}
	
	public boolean hasExclusions()
	{
		return (baseAccessPoint.getStaticExclusions() != null) || (baseAccessPoint.getOracleUrl() != null);
	}

	@Override
	public ExclusionFilterFactory getExclusionFactory() {
		
		if (!hasExclusions()) {
			return null;
		}
		
		if (exclusionFactory == null) {	
			exclusionFactory = buildExclusionFactory();
		}
		
		return exclusionFactory;
	}
	
	protected ExclusionFilterFactory buildExclusionFactory()
	{	
		ArrayList<ExclusionFilterFactory> staticExclusions = baseAccessPoint.getStaticExclusions();
		
		if (staticExclusions == null) {
			return new DynamicExclusionFactory();
		} else {
			CompositeExclusionFilterFactory composite = new CompositeExclusionFilterFactory();
			ArrayList<ExclusionFilterFactory> allExclusions = new ArrayList<ExclusionFilterFactory>();
			allExclusions.addAll(staticExclusions);
			if (baseAccessPoint.getOracleUrl() != null) {
				allExclusions.add(new DynamicExclusionFactory());
			}
			composite.setFactories(allExclusions);
			return composite;
		}
	}
	
	protected String getPrefix(String basePrefix)
	{
		if (isProxyMode()) {
			return basePrefix;
		} else {
			return basePrefix + config.getBeanName() + "/";
		}
	}
	
	@Override
	public String getStaticPrefix() {
		return baseAccessPoint.getStaticPrefix();
	}

	@Override
	public String getReplayPrefix() {
		return getPrefix(baseAccessPoint.getReplayPrefix());
	}

	@Override
	public String getQueryPrefix() {
		return getPrefix(baseAccessPoint.getQueryPrefix());
	}

	@Override
	public boolean isExactHostMatch() {
		return baseAccessPoint.isExactHostMatch();
	}

	@Override
	public boolean isExactSchemeMatch() {
		return baseAccessPoint.isExactSchemeMatch();
	}

	@Override
	public boolean isUseAnchorWindow() {
		return baseAccessPoint.isUseAnchorWindow();
	}

	@Override
	public boolean isServeStatic() {
		return baseAccessPoint.isServeStatic();
	}
	
	@Override	
	public ServletContext getServletContext() {
		return baseAccessPoint.getServletContext();
	}

	@Override
	public LiveWebRedirector getLiveWebRedirector() {
		return baseAccessPoint.getLiveWebRedirector();
	}
	
	@Override
	public String getLiveWebPrefix() {
		return baseAccessPoint.getLiveWebPrefix();
	}

	@Override
	public String getInterstitialJsp() {
		return baseAccessPoint.getInterstitialJsp();
	}

	@Override
	public Locale getLocale() {
		return baseAccessPoint.getLocale();
	}

	@Override
	public List<String> getFilePatterns() {
		return baseAccessPoint.getFilePatterns();
	}
	
	@Override
	public WaybackCollection getCollection() {
		if (config.getCollection() != null) {
			return config.getCollection();
		} else {
			return baseAccessPoint.getCollection();
		}
	}

	@Override
	public ExceptionRenderer getException() {
		return baseAccessPoint.getException();
	}

	@Override
	public QueryRenderer getQuery() {
		return baseAccessPoint.getQuery();
	}

	@Override
	public RequestParser getParser() {
		RequestParser requestParser = config.getRequestParser();
		
		if (requestParser != null) {
			return requestParser;
		} else {		
			return baseAccessPoint.getParser();
		}
	}

	@Override
	public ReplayDispatcher getReplay() {
		return baseAccessPoint.getReplay();
	}

	@Override
	public ResultURIConverter getUriConverter() {
		
		if (cacheUriConverter == null) {		
			ContextResultURIConverterFactory factory = baseAccessPoint.getUriConverterFactory();
			
			if (factory != null) {
				cacheUriConverter = factory.getContextConverter(getReplayPrefix());
			} else {
				cacheUriConverter = baseAccessPoint.getUriConverter();
			}
		}
		
		return cacheUriConverter;
	}

	@Override
	public BooleanOperator<WaybackRequest> getAuthentication() {
		return baseAccessPoint.getAuthentication();
	}

	@Override
	public String getRefererAuth() {
		return baseAccessPoint.getRefererAuth();
	}

	@Override
	public boolean isBounceToReplayPrefix() {
		return baseAccessPoint.isBounceToReplayPrefix();
	}

	@Override
	public boolean isBounceToQueryPrefix() {
		return baseAccessPoint.isBounceToQueryPrefix();
	}

	@Override
	public long getEmbargoMS() {
		return baseAccessPoint.getEmbargoMS();
	}

	@Override
	public boolean isForceCleanQueries() {
		// Setting this to false to allow custom handling of adapter access points
		return false;
	}

	@Override
	public CustomResultFilterFactory getFilterFactory() {
		return baseAccessPoint.getFilterFactory();
	}
	
	@Override
	public UrlCanonicalizer getSelfRedirectCanonicalizer()
	{
		return baseAccessPoint.getSelfRedirectCanonicalizer();
	}

	@Override
	public boolean isRequestAuth() {
		return baseAccessPoint.isRequestAuth();
	}

	@Override
	public int getMaxRedirectAttempts() {
		return baseAccessPoint.getMaxRedirectAttempts();
	}

	@Override
	public boolean isTimestampSearch() {
		return baseAccessPoint.isTimestampSearch();
	}

	@Override
	public String getPerfStatsHeader() {
		return baseAccessPoint.getPerfStatsHeader();
	}

	@Override
	public String getWarcFileHeader() {
		return baseAccessPoint.getWarcFileHeader();
	}
}
