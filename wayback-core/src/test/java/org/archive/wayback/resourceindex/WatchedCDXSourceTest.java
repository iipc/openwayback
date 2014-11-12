package org.archive.wayback.resourceindex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import junit.framework.TestCase;

public class WatchedCDXSourceTest extends TestCase {
    WatchedCDXSource cdxSource;
    Path cdxDir;

    @Override
    protected void setUp() {
	cdxSource = new WatchedCDXSource();

	String tmp = System.getProperty("java.io.tmpdir");
	cdxDir = Paths.get(tmp + File.separator
		+ Long.toString(System.nanoTime()));
	try {
	    cdxDir = Files.createDirectory(cdxDir);
	} catch (IOException e) {
	    fail("Error creating CDX directory: " + e.getMessage());
	}
    }

    private void addCdx() {
	try {
	    Files.createTempFile(cdxDir, Long.toString(System.nanoTime()),
		    ".cdx");
	} catch (IOException e) {
	    fail("Error creating CDX file: " + e.getMessage());
	}
    }

    @SuppressWarnings("unused")
    private int countCDXs() {
	int count = 0;
	try {
	    for (Path p : Files.newDirectoryStream(cdxDir)) {
		count++;
	    }
	} catch (IOException e) {
	    fail("Error reading CDX directory: " + e.getMessage());
	}
	return count;
    }

    /**
     * Test method for
     * {@link org.archive.wayback.resourceindex.WatchedCDXSource#addExistingSources(java.nio.file.Path)}
     * .
     * 
     * @throws IOException
     * 
     */
    public void testExistingSources() throws IOException {
	addCdx();
	cdxSource.setPath(cdxDir.toString());
	assertEquals(1, cdxSource.sources.size());
    }

    /**
     * Test method for
     * {@link org.archive.wayback.resourceindex.WatchedCDXSource.WatcherThread)}
     * .
     * 
     * @throws InterruptedException
     * 
     */
    public void testAddSources() throws InterruptedException {
	cdxSource.setPath(cdxDir.toString());
	for (int i = 0; i < 10; i++) {
	    addCdx();
	    Thread.sleep(100L);
	    assertEquals(countCDXs(), cdxSource.getSources().size());
	}
    }

    /**
     * Test method for
     * {@link org.archive.wayback.resourceindex.WatchedCDXSource.WatcherThread)}
     * .
     * @throws InterruptedException 
     * 
     */
    public void testRemoveSources() throws InterruptedException {
	cdxSource.setPath(cdxDir.toString());
	try {
	    for (Path p : Files.newDirectoryStream(cdxDir)) {
		Files.delete(p);
		Thread.sleep(100L);
		assertEquals(countCDXs(), cdxSource.sources.size());
	    }
	} catch (IOException e) {
	    fail("Error deleting CDX file: " + e.getMessage());
	}
    }

    @Override
    protected void tearDown() {
	try {
	    for (Path p : Files.newDirectoryStream(cdxDir)) {
		Files.delete(p);
	    }
	    Files.delete(cdxDir);
	} catch (IOException e) {
	    fail("Error deleting CDX directory: " + e.getMessage());
	}
    }
}