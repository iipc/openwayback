package org.archive.wayback.resourceindex;

import java.io.IOException;

import org.archive.format.cdx.CDXInputSource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.cdx.CDXLineToSearchResultAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.CloseableIterator;

/**
 * SearchResultSource wrapper for new implementation of CDX input source, including ZipNumCluster and CDX File
 * from archive-commons
 * @author ilya
 *
 */

public class CDXSearchResultSource implements SearchResultSource {

	protected CDXInputSource input;
	
	public CDXSearchResultSource(CDXInputSource input)
	{
		this.input = input;
	}
	
	@Override
	public CloseableIterator<CaptureSearchResult> getPrefixIterator(
			String prefix) throws ResourceIndexNotAvailableException {
		
		return getCaptureSearchResultIter(prefix, false);
	}
	
	@Override
	public CloseableIterator<CaptureSearchResult> getExactIterator(String key)
			throws ResourceIndexNotAvailableException {
		// TODO Auto-generated method stub
		return getCaptureSearchResultIter(key, true);
	}

	protected CloseableIterator<CaptureSearchResult> getCaptureSearchResultIter(
			String key, boolean exact) throws ResourceIndexNotAvailableException {
		try {
			
			return new AdaptedIterator<String,CaptureSearchResult>
				(input.getLineIterator(key, exact),	new CDXLineToSearchResultAdapter());
			
		} catch (IOException e) {
			throw new ResourceIndexNotAvailableException(e.toString());
		}
	}

	@Override
	public CloseableIterator<CaptureSearchResult> getPrefixReverseIterator(
			String prefix) throws ResourceIndexNotAvailableException {
		
		throw new ResourceIndexNotAvailableException("Unsupported");
	}

	@Override
	public void cleanup(CloseableIterator<CaptureSearchResult> c)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() throws IOException {
		// TODO Auto-generated method stub

	}
}
