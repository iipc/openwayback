package org.archive.wayback.resourcestore.resourcefile;

import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: csr@statsbiblioteket.dk (Colin Rosenthal)
 *
 */
public class ResourceFactoryTest extends TestCase {

    private String testfile="wayback-core/src/test/java/org/archive/wayback/resourcestore/testdata/testdata1.arc";
    private long offset = 7728L;

    /**
     * Test reading uncompressed arcfile for issue
     * https://github.com/iipc/openwayback/issues/101
     * @throws Exception
     */
    public void testGetResource() throws Exception {
        ArcResource arcResource = (ArcResource) ResourceFactory.getResource(testfile, offset);
        final long position = arcResource.getArcRecord().getPosition();
        final long recordLength = arcResource.getRecordLength();
        assertTrue("Position " + position + " is after end of record " + recordLength, position < recordLength);
    }
}
