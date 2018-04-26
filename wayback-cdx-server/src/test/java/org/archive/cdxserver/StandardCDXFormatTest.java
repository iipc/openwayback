/**
 * 
 */
package org.archive.cdxserver;

import junit.framework.TestCase;

import org.archive.cdxserver.format.CDX11Format;
import org.archive.cdxserver.format.CDX9Format;
import org.archive.cdxserver.format.CDXFormat;
import org.archive.cdxserver.format.StandardCDXFormat;
import org.archive.format.cdx.CDXFieldConstants;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

/**
 * Test for StandardCDXFormatTest
 */
public class StandardCDXFormatTest extends TestCase implements CDXFieldConstants {

	static final StandardCDXFormat cdx11Format = new CDX11Format();
	static final StandardCDXFormat cdx9Format = new CDX9Format();
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testFields() {
		CDXLine line = cdx11Format.createCDXLine("com,example)/ 20100101232959 http://example.com " +
				"text/html 200 SHASHASHA - - 200 10000 a/b.warc.gz");
		
		int indexOffset = line.getFieldIndex(offset);
		assertEquals(9, indexOffset);
		
		String valueOffset = line.getField(indexOffset);
		assertEquals("10000", valueOffset);
		
		assertEquals("text/html", line.getMimeType());
		
		assertFalse(cdx11Format.isRevisit(line));
	}
	
	public void testCDX9() {
		assertEquals(9, cdx9Format.getFields().getLength());
		
		CDXLine line = cdx9Format.createCDXLine("com,example)/ 20100101232959 http://example.com " +
	"text/html 200 SHASHASHASHA - 10000 a/b.warc.gz");
		
		int indexOffset = line.getFieldIndex(offset);
		assertEquals(7, indexOffset);
		
		String valueOffset = line.getField(indexOffset);
		assertEquals("10000", valueOffset);
		
		assertEquals("text/html", line.getMimeType());
		
		assertFalse(cdx9Format.isRevisit(line));
	}
	
	public void testExtend() {
		final String FIELD1 = "field1", FIELD2 = "field2", FIELD3 = "field3";
		CDXFormat format = cdx11Format.extend(FIELD1, FIELD2, FIELD3);
		
		assertTrue(format instanceof StandardCDXFormat);
		
		FieldSplitFormat names = format.getFields();
		assertEquals(11 + 3, names.getLength());
		
		// index -> name mapping
		assertEquals(filename, names.getName(10));
		assertEquals(FIELD1, names.getName(11));
		assertEquals(FIELD3, names.getName(13));
		
		// name -> index mapping
		assertEquals(10, names.getFieldIndex(filename));
		assertEquals(11, names.getFieldIndex(FIELD1));
		assertEquals(13, names.getFieldIndex(FIELD3));
		
		// name -> index mapping at CDXFormat level
		assertEquals(11, format.getFieldIndex(FIELD1));
	}
	
	/**
	 * {@link StandardCDXFormat#extend(String...)} without arguments
	 * shall return the same object.
	 */
	public void testExtendNothing() {
		CDXFormat format = cdx11Format.extend();
		assertSame(cdx11Format, format);
	}

}
