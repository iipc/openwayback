package org.archive.wayback.resourcestore.jwat;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.core.Resource;
import org.archive.wayback.resourcestore.resourcefile.WarcResourceTest;

/**
 * tests for {@link JWATResource}.
 * <p>Borrowing test methods from {@link WarcResourceTest}.</p>
 */
public class JWATResourceTest extends WarcResourceTest {

	protected void setUp() throws Exception {
	}

	@Override
	protected Resource createResource(WARCRecordInfo recinfo)
			throws Exception {
		return JWATResource.getResource(TestWARCReader.buildRecordContent(recinfo), 0);
	}
}
