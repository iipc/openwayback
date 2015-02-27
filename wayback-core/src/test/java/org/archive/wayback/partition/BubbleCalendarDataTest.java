package org.archive.wayback.partition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.resourceindex.cdx.CDXFormatToSearchResultAdapter;
import org.archive.wayback.resourceindex.cdx.format.CDXFlexFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;
import org.archive.wayback.util.partition.Partition;

public class BubbleCalendarDataTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected CaptureSearchResults getSampleIndex() throws IOException, CDXFormatException {
		CaptureSearchResults results = new CaptureSearchResults();
		// XXX CDXFormat has a constant for "n", but does #getField() does not recognize it.
		// It recognizes "S" instead. Field names doesn't matter anyway..
		CDXFormat format = new CDXFlexFormat(" CDX A b a m s k r M V S g");
		CDXFormatToSearchResultAdapter parser = new CDXFormatToSearchResultAdapter(format);
		InputStream is = getClass().getResourceAsStream("test-cdx.txt");
		assertNotNull("classpath resource not found", is);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			CaptureSearchResult csr = parser.adapt(line);
			results.addSearchResult(csr);
		}
		return results;
	}

	public void testGetCaptureCalendar() throws Exception {
		WaybackRequest wbRequest = WaybackRequest.createReplayRequest(
			"http://archive.org", "20130801000000", "20130101000000", "20131231115959");
		UIResults uiResults = new UIResults(wbRequest, null, getSampleIndex());
		BubbleCalendarData data = new BubbleCalendarData(uiResults);

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));

		data.setMonth(0);
		List<List<Partition<CaptureSearchResult>>> cal = data.getCaptureCalendar();
		assertEquals("Jan 2013 has five week rows", 5, cal.size());

		// Jan 1, 2013 is Tuesday.
		List<Partition<CaptureSearchResult>> w1 = cal.get(0);
		assertNull(w1.get(0)); // Sunday
		assertNull(w1.get(1)); // Monday
		Partition<CaptureSearchResult> d1 = w1.get(2);
		assertEquals("20130101000000", df.format(d1.getStart()));

		List<Partition<CaptureSearchResult>> w5 = cal.get(4);
		assertNull(w5.get(6)); // Saturday
		assertNull(w5.get(5)); // Friday
		Partition<CaptureSearchResult> d31 = w5.get(4);
		assertEquals("20130131000000", df.format(d31.getStart()));
	}
}
