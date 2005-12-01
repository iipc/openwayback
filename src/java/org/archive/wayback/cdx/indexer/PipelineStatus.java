/* PipelineStatus
 *
 * Created on Oct 20, 2005
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the wayback (crawler.archive.org).
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

package org.archive.wayback.cdx.indexer;

/**
 * Data bag for handing off status of Pipeline to PipelineStatus.jsp.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class PipelineStatus {

	/**
	 * number of ARCs waiting to be indexed
	 */
	private String numQueuedForIndex;

	/**
	 * number of serialized CDX files waiting to be merged into the index
	 */
	private String numQueuedForMerge;

	/**
	 * Constructor
	 */
	public PipelineStatus() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return Returns the numQueuedForIndex.
	 */
	public String getNumQueuedForIndex() {
		return numQueuedForIndex;
	}

	/**
	 * @param numQueuedForIndex
	 *            The numQueuedForIndex to set.
	 */
	public void setNumQueuedForIndex(String numQueuedForIndex) {
		this.numQueuedForIndex = numQueuedForIndex;
	}

	/**
	 * @return Returns the numQueuedForMerge.
	 */
	public String getNumQueuedForMerge() {
		return numQueuedForMerge;
	}

	/**
	 * @param numQueuedForMerge
	 *            The numQueuedForMerge to set.
	 */
	public void setNumQueuedForMerge(String numQueuedForMerge) {
		this.numQueuedForMerge = numQueuedForMerge;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
