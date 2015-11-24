package org.archive.wayback.resourcestore.resourcefile;

import junit.framework.TestCase;

import java.io.File;

/**
 * Tests for RegexFilenameFilter.
 *
 * @author mbitzl
 */
public class RegexFilenameFilterTest extends TestCase {

    private File directory;
    private RegexFilenameFilter filter;

    public void setUp() {
        directory = null;
        filter = new RegexFilenameFilter();
    }

    public void testAcceptsFileMatchingAcceptRegex() {
        filter.setAcceptRegex("^.*\\.warc\\.gz");
        assertTrue(filter.accept(directory, "beautiful.warc.gz"));
    }

    public void testRejectsFileMatchingAcceptRegex() {
        filter.setAcceptRegex("^.*\\.warc\\.gz");
        assertFalse(filter.accept(directory, "beautiful.txt"));
    }

    public void testRejectsFileWithoutAnyAcceptRegex() {
        assertFalse(filter.accept(directory, "beautiful.warc.gz"));
    }

    public void testRejectsFileMatchingRejectRegex() {
        filter.setAcceptRegex("^.*\\.warc\\.gz");
        filter.setRejectRegex(".*temp.*");
        assertFalse(filter.accept(directory, "beautiful.temp.warc.gz"));
    }

    public void testAcceptsFileNotMatchingRejectRegex() {
        filter.setAcceptRegex("^.*\\.warc\\.gz");
        filter.setRejectRegex(".*temp.*");
        assertTrue(filter.accept(directory, "beautiful.warc.gz"));
    }

    public void testGetAcceptRegexReturnsRegex() {
        String regex = "^.*\\.warc\\.gz";
        filter.setAcceptRegex(regex);
        assertEquals(regex, filter.getAcceptRegex());
    }

    public void testGetRejectRegexReturnsRegex() {
        String regex = "^.*\\.warc\\.gz";
        filter.setRejectRegex(regex);
        assertEquals(regex, filter.getRejectRegex());
    }

}
