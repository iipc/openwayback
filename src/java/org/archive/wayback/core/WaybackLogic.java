package org.archive.wayback.core;

import java.util.Properties;
import java.util.logging.Logger;

import org.archive.wayback.QueryUI;
import org.archive.wayback.ReplayUI;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;

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

	public WaybackLogic() {
		super();
		// TODO Auto-generated constructor stub
	}

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
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public QueryUI getQueryUI() {
		return queryUI;
	}

	public ReplayUI getReplayUI() {
		return replayUI;
	}

	public ResourceIndex getResourceIndex() {
		return resourceIndex;
	}

	public ResourceStore getResourceStore() {
		return resourceStore;
	}
}
