package org.archive.wayback.localbdbresourceindex;

import java.io.File;
import java.io.RandomAccessFile;

import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;

import com.sleepycat.je.DatabaseException;

public class BDBResourceIndexWriter {
	private BDBResourceIndex db = null;

	public BDBResourceIndexWriter() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void init(final String thePath, final String theDbName)
			throws Exception {
		db = new BDBResourceIndex(thePath, theDbName);
	}

	protected void init(BDBResourceIndex db) {
		this.db = db;
	}

	protected void shutdown() throws DatabaseException {
		db.shutdownDB();
	}

	public void importFile(String filePath) throws Exception {
		ResourceResults results = readFile(filePath);
		db.addResults(results);
	}

	private ResourceResults readFile(String filePath) throws Exception {
		File file = new File(filePath);
		RandomAccessFile raFile = new RandomAccessFile(file, "r");
		ResourceResults results = new ResourceResults();
		int lineNumber = 0;
		while (true) {
			String line = raFile.readLine();
			if (line == null) {
				break;
			}
			lineNumber++;
			if ((lineNumber == 1) && (line.contains(" CDX "))) {
				continue;
			}
			ResourceResult result = new ResourceResult();
			result.parseLine(line, lineNumber);

			results.addResourceResult(result);
		}
		return results;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BDBResourceIndexWriter idx = new BDBResourceIndexWriter();
			idx.init(args[0], args[1]);
			idx.importFile(args[2]);

			idx.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
