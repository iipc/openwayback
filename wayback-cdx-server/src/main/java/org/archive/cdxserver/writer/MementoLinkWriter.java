package org.archive.cdxserver.writer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.cdxserver.CDXQuery;
import org.archive.format.cdx.CDXLine;
import org.archive.util.ArchiveUtils;

public class MementoLinkWriter extends HttpCDXWriter {

	protected boolean hasStarted = false;
		
	protected String timemapUrl;
	protected String timegateUrl;
	protected String format;
	
	protected final static String RESUME_KEY = "resumeKey=";

	
	public MementoLinkWriter(
			HttpServletRequest request, 
			HttpServletResponse response,
			CDXQuery cdxquery,
			boolean gzip)
            throws IOException {
	    super(response, gzip);
	    
	    if (cdxquery.getLimit() > 0) {
	    	cdxquery.setShowResumeKey(true);
	    }
	    
	    StringBuffer requestUrlBuff = request.getRequestURL();
	    
	    int index = requestUrlBuff.indexOf(TIMEMAP);

	    if (index > 0) {
	    	timegateUrl = requestUrlBuff.substring(0, index);
	    	index += TIMEMAP.length() + 1;
	    	int endIndex = requestUrlBuff.indexOf("/", index);
	    	format = ((endIndex > 0) ? requestUrlBuff.substring(index, endIndex) : requestUrlBuff.substring(index));
	    } else {
	    	timegateUrl = requestUrlBuff.toString();
	    	format = LINK;
	    }
	    
	    String query = request.getQueryString();
	    if (query != null) {
	    	requestUrlBuff.append('?');
		    requestUrlBuff.append(request.getQueryString());	
	    }
	    this.timemapUrl = requestUrlBuff.toString();
	    
		setContentType(APPLICATION_LINK_FORMAT);
    }

	@Override
    public void begin() {
	    // TODO Auto-generated method stub
    }
	
	private static String makeLink(String url, String rel) {
		return String.format("<%s>; rel=\"%s\"", url, rel);
	}

	private static String makeLink(String url, String rel, String type) {
		return String.format("<%s>; rel=\"%s\"; type=\"%s\"", url, rel, type);
	}
	
	private static String makeLink(String prefix, String url, String rel, String timestamp)
	{
		Date date = ArchiveUtils.getDate(timestamp, ERROR_DATE);
		return String.format("<%s%s/%s>; rel=\"%s\"; datetime=\"%s\"", prefix, timestamp, url,
				rel, HTTP_LINK_DATE_FORMATTER.format(date));		
	}
	
	public final static String ORIGINAL = "original";
	
	public final static String APPLICATION_LINK_FORMAT = 
			"application/link-format";
	
	public final static String HTTP_LINK_DATE_FORMAT = 
			"E, dd MMM yyyy HH:mm:ss z";
	
	public final static String TIMEGATE = "timegate";
	public final static String TIMEMAP = "timemap";
	public final static String LINK = "link";
	
	public final static String FIRST = "first memento";
	public final static String LAST = "last memento";
	public final static String FIRST_LAST = "first last memento";
	public final static String MEMENTO = "memento";
	
	public final static Date ERROR_DATE = new Date(0);
	
	public final static SimpleDateFormat HTTP_LINK_DATE_FORMATTER;
	
	public final static TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");

	static {
		HTTP_LINK_DATE_FORMATTER = new SimpleDateFormat(HTTP_LINK_DATE_FORMAT);
		HTTP_LINK_DATE_FORMATTER.setTimeZone(GMT_TZ);
	}
	
	protected CDXLine prevLine;
	
	protected void writeHeader(CDXLine firstLine)
	{
		writer.print(makeLink(firstLine.getOriginalUrl(), ORIGINAL));
		writer.println(",");
		
		Date date = ArchiveUtils.getDate(firstLine.getTimestamp(), ERROR_DATE);

		writer.print(makeLink(timemapUrl,
				"self", APPLICATION_LINK_FORMAT)
				+ "; from=\""
				+ HTTP_LINK_DATE_FORMATTER.format(date)
				+ "\"");
			//	+ "; until=\""
			//	+ HTTP_LINK_DATE_FORMATTER.format(last) + "\"");
		writer.println(",");
		writer.print(makeLink(timegateUrl + firstLine.getOriginalUrl(), TIMEGATE));
		writer.println(",");
	}
	
	protected void writeHeaderAndFirstLine(CDXLine line, String first, String last)
	{
		String rel;
		
		if (!hasStarted) {
			writeHeader(prevLine);
			hasStarted = true;
			rel = first;
		} else {
			if (line != null) {
				writer.println(",");
			}
			rel = last;
		}
		
		if (line != null) {
			writer.print(makeLink(timegateUrl, line.getOriginalUrl(), rel, line.getTimestamp()));
			
			String digest = line.getDigest();
			if (!digest.equals(CDXLine.EMPTY_VALUE)) {
				writer.print("; hash=\"sha1:");
				writer.print(digest);
				writer.print("\"");
			}
		}
	}

	@Override
    public int writeLine(CDXLine line) {
		if (!includeBlockedCaptures && isBlocked(line))
			return 0;

		if (prevLine == null) {
			prevLine = line;
			return 1;
		}
		
		writeHeaderAndFirstLine(prevLine, FIRST, MEMENTO);		
		prevLine = line;
		
		return 1;
    }
	

	@Override
    public void writeResumeKey(String resumeKey) {
		writeHeaderAndFirstLine(prevLine, FIRST_LAST, LAST);
		CDXLine lastLine = prevLine;
		prevLine = null;
		
		String timestamp = lastLine.getTimestamp();
		
		Date date = ArchiveUtils.getDate(timestamp, null);
		
		if (date == null) {
			return;
		}
		
		StringBuilder sb = new StringBuilder(timemapUrl);
		int resumeIndex = sb.indexOf(RESUME_KEY);
		if (resumeIndex > 0) {
			int end = sb.indexOf("&", resumeIndex);
			if (end < 0) {
				end = sb.length();
			}
			sb.replace(resumeIndex + RESUME_KEY.length(), end, resumeKey);
		} else {
			sb.append("&");
			sb.append(RESUME_KEY);
			sb.append(resumeKey);
		}
		
		
//		sb.append(TIMEMAP);
//		sb.append("/");
//		sb.append(format);
//		sb.append("/");
//		sb.append(timestamp);
//		sb.append("/");
//		sb.append(lastLine.getOriginalUrl());
		
		writer.println(",");
		writer.print(makeLink(sb.toString(), TIMEMAP, APPLICATION_LINK_FORMAT)
				+ "; from=\""
				+ HTTP_LINK_DATE_FORMATTER.format(date)
				+ "\"");
    }

	@Override
    public void end() {
		writeHeaderAndFirstLine(prevLine, FIRST_LAST, LAST); 
    }
	
}
