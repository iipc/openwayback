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
import java.util.Map;
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
import org.archive.wayback.webapp.WaybackCollection;

public class AccessPointAdapter extends AccessPoint {
	
	private CompositeAccessPoint baseAccessPoint;
	private AccessPointConfig config;
	private ExclusionFilterFactory exclusionFactory;
	private ResultURIConverter cacheUriConverter;
	
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
	}
	
	public AccessPointAdapter(String accessPointName, CompositeAccessPoint baseAccessPoint)
	{
		this.baseAccessPoint = baseAccessPoint;
		this.exclusionFactory = null;
		this.config = baseAccessPoint.getAccessPointConfigs().getAccessPointConfigs().get(accessPointName);
		
		this.switchable = false;
	}
	
	public CompositeAccessPoint getBaseAccessPoint()
	{
		return baseAccessPoint;
	}
	
	public boolean isProxyMode()
	{
		return (baseAccessPoint instanceof ProxyAccessPoint);
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
	
	public Map<String, Object> getUserProps()
	{
		return baseAccessPoint.getUserProps();
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
		// TODO Auto-generated method stub
		return config.getConfigs();
	}
	
	@Override
	public String getAccessPointPath()
	{
		return config.getBeanName();
	}

	@Override
	public ExclusionFilterFactory getExclusionFactory() {
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
			allExclusions.add(new DynamicExclusionFactory());
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
		// TODO Auto-generated method stub
		return getPrefix(baseAccessPoint.getStaticPrefix());
	}

	@Override
	public String getReplayPrefix() {
		// TODO Auto-generated method stub
		return getPrefix(baseAccessPoint.getReplayPrefix());
	}

	@Override
	public String getQueryPrefix() {
		// TODO Auto-generated method stub
		return getPrefix(baseAccessPoint.getQueryPrefix());
	}

	@Override
	public boolean isExactHostMatch() {
		// TODO Auto-generated method stub
		return baseAccessPoint.isExactHostMatch();
	}

	@Override
	public boolean isExactSchemeMatch() {
		// TODO Auto-generated method stub
		return baseAccessPoint.isExactSchemeMatch();
	}

	@Override
	public boolean isUseAnchorWindow() {
		// TODO Auto-generated method stub
		return baseAccessPoint.isUseAnchorWindow();
	}

	@Override
	public boolean isServeStatic() {
		// TODO Auto-generated method stub
		return baseAccessPoint.isServeStatic();
	}
	
	@Override	
	public ServletContext getServletContext() {
		return baseAccessPoint.getServletContext();
	}

	@Override
	public String getLiveWebPrefix() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getLiveWebPrefix();
	}

	@Override
	public String getInterstitialJsp() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getInterstitialJsp();
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getLocale();
	}

	@Override
	public List<String> getFilePatterns() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getFilePatterns();
	}

	@Override
	public WaybackCollection getCollection() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getCollection();
	}

	@Override
	public ExceptionRenderer getException() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getException();
	}

	@Override
	public QueryRenderer getQuery() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getQuery();
	}

	@Override
	public RequestParser getParser() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getParser();
	}

	@Override
	public ReplayDispatcher getReplay() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return baseAccessPoint.getAuthentication();
	}

	@Override
	public String getRefererAuth() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getRefererAuth();
	}

	@Override
	public boolean isBounceToReplayPrefix() {
		// TODO Auto-generated method stub
		return baseAccessPoint.isBounceToReplayPrefix();
	}

	@Override
	public boolean isBounceToQueryPrefix() {
		// TODO Auto-generated method stub
		return baseAccessPoint.isBounceToQueryPrefix();
	}

	@Override
	public long getEmbargoMS() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getEmbargoMS();
	}

	@Override
	public boolean isForceCleanQueries() {
		// TODO Auto-generated method stub
		return baseAccessPoint.isForceCleanQueries();
	}

	@Override
	public CustomResultFilterFactory getFilterFactory() {
		// TODO Auto-generated method stub
		return baseAccessPoint.getFilterFactory();
	}
	
	@Override
	public UrlCanonicalizer getSelfRedirectCanonicalizer()
	{
		return baseAccessPoint.getSelfRedirectCanonicalizer();
	}
}
