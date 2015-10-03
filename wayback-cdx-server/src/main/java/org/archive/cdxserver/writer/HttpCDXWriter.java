package org.archive.cdxserver.writer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.archive.format.cdx.CDXLine;
import org.archive.util.io.RuntimeIOException;

public abstract class HttpCDXWriter extends CDXWriter {

	public final static String X_NUM_PAGES = "X-CDX-Num-Pages";
	public final static String X_MAX_LINES = "X-CDX-Max-Lines";
	public final static String X_CLUSTER_URI = "X-CDX-Cluster-Uri";
	
	public final static String RUNTIME_ERROR_HEADER = "X-Archive-Wayback-Runtime-Error";
	
	protected HttpServletResponse response;
	protected PrintWriter writer;

	protected boolean includeBlockedCaptures = false;
	
	public HttpCDXWriter(HttpServletResponse response, boolean gzip) throws IOException {
	    this.response = response;
	    
	    if (gzip) {
	    	this.writer = getGzipWriter(response);
	    } else {
	    	this.writer = response.getWriter();
	    }
    }
	
	@Override
	public void close()
	{
		writer.flush();
		writer.close();
	}
	
	@Override
	public boolean isAborted()
	{
		return writer.checkError();
	}

	@Override
    public void printError(String msg) {
	    response.setStatus(400);
	    writer.println(msg);
	    writer.flush();
    }
	
	@Override
	public void serverError(Exception io) {
		int status = 503;
		
		if (io instanceof RuntimeIOException) {
			status = ((RuntimeIOException)io).getStatus();
		}
		
	    response.setStatus(status);
	    response.setHeader(RUNTIME_ERROR_HEADER, io.toString());
	    writer.println(io.toString());
	    writer.flush();
    }
	
	@Override
	public void setContentType(String type)
	{
		response.setContentType(type);
	}

	@Override
    public void setMaxLines(int maxLines, String remoteClusterUri) {
		response.setHeader(X_MAX_LINES, "" + maxLines);
		
		if (remoteClusterUri != null) {
			response.setHeader(X_CLUSTER_URI, remoteClusterUri);
		}
    }
	
	//TODO: remove this eventually, used for idx output
	@Override
	public void writeMiscLine(String line)
	{
		writer.println(line);
	}

	@Override
    public void printNumPages(int numPages, boolean printInBody) {
		response.setHeader(X_NUM_PAGES, "" + numPages);
		if (printInBody) {
			writer.println(numPages);
		}
    }
	
    public static PrintWriter getGzipWriter(HttpServletResponse response) throws IOException
    {
		response.setHeader("Content-Encoding", "gzip");
		
		PrintWriter writer = new PrintWriter(new GZIPOutputStream(response.getOutputStream())
		{
//			{
//			    def.setLevel(Deflater.BEST_COMPRESSION);
//			}
		});
		
		return writer;
    }

    protected static boolean isBlocked(CDXLine line) {
		String robotsFlags = line.getRobotFlags();
		// XXX named constant for 'X' is defined in CaptureSearchResult
		return robotsFlags.indexOf('X') >= 0;
    }
}
