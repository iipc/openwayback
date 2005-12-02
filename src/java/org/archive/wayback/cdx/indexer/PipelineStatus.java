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
	 * name of this Wayback Installation
	 */
	private String installationName;

	/**
	 * directory that holds the local Database (BDBJE)
	 */
	private String databasePath;

	/**
	 * name of the local Database (BDBJE)
	 */
	private String databaseName;

	/**
	 * maximum size (in bytes) of BDBJE files
	 */
	private String dbFileSize;

	/**
	 * directory(local) where ARC files are located 
	 */
	private String arcPath;

	/**
	 * directory(local) where the index Pipeline stores state 
	 */
	private String pipelineWorkPath;

	/**
	 * boolean flag: is the pipeline thread configured to run? 
	 */
	private boolean pipelineActive;
	
	/**
	 * number of ARCs waiting to be indexed
	 */
	private String numQueuedForIndex;

	/**
	 * number of serialized CDX files waiting to be merged into the index
	 */
	private String numQueuedForMerge;

	/**
	 * number of ARC files already added to the index
	 */
	private String numIndexed;

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

	/**
	 * @return Returns the arcPath.
	 */
	public String getArcPath() {
		return arcPath;
	}

	/**
	 * @param arcPath The arcPath to set.
	 */
	public void setArcPath(String arcPath) {
		this.arcPath = arcPath;
	}

	/**
	 * @return Returns the databaseName.
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @param databaseName The databaseName to set.
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @return Returns the databasePath.
	 */
	public String getDatabasePath() {
		return databasePath;
	}

	/**
	 * @param databasePath The databasePath to set.
	 */
	public void setDatabasePath(String databasePath) {
		this.databasePath = databasePath;
	}

	/**
	 * @return Returns the installationName.
	 */
	public String getInstallationName() {
		return installationName;
	}

	/**
	 * @param installationName The installationName to set.
	 */
	public void setInstallationName(String installationName) {
		this.installationName = installationName;
	}

	/**
	 * @return Returns the dbFileSize.
	 */
	public String getDbFileSize() {
		return dbFileSize;
	}

	/**
	 * @param dbFileSize The dbFileSize to set.
	 */
	public void setDbFileSize(String dbFileSize) {
		this.dbFileSize = dbFileSize;
	}

	/**
	 * @return Returns the pipelineActive.
	 */
	public boolean isPipelineActive() {
		return pipelineActive;
	}

	/**
	 * @param pipelineActive The pipelineActive to set.
	 */
	public void setPipelineActive(boolean pipelineActive) {
		this.pipelineActive = pipelineActive;
	}

	/**
	 * @return Returns the pipelineWorkPath.
	 */
	public String getPipelineWorkPath() {
		return pipelineWorkPath;
	}

	/**
	 * @param pipelineWorkPath The pipelineWorkPath to set.
	 */
	public void setPipelineWorkPath(String pipelineWorkPath) {
		this.pipelineWorkPath = pipelineWorkPath;
	}

	/**
	 * @return Returns the numIndexed.
	 */
	public String getNumIndexed() {
		return numIndexed;
	}

	/**
	 * @param numIndexed The numIndexed to set.
	 */
	public void setNumIndexed(String numIndexed) {
		this.numIndexed = numIndexed;
	}

}
