/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual
 *  contributors.
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.cdxserver.processor;

import junit.framework.TestCase;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;
import org.easymock.EasyMock;

/**
 * Test for {@link DupeTimestampBestStatusFilter} and
 * {@link DupeTimestampLastBestStatusFilter}.
 *
 */
public class DupeTimestampBestStatusFilterTest extends TestCase {

	DupeTimestampBestStatusFilter cut;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	static class TestCDXLine extends CDXLine {
		static final FieldSplitFormat format = new FieldSplitFormat("timestamp,statuscode,robotflags,filename");
		public TestCDXLine(String timestamp, int status) {
			this(timestamp, status, "-", "NA");
		}
		public TestCDXLine(String timestamp, int status, String filename) {
			this(timestamp, status, "-", filename);
		}
		public TestCDXLine(String timestamp, int status, String robotflags, String filename) {
			super(timestamp + " " + status + " " + robotflags + " " + filename, format);
		}
	}

	protected final BaseProcessor setupOutputMock(CDXLine[] cdxlines, int... expected) {
		BaseProcessor output = EasyMock.createStrictMock(BaseProcessor.class);
		output.begin();
		EasyMock.expectLastCall().once();
		for (int i : expected) {
			EasyMock.expect(output.writeLine(cdxlines[i])).andReturn(1);
		}
		output.end();
		EasyMock.expectLastCall().once();
		return output;
	}
	protected final void process(CDXLine[] cdxlines) {
		// simplified sequence - actual code also calls trackLine()
		// and modifyOutputFormat, which are irrelevant to the class
		// under test.
		cut.begin();
		for (CDXLine l : cdxlines) {
			cut.writeLine(l);
		}
		cut.end();
	}

	static final CDXLine[] TEST_CASE_1 = {
		new TestCDXLine("20140101022436", 200),
		new TestCDXLine("20140101033526", 200),
		new TestCDXLine("20140101033819", 200),
		new TestCDXLine("20140101042648", 200)
	};

	public void testBasic_dedupLength10() {
		BaseProcessor output = setupOutputMock(TEST_CASE_1, 0, 1, 3);

		cut = new DupeTimestampBestStatusFilter(output, 10, null);

		EasyMock.replay(output);

		process(TEST_CASE_1);

		EasyMock.verify();
	}

	public void testBasic_dedupLength8() {
		BaseProcessor output = setupOutputMock(TEST_CASE_1, 0);

		cut = new DupeTimestampBestStatusFilter(output, 8, null);

		EasyMock.replay(output);

		process(TEST_CASE_1);

		EasyMock.verify();
	}

	/**
	 * dedupLength > 14 is assumed 0 (i.e. no collapsing) and does not cause any
	 * trouble.
	 */
	public void testBasic_dedupLength16() {
		BaseProcessor output = setupOutputMock(TEST_CASE_1, 0, 1, 2, 3);

		cut = new DupeTimestampBestStatusFilter(output, 16, null);

		EasyMock.replay(output);

		process(TEST_CASE_1);

		EasyMock.verify();
	}

	static final CDXLine[] TEST_CASE_2 = {
		new TestCDXLine("20140902201508", 200, "A"),
		new TestCDXLine("20140903012025", 200, "A"),
		new TestCDXLine("20140903020020", 301, "A"),
		new TestCDXLine("20140903182258", 200, "L"),
		new TestCDXLine("20140903192521", 200, "A"),
		new TestCDXLine("20140903192732", 301, "L")
	};

	/**
	 * Picks the first CDX line with the best (smallest) {@code statuscode}
	 * within each group.
	 */
	public void testBasic_bestStatusCode() {
		BaseProcessor output = setupOutputMock(TEST_CASE_2, 0, 1);

		cut = new DupeTimestampBestStatusFilter(output, 8, null);

		EasyMock.replay(output);

		process(TEST_CASE_2);

		EasyMock.verify();
	}

	/**
	 * soft-blocked captures are passed-through (they are supposed to be
	 * removed later).
	 */
	public void testBlockedPassesThrough() {
		final CDXLine[] TEST_CASE = {
			new TestCDXLine("20140902201508", 200, "-", "A"),
			new TestCDXLine("20140903012025", 200, "X", "A"),
			new TestCDXLine("20140903020020", 200, "-", "A"),
			new TestCDXLine("20140903182258", 200, "-", "L"),
			new TestCDXLine("20140903192521", 200, "-", "A"),
			new TestCDXLine("20140903192732", 200, "-", "L")
		};
		// 20140903012025 capture is passed through. The next one
		// 20140903020020 is picked up by the filter for 20140903
		// period, and the rest is dropped.
		BaseProcessor output = setupOutputMock(TEST_CASE, 0, 1, 2);

		cut = new DupeTimestampBestStatusFilter(output, 8, null);

		EasyMock.replay(output);

		process(TEST_CASE);

		EasyMock.verify();
	}

	// DupeTimestampLastBestStatusFilter

	/**
	 * Picks the last CDX line with the best (smallest) {@code statuscode}
	 * within each group.
	 */
	public void testLastBestStatus() {
		BaseProcessor output = setupOutputMock(TEST_CASE_2, 0, 4);

		cut = new DupeTimestampLastBestStatusFilter(output, 8, null);

		EasyMock.replay(output);

		process(TEST_CASE_2);

		EasyMock.verify();
	}

	public void testLastBestStatus_withNoCollapse() {
		BaseProcessor output = setupOutputMock(TEST_CASE_2, 0, 3, 4, 5);

		cut = new DupeTimestampLastBestStatusFilter(output, 8, new String[] { "L" });

		EasyMock.replay(output);

		process(TEST_CASE_2);

		EasyMock.verify();
	}
}
