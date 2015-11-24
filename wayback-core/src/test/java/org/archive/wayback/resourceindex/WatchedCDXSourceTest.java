package org.archive.wayback.resourceindex;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.logging.Logger;

import junit.framework.TestCase;

public class WatchedCDXSourceTest extends TestCase {
	private static final Logger LOGGER = Logger.getLogger(WatchedCDXSourceTest.class.getName());
	private long WAIT = 100L;
	private int NUM_TESTS = 10; 
	WatchedCDXSource cdxSource;
	Path cdxDir;
	int cdxCount;
	boolean isMac = false;

	public WatchedCDXSourceTest() {
		if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
			isMac = true;
			WAIT = 10500L;
			NUM_TESTS = 1;
		}
	}

	@Override
	protected void setUp() {
		cdxSource = new WatchedCDXSource();
		cdxCount = 0;

		String tmp = System.getProperty("java.io.tmpdir");
		cdxDir = Paths.get(tmp + File.separator + Long.toString(System.nanoTime()));
		try {
			cdxDir = Files.createDirectory(cdxDir);
		} catch (IOException e) {
			fail("Error creating CDX directory: " + e.getMessage());
		}
	}

	private Path addCdx(Path dir) {
		Path newPath = null;
		try {
			newPath = Files.createTempFile(dir, Long.toString(System.nanoTime()), ".cdx");
			cdxCount++;
			LOGGER.finest("CDXs: " + cdxCount);
		} catch (IOException e) {
			fail("Error creating CDX file: " + e.getMessage());
		}
		return newPath;
	}

	private Path addNonCdx(Path dir) {
		Path newPath = null;
		try {
			newPath = Files.createTempFile(dir, Long.toString(System.nanoTime()), ".tmp");
		} catch (IOException e) {
			fail("Error creating temp. file: " + e.getMessage());
		}
		return newPath;
	}

	protected Path addSubdirectory() {
		Path newDir = null;
		try {
			newDir = Files.createDirectory(Paths.get(cdxDir + File.separator + Long.toString(System.nanoTime())));
		} catch (IOException e) {
			fail("Error creating CDX file: " + e.getMessage());
		}
		return newDir;
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
		addCdx(cdxDir);
		cdxSource.setRecursive(false);
		cdxSource.setPath(cdxDir.toString());
		assertEquals(1, cdxSource.sources.size());
	}

	/**
	 * Test method for ENTRY_CREATE in
	 * {@link org.archive.wayback.resourceindex.WatchedCDXSource.WatcherThread)}
	 * .
	 * 
	 * @throws InterruptedException
	 * 
	 */
	public void testAddSources() throws InterruptedException {
		cdxSource.setRecursive(false);
		cdxSource.setPath(cdxDir.toString());
		for (int i = 0; i < NUM_TESTS; i++) {
			addCdx(cdxDir);
			Thread.sleep(WAIT);
			assertEquals(cdxCount, cdxSource.getSources().size());
		}
	}

	/**
	 * Test method for non-CDX ENTRY_CREATE in
	 * {@link org.archive.wayback.resourceindex.WatchedCDXSource.WatcherThread)}
	 * .
	 * 
	 * @throws InterruptedException
	 * 
	 */
	public void testAddNonCDXSources() throws InterruptedException {
		cdxSource.setRecursive(false);
		cdxSource.setPath(cdxDir.toString());
		addCdx(cdxDir);
		Thread.sleep(WAIT);
		addNonCdx(cdxDir);
		Thread.sleep(WAIT);
		assertEquals(1, cdxSource.getSources().size());
	}

	/**
	 * Test method for ENTRY_DELETE in
	 * {@link org.archive.wayback.resourceindex.WatchedCDXSource.WatcherThread)}
	 * .
	 * 
	 * @throws InterruptedException
	 * 
	 */
	public void testRemoveSources() throws InterruptedException {
		cdxSource.setRecursive(false);
		cdxSource.setPath(cdxDir.toString());
		ArrayList<Path> paths = new ArrayList<Path>();
		for (int i = 0; i < NUM_TESTS; i++) {
			paths.add(addCdx(cdxDir));
			Thread.sleep(WAIT);
			assertEquals(cdxCount, cdxSource.getSources().size());
		}
		try {
			for (Path p : paths) {
				Files.delete(p);
				cdxCount--;
				Thread.sleep(WAIT);
				assertEquals(cdxCount, cdxSource.sources.size());
			}
		} catch (IOException e) {
			fail("Error deleting CDX file: " + e.getMessage());
		}
	}

	/**
	 * Test method for recursive ENTRY_CREATE in
	 * {@link org.archive.wayback.resourceindex.WatchedCDXSource.WatcherThread)}
	 * .
	 * 
	 * @throws InterruptedException
	 * 
	 */
	public void testSubdirectoryAddSources() throws InterruptedException {
		cdxSource.setRecursive(true);
		cdxSource.setPath(cdxDir.toString());
		Path newDir = addSubdirectory();
		for (int i = 0; i < NUM_TESTS; i++) {
			addCdx(newDir);
			Thread.sleep(WAIT);
			assertEquals(cdxCount, cdxSource.getSources().size());
		}
	}

	/**
	 * Test method for non-recursive ENTRY_CREATE in
	 * {@link org.archive.wayback.resourceindex.WatchedCDXSource.WatcherThread)}
	 * .
	 * 
	 * @throws InterruptedException
	 * 
	 */
	public void testSubdirectoryAddSourcesNonRecursive() throws InterruptedException {
		cdxSource.setRecursive(false);
		cdxSource.setPath(cdxDir.toString());
		Path newDir = addSubdirectory();
		for (int i = 0; i < NUM_TESTS; i++) {
			addCdx(newDir);
			Thread.sleep(WAIT);
			assertEquals(0, cdxSource.getSources().size());
		}
	}

	@Override
	protected void tearDown() {
		try {
			Files.walkFileTree(cdxDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					try {
						Thread.sleep(WAIT);
					} catch (InterruptedException e) {
						fail("Error deleting CDX file: " + e.getMessage());
					}
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					try {
						Files.delete(dir);
						Thread.sleep(WAIT);
					} catch (IOException e) {
						fail("Error deleting CDX dir.: " + e.getMessage());
					} catch (InterruptedException e) {
						fail("Error deleting CDX dir.: " + e.getMessage());
					}
					return CONTINUE;
				}
			});
		} catch (IOException e) {
			fail("Error tearing down: " + e.getMessage());
		}
	}
}
