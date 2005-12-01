/* WaybackLogic
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

package org.archive.wayback.core;

import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.exception.ConfigurationException;

/**
 * Constructor and go-between for the major components in the Wayback Machine.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackLogic implements PropertyConfigurable {
	private static final Logger LOGGER = Logger.getLogger(WaybackLogic.class
			.getName());

	private static final String REPLAY_URI_CONVERTER_PROPERTY =
		"replayuriconverter";

	private static final String REPLAY_RENDERER_PROPERTY = "replayrenderer";

	private static final String QUERY_RENDERER_PROPERTY = "queryrenderer";

	private static final String RESOURCE_STORE_PROPERTY = "resourcestore";

	private static final String RESOURCE_INDEX_PROPERTY = "resourceindex";

	private ReplayResultURIConverter uriConverter = null;

	private ReplayRenderer replayRenderer = null;

	private QueryRenderer queryRenderer = null;

	private ResourceIndex resourceIndex = null;

	private ResourceStore resourceStore = null;

	/**
	 * Constructor
	 */
	public WaybackLogic() {
		super();
	}

	/**
	 * Initialize this WaybackLogic. Pass in the specific configurations via
	 * Properties. Will construct and initialize implementations of
	 * ResourceIndex, ResourceResults, QueryUI, and ReplayUI.
	 * 
	 * @param p
	 *            Generic properties bag for configurations
	 * @throws ConfigurationException
	 */
	public void init(Properties p) throws ConfigurationException {
		LOGGER.info("WaybackLogic constructing classes...");

		uriConverter = (ReplayResultURIConverter) getInstance(p,
				REPLAY_URI_CONVERTER_PROPERTY);

		replayRenderer = (ReplayRenderer) getInstance(p,
				REPLAY_RENDERER_PROPERTY);

		queryRenderer = (QueryRenderer) getInstance(p, QUERY_RENDERER_PROPERTY);

		resourceStore = (ResourceStore) getInstance(p, RESOURCE_STORE_PROPERTY);
		resourceIndex = (ResourceIndex) getInstance(p, RESOURCE_INDEX_PROPERTY);

		LOGGER.info("WaybackLogic initialized classes...");

	}

	protected PropertyConfigurable getInstance(final Properties p,
			final String classPrefix) throws ConfigurationException {

		PropertyConfigurable result = null;

		String classNameKey = classPrefix + ".classname";
		String propertyPrefix = classPrefix + ".";
		String className = null;

		// build new class-specific Properties for class initialization:
		Properties classProperties = new Properties();
		for (Enumeration e = p.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();

			if (key.equals(classNameKey)) {

				// special .classname value:
				className = (String) p.get(key);

			} else if (key.startsWith(propertyPrefix)) {

				String finalKey = key.substring(propertyPrefix.length());
				String value = (String) p.get(key);
				classProperties.put(finalKey, value);

			}
		}

		// did we find the implementation class?
		if (className == null) {
			throw new ConfigurationException("No configuration for ("
					+ classNameKey + ")");
		}

		try {
			result = (PropertyConfigurable) Class.forName(className)
					.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
		LOGGER.info("new " + className + " created.");
		result.init(p);
		LOGGER.info("initialized " + className);

		return result;
	}

	/**
	 * @return Returns the resourceIndex.
	 */
	public ResourceIndex getResourceIndex() {
		return resourceIndex;
	}

	/**
	 * @return Returns the resourceStore.
	 */
	public ResourceStore getResourceStore() {
		return resourceStore;
	}

	/**
	 * @return Returns the uriConverter.
	 */
	public ReplayResultURIConverter getURIConverter() {
		return uriConverter;
	}

	/**
	 * @return Returns the replayRenderer.
	 */
	public ReplayRenderer getReplayRenderer() {
		return replayRenderer;
	}

	/**
	 * @return Returns the queryRenderer.
	 */
	public QueryRenderer getQueryRenderer() {
		return queryRenderer;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
}
