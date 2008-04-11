/* WaybackCollection
 *
 * $Id$
 *
 * Created on 11:28:52 AM Sep 28, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.webapp;

import java.io.IOException;

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.exception.ConfigurationException;

/**
 * Abstraction point for sharing document collection and index across multiple
 * AccessPoints.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WaybackCollection {
	private ResourceStore resourceStore = null;
	private ResourceIndex resourceIndex = null;
	private boolean shutdownDone = false;
	public ResourceStore getResourceStore() throws ConfigurationException {
		if(resourceStore == null) {
			throw new ConfigurationException("No resourceStore declared");
		}
		return resourceStore;
	}
	public void setResourceStore(ResourceStore resourceStore) {
		this.resourceStore = resourceStore;
	}
	public ResourceIndex getResourceIndex() throws ConfigurationException {
		if(resourceIndex == null) {
			throw new ConfigurationException("No resourceIndex declared");
		}
		return resourceIndex;
	}
	public void setResourceIndex(ResourceIndex resourceIndex) {
		this.resourceIndex = resourceIndex;
	}
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
		shutdownDone = true;
	}
}
