wayback Overview:
	org.archive.wayback
		includes primary interfaces for Wayback components
	org.archive.wayback.core
		includes implementations for internal classes used within wayback
		
	org.archive.wayback.exception
		includes skeleton for a few exceptions -- no real significance as yet...
	org.archive.wayback.servletglue
		includes 3 primary interfaces:
			1) RequestFilter:
				attempts to parse incoming HTTP requests, and forward to correct servlet.
			2) WBQueryUIServlet:
				teeny glue that calls handle() on QueryUI for query requests
			3) WBReplayUIServlet:
				teeny glue that calls handle() on ReplayUI for replay requests
	org.archive.wayback.arcindexer
		includes 2 classes:
			1) ArcIndexer:
				transforms ARC file into CDX file
			2) IndexPipeline:
				uses multiple directories to store "flag" files while updating the CDX-BDB
				currently it runs in a single thread which calls:
					1) queue new arcs (inspect ARC directory, filter those already in queuedDir
						create a flag file in toBeIndexed)
					2) index new arcs (for each flag file in queueDir, create a CDX in mergeDir, 
						remove flag file)
					3) merge new cdx files (for each CDX in mergeDir, insert into BDB, remove CDX)
		This could be split into several threads, one for each step, but is in a single thread now
		for simplicity. This implementation could be changed to use interfaces that worked in both the 
		local "standalone" version as well as with a distributed ResourceStore, ResourceIndex, and a 
		pool of ArcIndexers. Not a rev. 1 feature..

	org.archive.wayback.ippreplayui
		this is unused, but subclasses RawReplayUI to modify the document content before returning.
		For now, all it does is add a <DIV> at the very end of the page.
	
	org.archive.wayback.localdbdresourceindex
		includes 3 classes:
			1) BDBResourceIndex:
				a very thin RersourceResults-specific wrapper around the BDB-JE library.
			2) LocalBDBResourceIndex:
				actual ResourceIndex implementation, transforms query() into one of the specific BDB 
				queries.
			3) BDBResourceWriter:
				super small module: importFile(CDXFile)

	org.archive.wayback.localresourcestore
		single class, transforms ARCLocation into a Resource with an ARCRecord (via ARCReader)

	org.archive.wayback.rawreplayui
		basic implementation of ReplayUI:
			parses Archival Urls
			transforms ResourceResults into ReplayUI Urls
			provides HTTP access to individual documents, including redirects to closest versions.
			no markup of resources
	
	org.archive.wayback.simplequeryui
		basic QueryUI implementation:
			trivial error display
			trivial UrlQuery and UrlPrefixQuery result HTML pages
			uses UIResults class to marshal result data to JSPs which actually draw the HTML
			the JSPs use header + footer templates for page consistancy, and are very basic.


WaybackRequest:
	resultsPerPage
	pageNum
	Properties:
		startdate = all results must be AFTER this date
		enddate   = all results must be BEFORE this date
		exactdate = return results matching EXACTLY this date
		type      = "replay" | "urlquery" | "urlprefixquery" | "text"
		url       = all results must either exactly match this, or begin with
					this, depending on type
	
SearchResults:
	CDX:
		url
		capturedate
		arcfile
		compressedoffset
		originalhost
		mimetype
		httpresponsecode
		md5fragment
		redirecturl
		compressedoffset
		arcfilename