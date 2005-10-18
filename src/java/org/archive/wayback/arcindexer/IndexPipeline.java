package org.archive.wayback.arcindexer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.core.ResourceResults;

import com.sun.org.apache.xml.internal.utils.StringToStringTable;

public class IndexPipeline {
	private File arcDir = null;

	public File mergeDir = null;

	private File queuedDir = null;

	private File toBeIndexedDir = null;

	private File indexingDir = null;

	private ArcIndexer indexer = null;

	public IndexPipeline() {
		super();
		// TODO Auto-generated constructor stub
	}

	private void ensureDir(File dir) throws IOException {
		if (!dir.isDirectory() && !dir.mkdir()) {
			throw new IOException("FAILED to create " + dir.getAbsolutePath());
		}
	}

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
		// TODO Auto-generated method stub

	}

}
