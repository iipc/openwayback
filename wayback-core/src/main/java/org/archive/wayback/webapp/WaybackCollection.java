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
package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.List;

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.Shutdownable;
import org.archive.wayback.exception.ConfigurationException;

/**
 * Composite class containing a ResourceStore, and a ResourceIndex, to simplify
 * sharing them as a pair across multiple AccessPoints.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WaybackCollection {
	private ResourceStore resourceStore = null;
	private ResourceIndex resourceIndex = null;
	private List<Shutdownable> shutdownables = null;
	private boolean shutdownDone = false;

	/**
	 * close/release any resources held by this WaybackCollection
	 * @throws IOException when thrown by an internal class being shut down.
	 */
	public void shutdown() throws IOException {
		if(shutdownDone) {
			return;
		}
		if(resourceStore != null) {
			resourceStore.shutdown();
		}
		if(resourceIndex != null) {
			resourceIndex.shutdown();
		}
		if(shutdownables != null) {
			for(Shutdownable s : shutdownables) {
				s.shutdown();
			}
		}
		shutdownDone = true;
	}

	/**
	 * @return the ResourceStore used with this WaybackCollection
	 * @throws ConfigurationException if none is configured
	 */
	public ResourceStore getResourceStore() throws ConfigurationException {
		if(resourceStore == null) {
			throw new ConfigurationException("No resourceStore declared");
		}
		return resourceStore;
	}
	/**
	 * @param resourceStore the ResourceStore to use with this WaybackCollection
	 */
	public void setResourceStore(ResourceStore resourceStore) {
		this.resourceStore = resourceStore;
	}
	/**
	 * @return the ResourceIndex used with this WaybackCollection
	 * @throws ConfigurationException if none is configured
	 */
	public ResourceIndex getResourceIndex() throws ConfigurationException {
		if(resourceIndex == null) {
			throw new ConfigurationException("No resourceIndex declared");
		}
		return resourceIndex;
	}
	/**
	 * @param resourceIndex the ResourceIndex to use with this WaybackCollection
	 */
	public void setResourceIndex(ResourceIndex resourceIndex) {
		this.resourceIndex = resourceIndex;
	}

	/**
	 * @return List of Shutdownable objects associated with this 
	 * WaybackCollection, or null, if none are configured
	 */
	public List<Shutdownable> getShutdownables() {
		return shutdownables;
	}

	/**
	 * @param shutdownables set a List of Shutdownable objects associated with
	 * this WaybackCollection
	 */
	public void setShutdownables(List<Shutdownable> shutdownables) {
		this.shutdownables = shutdownables;
	}
}
