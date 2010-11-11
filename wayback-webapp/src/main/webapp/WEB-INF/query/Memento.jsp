<%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.archivalurl.ArchivalUrl"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.core.CaptureSearchResults"
%><%@ page import="org.archive.wayback.core.CaptureSearchResult"
%><%@ page import="org.archive.wayback.ResultURIConverter"
%><%@ page import="java.text.SimpleDateFormat"
%><%@ page import="org.archive.wayback.util.Timestamp"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.List"
%><%@ page import="java.util.ArrayList"
%><%@ page import="org.archive.wayback.ResultURIConverter"
%><%@ page import="java.util.TimeZone"
%><%@ page import="java.io.PrintWriter"
%><%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter"
%><%
	//timegate implementation
	String method = request.getMethod();
	// may be there is better place to put this peace of code
	if (!(method.equals("GET") || method.equals("HEAD"))) {
		response.setStatus(405);
		response.setHeader("Allow", "GET, HEAD");
		return;
	}

	response.setHeader("Vary", "negotiate,accept-datetime");
	SimpleDateFormat httpformatterl = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss z");
	TimeZone tzo = TimeZone.getTimeZone("GMT");
	httpformatterl.setTimeZone(tzo);
	SimpleDateFormat formatterk = new SimpleDateFormat("yyyyMMddHHmmss");
	formatterk.setTimeZone(tzo);
	Date now = new Date();
	UIResults results = UIResults.extractCaptureQuery(request);//nuzno potom perepisat'

	WaybackRequest wbRequest = results.getWbRequest();

	String u = wbRequest.getRequestUrl();

	String dtdate = wbRequest.get("dtconneg");

	CaptureSearchResults cResults = results.getCaptureResults();
	CaptureSearchResult res = cResults.getClosest();

	Date closestDate = res.getCaptureDate();

	String agguri = results.getContextConfig("aggregationPrefix")
			+ "timebundle/" + u;
	String timemap = " , <"
			+ results.getContextConfig("aggregationPrefix")
			+ "timemap/link/" + u
			+ ">;rel=\"timemap\"; type=\"application/link-format\"";
	String origlink = ", <" + u + ">;rel=\"original\"";
	String uriPrefix = wbRequest.getAccessPoint().getReplayPrefix();
	
    ArchivalUrl aUrl = new ArchivalUrl(wbRequest);
    String replayUrl = uriPrefix + aUrl.toString(res.getCaptureTimestamp(), 
            res.getOriginalUrl());

	StringBuffer sb = new StringBuffer();

	String memento = ",<" + replayUrl + ">;rel=\"memento\";datetime=\""
			+ httpformatterl.format(closestDate) + "\"";
	StringFormatter fmt = results.getWbRequest().getFormatter();
	Date f = cResults.getFirstResultDate();
	Date l = cResults.getLastResultDate();

	String mfl = null;
	if ((closestDate.equals(f)) && closestDate.equals(l)) {
		mfl = ", <"
				+ uriPrefix
				+ formatterk.format(f)
				+ "/"
				+ u
				+ ">;rel=\"first last memento\"; datetime=\""
				+ httpformatterl.format(f) + "\"";
	} else if (closestDate.equals(f)) {
		mfl = ", <" + uriPrefix + formatterk.format(f) + "/" + u
				+ ">;rel=\"first memento\"; datetime=\""
				+ httpformatterl.format(f) + "\"";
		mfl = mfl + ", <" + uriPrefix + formatterk.format(l) + "/" + u
				+ ">;rel=\"last memento\"; datetime=\""
				+ httpformatterl.format(l) + "\"";

	} else if (closestDate.equals(l)) {
		mfl = ", <" + uriPrefix + formatterk.format(l) + "/" + u
				+ ">;rel=\"last memento\"; datetime=\""
				+ httpformatterl.format(l) + "\"";
		mfl = mfl + ", <" + uriPrefix + formatterk.format(f) + "/" + u
				+ ">;rel=\"first memento\"; datetime=\""
				+ httpformatterl.format(f) + "\"";
	} else {

		mfl = memento;
		mfl = mfl + ", <" + uriPrefix + formatterk.format(l) + "/" + u
				+ ">;rel=\"last memento\"; datetime=\""
				+ httpformatterl.format(l) + "\"";
		mfl = mfl + ", <" + uriPrefix + formatterk.format(f) + "/" + u
				+ ">;rel=\"first memento\"; datetime=\""
				+ httpformatterl.format(f) + "\"";
	}

	sb = new StringBuffer(mfl);

	if (dtdate == null)
		dtdate = "";

	//special handling date unparsable case
	if (dtdate.equals("unparsable")) {
		String fl = null;
		if (f.equals(l)) {
			fl = ", <"
					+ uriPrefix
					+ formatterk.format(f)
					+ "/"
					+ u
					+ ">;rel=\"last first memento\"; datetime=\""
					+ httpformatterl.format(f) + "\"";

		} else {
			fl = ", <" + uriPrefix + formatterk.format(l) + "/" + u
					+ ">;rel=\"last memento\"; datetime=\""
					+ httpformatterl.format(l) + "\"";
			fl = fl + ", <" + uriPrefix + formatterk.format(f) + "/"
					+ u + ">;rel=\"first memento\"; datetime=\""
					+ httpformatterl.format(f) + "\"";
		}

		response.setStatus(400);
		response.setHeader("Link", "<" + agguri
				+ ">;rel=\"timebundle\"" + origlink + fl + timemap);

		StringBuffer sberr = new StringBuffer();
		sberr.append("<html><head><title>400  Bad Request</title></head><body>");
		sberr.append("<center><table width='800px'><tr><td><div style='background-color: #e0e0e0; padding: 10px;'><br/>");
		sberr.append("<center><b>Error: 400</b><center>");
		sberr.append("<center><p>Bad Date Request.</p>");
		sberr.append("However, we found archived resources available in the following time-range: ");
		sberr.append("<i><blockquote><ul> ");

		sberr.append("<li>Very first available Memento  " + "  at "
				+ uriPrefix + formatterk.format(f) + "/" + u
				+ "</BR>\n");
		sberr.append("<li>Most recent available Memento " + "  at "
				+ uriPrefix + formatterk.format(f) + "/" + u
				+ "</BR>\n");

		sberr.append("</ul> </blockquote></i>");
		sberr.append("<br/></div></td></tr>");
		sberr.append("</table>");
		sberr.append("</body></html>");
		PrintWriter pw = response.getWriter();
		response.setContentType("text/html");
		pw.print(sberr.toString());
		pw.flush();
		pw.close();
		return;
	}

	// calculate closest values for alternates
	CaptureSearchResult closestleft = null;
	CaptureSearchResult closestright = null;
	long rclosestDistance = 0;
	long lclosestDistance = 0;
	CaptureSearchResult cur = null;
	String anchorDate = null;

	long maxWindow = -1;
	long wantTime = closestDate.getTime();

	Iterator<CaptureSearchResult> itr = cResults.iterator();
	while (itr.hasNext()) {
		cur = itr.next();
		cur.getCaptureDate();
		long curDistance = cur.getCaptureDate().getTime() - wantTime;
		// == 0 skip
		if (curDistance > 0) {
			if ((closestright == null)
					|| (Math.abs(curDistance) < Math
							.abs(rclosestDistance))) {
				closestright = cur;
				rclosestDistance = Math.abs(curDistance);
			}
		}

		if (curDistance < 0) {
			if ((closestleft == null)
					|| (Math.abs(curDistance) < Math
							.abs(lclosestDistance))) {
				closestleft = cur;
				lclosestDistance = Math.abs(curDistance);
			}
		}

	}

	if (closestleft != null) {
		if (!(closestleft.getCaptureDate().equals(f))) {
			sb.append(", <"
					+ uriPrefix
					+ formatterk.format(closestleft.getCaptureDate())
					+ "/"
					+ u
					+ ">;rel=\"prev memento\"; datetime=\""
					+ httpformatterl.format(closestleft
							.getCaptureDate()) + "\"");
		} else {
			int m_index = sb.lastIndexOf("\"first memento\"");
			sb.insert(m_index + 1, "prev ");
		}
	}
	if (closestright != null) {
		if (!(closestright.getCaptureDate().equals(l))) {
			sb.append(", <"
					+ uriPrefix
					+ formatterk.format(closestright.getCaptureDate())
					+ "/"
					+ u
					+ ">;rel=\"next \"; datetime=\""
					+ httpformatterl.format(closestright
							.getCaptureDate()) + "\"");
		} else {
			int m_index = sb.lastIndexOf("\"last memento\"");
			sb.insert(m_index + 1, "next ");
		}

	}

	response.setHeader("Link", "<" + agguri + ">;rel=\"timebundle\""
			+ origlink + sb.toString() + timemap); //added timemap

	response.setHeader("Location", replayUrl);
	response.sendError(302, "Found");
%>