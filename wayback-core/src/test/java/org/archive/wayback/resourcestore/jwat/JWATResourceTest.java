/**
 *
 */
package org.archive.wayback.resourcestore.jwat;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.core.Resource;

import junit.framework.TestCase;

/**
 * test for {@link JWATResource}.
 */
public class JWATResourceTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	// TODO: more tests

	public void testUrlAgnosticRevisitRecord() throws Exception {
		final String ctype = "text/html";
		WARCRecordInfo recinfo = TestWARCRecordInfo
			.createUrlAgnosticRevisitHttpResponse(ctype, 1345);
		Resource res = JWATResource.getResource(
			TestWARCReader.buildRecordContent(recinfo), 0);

		assertEquals("http://example.com/", res.getRefersToTargetURI());
		assertEquals("20140101101010", res.getRefersToDate());
	}
}
