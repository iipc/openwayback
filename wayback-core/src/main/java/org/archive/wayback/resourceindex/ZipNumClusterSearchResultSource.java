package org.archive.wayback.resourceindex;

import java.io.IOException;

import org.archive.format.gzip.zipnum.ZipNumCluster;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.cdx.format.CDXFlexFormat;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;

/**
 * SearchResultSource wrapper for new implementation of CDX input source, including ZipNumCluster and CDX File
 * from archive-commons
 * @author ilya
 *
 */

public class ZipNumClusterSearchResultSource implements SearchResultSource, Adapter<String,CaptureSearchResult> {
		
	protected ZipNumCluster cluster;
	protected ZipNumParams params = null;
	
	protected ZipNumParams oneBlockParams;
		
	public void init() throws IOException
	{
		//this.cluster = new ZipNumCluster(clusterUri, summaryFile, blockLoader);
		
		oneBlockParams = new ZipNumParams();
		oneBlockParams.setMaxBlocks(1);
	}
	
	@Override
	public CloseableIterator<CaptureSearchResult> getPrefixIterator(
			String urlkey) throws ResourceIndexNotAvailableException {
		
		try {			
			CloseableIterator<String> cdxIter = null;
			
			String prefix = urlkey;
			
			int space = prefix.indexOf(' ');
			
			// One-block query
			if (space >= 0) {
				prefix = prefix.substring(0, space);
				
				cdxIter = cluster.getCDXIterator(urlkey, prefix, true, oneBlockParams);
			// Exact Match
			} else if (!prefix.endsWith("*\t")) {
				cdxIter = cluster.getCDXIterator(urlkey, prefix, true, params);
			// Prefix Match
			} else {
				cdxIter = cluster.getCDXIterator(urlkey, prefix.substring(0, prefix.length() - 2), false, params);
			}
			
			return new AdaptedIterator<String,CaptureSearchResult>(cdxIter, this);
			 
			
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
		c.close();
	}

	@Override
	public void shutdown() throws IOException {
		// TODO Auto-generated method stub

	}

	public ZipNumCluster getCluster() {
		return cluster;
	}

	public void setCluster(ZipNumCluster cluster) {
		this.cluster = cluster;
	}

	public ZipNumParams getParams() {
		return params;
	}

	public void setParams(ZipNumParams params) {
		this.params = params;
	}
	
	public CaptureSearchResult adapt(String line) {
		return CDXFlexFormat.parseCDXLineFlex(line);
	}	
}
