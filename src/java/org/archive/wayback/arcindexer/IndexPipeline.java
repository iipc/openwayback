/* IndexPipeline
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

package org.archive.wayback.arcindexer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.core.ResourceResults;

import com.sun.org.apache.xml.internal.utils.StringToStringTable;

/**
 * Implements updating of a BDBResourceIndex using several directories with data
 * files or flag files.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class IndexPipeline {
	private File arcDir = null;

	private File mergeDir = null;

	private File queuedDir = null;

	private File toBeIndexedDir = null;

	private File indexingDir = null;

	private ArcIndexer indexer = null;

	/**
	 * Constructor
	 */
	public IndexPipeline() {
		super();
	}

	private void ensureDir(File dir) throws IOException {
		if (!dir.isDirectory() && !dir.mkdir()) {
			throw new IOException("FAILED to create " + dir.getAbsolutePath());
		}
	}

	/**
	 * Initialize this object from several path arguments.
	 * 
	 * @param arcDir
	 * @param mergeDir
	 * @param workDir
	 * @throws IOException
	 */
	public void init(final String arcDir, final String mergeDir,
			final String workDir) throws IOException {
		this.arcDir = new File(arcDir);
		this.mergeDir = new File(mergeDir);
		this.queuedDir = new File(workDir + "/queued");
		this.toBeIndexedDir = new File(workDir + "/to-be-indexed");
		this.indexingDir = new File(workDir + "/indexing");
		ensureDir(new File(workDir));
		ensureDir(this.queuedDir);
		ensureDir(this.toBeIndexedDir);
		ensureDir(this.indexingDir);
		indexer = new ArcIndexer();
	}

	private StringToStringTable dirToSTST(File dir) {
		StringToStringTable hash = new StringToStringTable();
		String entries[] = dir.list();
		for (int i = 0; i < entries.length; i++) {
			hash.put(entries[i], "i");
		}
		return hash;
	}

	private StringToStringTable getQueuedFiles() {
		return dirToSTST(this.queuedDir);
	}

	private ArrayList getNewArcs() {
		StringToStringTable queued = getQueuedFiles();
		ArrayList newArcs = new ArrayList();

		String arcs[] = this.arcDir.list();
		for (int i = 0; i < arcs.length; i++) {
			if (!queued.contains(arcs[i])) {
				newArcs.add(arcs[i]);
			}
		}
		return newArcs;
	}

	private void queueArc(final String newArc) throws IOException {
		File newQueuedFile = new File(this.queuedDir.getAbsolutePath() + "/"
				+ newArc);
		File newToBeIndexedFile = new File(this.toBeIndexedDir
				.getAbsolutePath()
				+ "/" + newArc);
		newToBeIndexedFile.createNewFile();
		newQueuedFile.createNewFile();
	}

	/**
	 * Find all new ARC files, and queue them for indexing.
	 * 
	 * @throws IOException
	 */
	public void queueNewArcs() throws IOException {
		ArrayList newArcs = getNewArcs();
		if (!newArcs.isEmpty()) {
			Iterator itr = newArcs.iterator();
			while (itr.hasNext()) {
				String newArc = (String) itr.next();
				queueArc(newArc);
			}
		}
	}

	/**
	 * Index any ARC files queued for indexing, queueing the resulting CDX files
	 * for merging with the BDBResourceIndex.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void indexArcs() throws MalformedURLException, IOException {
		queueNewArcs();
		String toBeIndexed[] = this.toBeIndexedDir.list();
		for (int i = 0; i < toBeIndexed.length; i++) {

			String base = toBeIndexed[i];

			File arcFile = new File(this.arcDir.getAbsolutePath().concat(
					"/" + base));
			File tmpFile = new File(this.indexingDir.getAbsolutePath().concat(
					"/" + base));
			File flagFile = new File(this.toBeIndexedDir.getAbsolutePath()
					.concat("/" + base));
			File finalFile = new File(this.mergeDir.getAbsolutePath().concat(
					"/" + base));

			ResourceResults res = indexer.indexArc(arcFile.getAbsolutePath());
			indexer.serializeResults(res, tmpFile.getAbsolutePath());
			if (!tmpFile.renameTo(finalFile)) {
				throw new IOException("Unable to move "
						+ tmpFile.getAbsolutePath() + " to "
						+ finalFile.getAbsolutePath());
			}
			if (!flagFile.delete()) {
				throw new IOException("Unable to delete "
						+ flagFile.getAbsolutePath());
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return Returns the mergeDir.
	 */
	public File getMergeDir() {
		return mergeDir;
	}

}
