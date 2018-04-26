package org.archive.cdxserver.processor;

import junit.framework.TestCase;

import org.archive.cdxserver.format.CDX11Format;
import org.archive.cdxserver.format.CDXFormat;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Test for FieldSelectorProcessor
 *
 */
public class FieldSelectorProcessorTest extends TestCase {

	final static CDXFormat cdxFormat = new CDX11Format();

	final static String CDXLINE = "com,exmaple)/ 20100101232959 http://example.com/ text/html "
			+ "200 SHASHASHASHA - - 200 10000 a/b.warc.gz";

	class CdxOut implements BaseProcessor {
		FieldSplitFormat expectedFormat;

		public CdxOut(FieldSplitFormat expectedFormat) {
			this.expectedFormat = expectedFormat;
		}

		public CdxOut(String expectedFormat) {
			this(new FieldSplitFormat(expectedFormat));
		}

		@Override
		public void begin() {
		}

		@Override
		public void trackLine(CDXLine line) {
		}

		@Override
		public void writeResumeKey(String resumeKey) {
		}

		@Override
		public void end() {
		}

		@Override
		public CDXFormat modifyOutputFormat(CDXFormat format) {
			return format;
		}

		@Override
		public int writeLine(CDXLine line) {
			assertTrue(line.getNumFields() == expectedFormat.getLength());
			for (int i = 0; i < line.getNumFields(); i++) {
				int idx = line.getFieldIndex(expectedFormat.getName(i));
				assertEquals(i, idx);
			}
			return 1;
		}

	}

	class AnswerWriteLine implements IAnswer<Integer> {
		FieldSplitFormat expectedFormat;

		public AnswerWriteLine(FieldSplitFormat expectedFormat) {
			this.expectedFormat = expectedFormat;
		}

		public AnswerWriteLine(String expectedFormat) {
			this(new FieldSplitFormat(expectedFormat));
		}

		@Override
		public Integer answer() throws Throwable {
			CDXLine line = (CDXLine)EasyMock.getCurrentArguments()[0];
			assertEquals(expectedFormat.getLength(), line.getNumFields());
			// check line has the same fields as expectedFormat, in the same
			// order.
			for (int i = 0; i < line.getNumFields(); i++) {
				int idx = line.getFieldIndex(expectedFormat.getName(i));
				assertEquals(expectedFormat.getName(i), i, idx);
			}
			return 1;
		}
	}

	BaseProcessor writer;

	protected void setUp() throws Exception {
		super.setUp();
		writer = EasyMock.createNiceMock(BaseProcessor.class);
		EasyMock.expect(writer.modifyOutputFormat(cdxFormat)).andReturn(
			cdxFormat);
		// each test should set up writeLine().
	}

	protected void expectWriteLine(String expectedFormat) {
		EasyMock.expect(writer.writeLine(EasyMock.<CDXLine>notNull()))
			.andAnswer(new AnswerWriteLine(expectedFormat));
	}

	protected void runWriteLineTest(FieldSplitFormat outputFields,
			FieldSplitFormat allowedFields, String expectedFormat) {
		expectWriteLine(expectedFormat);

		EasyMock.replay(writer);

		FieldSelectorProcessor selector = new FieldSelectorProcessor(writer,
			outputFields, allowedFields);
		CDXFormat parseFormat = selector.modifyOutputFormat(cdxFormat);
		CDXLine line = parseFormat.createCDXLine(CDXLINE);
		selector.writeLine(line);
	}

	public void testNullOutputNullAllowed() {
		runWriteLineTest(
			null,
			null,
			"urlkey,timestamp,original,mimetype,statuscode,digest,redirect,robotflags,length,offset,filename");
	}

	public void testNullOputput() {
		runWriteLineTest(
			null,
			new FieldSplitFormat("urlkey,timestamp,original,mimetype,statuscode,digest,length"),
			"urlkey,timestamp,original,mimetype,statuscode,digest,length"
			);
	}
	
	public void testNullAllowed() {
		runWriteLineTest(
			new FieldSplitFormat("original,timestamp,mimetype,statuscode,offset,filename"),
			null,
			"original,timestamp,mimetype,statuscode,offset,filename");
	}
	
	public void testBothNotNull() {
		runWriteLineTest(
			new FieldSplitFormat("original,timestamp,mimetype,statuscode,offset,filename"),
			new FieldSplitFormat("urlkey,timestamp,original,mimetype,statuscode,digest,length"),
			"original,timestamp,mimetype,statuscode");
	}
	
	public void testUndefinedFieldInAllowed() {
		runWriteLineTest(
			new FieldSplitFormat("original,timestamp,mimetype,statuscode,offset,filename"),
			new FieldSplitFormat("urlkey,timestamp,original,mimetype,statuscode,digest,length,groupcount"),
			"original,timestamp,mimetype,statuscode");
		
	}
}
