package org.archive.wayback.arcindexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;

import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.net.UURI;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.Timestamp;
import org.apache.commons.httpclient.Header;

public class ArcIndexer {
	private final static String LOCATION_HTTP_HEADER = "Location";

	public ArcIndexer() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ResourceResults indexArc(final String arcPath) throws IOException {
		ResourceResults results = new ResourceResults();
		File arc = new File(arcPath);
		ARCReader arcReader = ARCReaderFactory.get(arc);
		arcReader.setParseHttpHeaders(true);
		// doh. this does not generate quite the columns we need:
		//arcReader.createCDXIndexFile(arcPath);
		Iterator itr = arcReader.iterator();
		while (itr.hasNext()) {
			ARCRecord rec = (ARCRecord) itr.next();
			ResourceResult result;
			try {
				result = arcRecordToResourceResult(rec, arc);
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			results.addResourceResult(result);
		}
		return results;
	}

	private ResourceResult arcRecordToResourceResult(final ARCRecord rec,
			File arc) throws NullPointerException, IOException, ParseException {
		rec.close();
		ARCRecordMetaData meta = rec.getMetaData();

		ResourceResult result = new ResourceResult();
		result.setArcFileName(arc.getName());
		result.setCompressedOffset(meta.getOffset());

		String statusCode = (meta.getStatusCode() == null) ? "-" : meta
				.getStatusCode();
		result.setHttpResponseCode(statusCode);

		result.setMd5Fragment(meta.getDigest());
		result.setMimeType(meta.getMimetype());
		UURI uri = new UURI(meta.getUrl(), false);
		result.setOrigHost(uri.getHost());

		String redirectUrl = "-";
		Header[] headers = rec.getHttpHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				if (headers[i].getName().equals(LOCATION_HTTP_HEADER)) {
					redirectUrl = headers[i].getValue();
					break;
				}
			}
		}
		result.setRedirectUrl(redirectUrl);
		result.setTimeStamp(Timestamp.parseBefore(meta.getDate()));
		UURI uriCap = new UURI(meta.getUrl(), false);
		String searchHost = uriCap.getHostBasename();
		String searchPath = uriCap.getEscapedPathQuery();

		String indexUrl = searchHost + searchPath;
		result.setUrl(indexUrl);

		return result;
	}

	public void serializeResults(final ResourceResults results,
			final String cdxPath) throws IOException {
		Iterator itr = results.iterator();
		File cdx = new File(cdxPath);
		FileOutputStream output = new FileOutputStream(cdx);
		output.write((ResourceResult.getCDXHeaderString() + "\n").getBytes());
		while (itr.hasNext()) {
			ResourceResult result = (ResourceResult) itr.next();
			output.write((result.toString() + "\n").getBytes());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArcIndexer indexer = new ArcIndexer();
		String arc = args[0];
		String cdx = args[1];
		try {
			ResourceResults results = indexer.indexArc(arc);
			indexer.serializeResults(results, cdx);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
