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

import java.util.Properties;
import java.util.logging.Logger;

import org.archive.wayback.QueryUI;
import org.archive.wayback.ReplayUI;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;

/**
 * Constructor and go-between for the major components in the Wayback Machine.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackLogic {
	private static final Logger LOGGER = Logger.getLogger(WaybackLogic.class
			.getName());

	private static final String REPLAY_UI_CLASS = "replayui.class";

	private static final String QUERY_UI_CLASS = "queryui.class";

	private static final String RESOURCE_STORE_CLASS = "resourcestore.class";

	private static final String RESOURCE_INDEX_CLASS = "resourceindex.class";

	private ReplayUI replayUI = null;

	private QueryUI queryUI = null;

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
	 * @throws Exception
	 */
	public void init(Properties p) throws Exception {
		LOGGER.info("WaybackLogic constructing classes...");

		replayUI = (ReplayUI) getInstance(p, REPLAY_UI_CLASS, "replayui");
		queryUI = (QueryUI) getInstance(p, QUERY_UI_CLASS, "queryUI");
		resourceStore = (ResourceStore) getInstance(p, RESOURCE_STORE_CLASS,
				"resourceStore");
		resourceIndex = (ResourceIndex) getInstance(p, RESOURCE_INDEX_CLASS,
				"resourceIndex");

		LOGGER.info("WaybackLogic initializing classes...");

		try {

			replayUI.init(p);
			LOGGER.info("initialized replayUI");
			queryUI.init(p);
			LOGGER.info("initialized queryUI");
			resourceStore.init(p);
			LOGGER.info("initialized resourceStore");
			resourceIndex.init(p);
			LOGGER.info("initialized resourceIndex");

		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	protected Object getInstance(final Properties p,
			final String classProperty, final String pretty) throws Exception {
		Object result = null;

		String className = (String) p.get(classProperty);
		if ((className == null) || (className.length() <= 0)) {
			throw new Exception("No config (" + classProperty + " for "
					+ pretty + ")");
		}

		try {
			result = Class.forName(className).newInstance();
			LOGGER.info("new " + className + " " + pretty + " created.");
		} catch (Exception e) {
			// Convert. Add info.
			throw new Exception("Failed making " + pretty + " with "
					+ className + ": " + e.getMessage());
		}
		return result;
	}

	/**
	 * @return Returns the queryUI.
	 */
	public QueryUI getQueryUI() {
		return queryUI;
	}

	/**
	 * @return Returns the replayUI.
	 */
	public ReplayUI getReplayUI() {
		return replayUI;
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
	 * @param args
	 */
	public static void main(String[] args) {

	}
}
