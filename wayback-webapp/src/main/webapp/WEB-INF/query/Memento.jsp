<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResults" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult"%>
<%@ page import="org.archive.wayback.ResultURIConverter" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.archive.wayback.util.Timestamp" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List"  %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.archive.wayback.ResultURIConverter" %>
<%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter" %>
<%
	response.setHeader("Vary", "negotiate,accept-datetime");
	SimpleDateFormat httpformatterl = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss z");
	Date now = new Date();
	UIResults results = UIResults.extractCaptureQuery(request);//nuzno potom perepisat'

	WaybackRequest wbRequest = results.getWbRequest();
	//String p_url = wbRequest.getContextPrefix();

	String u = wbRequest.getRequestUrl();
	// String agguri = p_url.replace("memento","ore") +"timebundle/" + u;
	// String ad = wbRequest.getStartTimestamp();
	// Date sdate = wbRequest.getStartDate();
	//Date pdate = wbRequest.getAnchorDate();
	String dtdate = wbRequest.get("dtconneg");

	Date dt = now;
	if (dtdate != null) {
		dt = httpformatterl.parse(dtdate);
	}
	CaptureSearchResults cResults = results.getCaptureResults();
	CaptureSearchResult res = cResults.getClosest(wbRequest, true);

	Date closestDate = res.getCaptureDate();
	//String url = res.getRedirectUrl();
	String agguri = results.getContextConfig("aggregationPrefix")
			+ "timebundle/" + u;
	String timemap = " , <"
			+ results.getContextConfig("aggregationPrefix")
			+ "timemap/link/" + u
			+ ">;rel=\"timemap\"; type=\"text/csv\"";
	ArchivalUrlResultURIConverter uriconverter = (ArchivalUrlResultURIConverter) results
			.getURIConverter();
	String uriPrefix = uriconverter.getReplayURIPrefix();
	String replayUrl = results.resultToReplayUrl(res);
	//alternates header
	String qvalue = "1.0"; //just example
	StringBuffer sb = new StringBuffer();
	// sb.append("{");
	//sb.append("\"" + u +"\" "+qvalue);
	//sb.append(" {dt original}},");
	//calculate X-Archive-Interval

	StringFormatter fmt = results.getWbRequest().getFormatter();
	Date f = cResults.getFirstResultDate();
	Date l = cResults.getLastResultDate();
	SimpleDateFormat formatterk = new SimpleDateFormat("yyyyMMddHHmmss");

	//sb.append("{\"" +  uriPrefix +formatterk.format(f)+"/" +u +"\" " +qvalue);
	//sb.append(" {dt " + "\""+httpformatterl.format(f) +"\" first}}");
	sb.append(", <" + uriPrefix + formatterk.format(f) + "/" + u
			+ ">;rel=\"first-memento\"; datetime=\""
			+ httpformatterl.format(f) + "\"");
	if (!f.equals(l)) {

		// sb.append(","); 
		// sb.append("{\"" +  uriPrefix +formatterk.format(l)+"/" +u +"\"  " +qvalue);
		// sb.append(" {dt " + "\""+httpformatterl.format(l) +"\" last}}");
		sb.append(", <" + uriPrefix + formatterk.format(f) + "/" + u
				+ ">;rel=\"last-memento\"; datetime=\""
				+ httpformatterl.format(f) + "\"");
	}

	//response.setHeader("X-Archive-Interval","{"+httpformatterl.format(f)+"} - {"+httpformatterl.format(l)+"}");

	// calculate closest values for alternates

	CaptureSearchResult closestleft = null;
	CaptureSearchResult closestright = null;
	long rclosestDistance = 0;
	long lclosestDistance = 0;
	CaptureSearchResult cur = null;
	String anchorDate = null;

	long maxWindow = -1;
	//long wantTime = wbRequest.getReplayDate().getTime();
	long wantTime = closestDate.getTime();

	Iterator<CaptureSearchResult> itr = cResults.iterator();
	while (itr.hasNext()) {
		cur = itr.next();
		cur.getCaptureDate();
		//long curDistance = Math.abs(wantTime - cur.getCaptureDate().getTime());
		long curDistance = wantTime - cur.getCaptureDate().getTime();
		// == 0 propuskaem
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

	if ((dt.before(f)) || dt.after(now)) {
		//if ((pdate.before(f))||pdate.after(now)) {
		response.setHeader("TCN", "list");
		response.setStatus(406);
		// response.setHeader("Link","<"+agguri+">;rel=\"aggregation\"");
		// sb.append("}");
		// response.setHeader("Alternates",sb.toString());
	} else {
		// SimpleDateFormat formatterk = new SimpleDateFormat("yyyyMMddHHmmss");

		// StringBuffer sb = new StringBuffer();

		// List list = new ArrayList();
		if (closestleft != null) {
			if (!closestleft.getCaptureDate().equals(f)) {
				//  sb.append(",");
				//  sb.append("{\"" +  uriPrefix +formatterk.format(closestleft.getCaptureDate())+"/" +u +"\"  "+qvalue);
				//  sb.append(" {dt " +"\""+httpformatterl.format(closestleft.getCaptureDate()) +"\" prev} {type " + closestleft.getMimeType() +"}}");
				sb.append(", <" + uriPrefix + formatterk.format(f)
						+ "/" + u
						+ ">;rel=\"prev-memento\"; datetime=\""
						+ httpformatterl.format(f) + "\"");
				// list.add(closestleft);
			}
		}
		if (closestright != null) {
			if (!closestright.getCaptureDate().equals(l)) {
				//  sb.append(",");
				// sb.append("{\"" +  uriPrefix +formatterk.format(closestright.getCaptureDate())+"/" +u +"\"  " +qvalue);
				//sb.append(" {dt " +"\""+httpformatterl.format(closestright.getCaptureDate()) +"\" next} {type " + closestright.getMimeType() +"}}");
				sb.append(", <" + uriPrefix + formatterk.format(f)
						+ "/" + u
						+ ">;rel=\"next-memento\"; datetime=\""
						+ httpformatterl.format(f) + "\"");
			}

			// list.add(closestright); 
		}

		//  Iterator it =  list.iterator();
		//int count=0; 
		//while (it.hasNext()) {

		//  count++;
		//CaptureSearchResult alt = (CaptureSearchResult) it.next();

		// sb.append("{");
		//sb.append("\"" +  uriPrefix +formatterk.format(alt.getCaptureDate())+"/" +u +"\"  ");
		//sb.append("{dt " + httpformatterl.format(alt.getCaptureDate()) +"} {type " + alt.getMimeType() +"}");

		//sb.append("}");
		//if (count!=list.size()) {
		//  sb.append(",");  }

		//}

		// sb.append("}");
		String origlink = ", <" + u + ">;rel=\"original\"";
		String memento = ",<" + replayUrl
				+ ">;rel=\"memento\";datetime=\""
				+ httpformatterl.format(closestDate) + "\"";
		response.setHeader("Link", "<" + agguri
				+ ">;rel=\"timebundle\"" + origlink + sb.toString()
				+ memento + timemap); //added timemap
		// response.setHeader("Alternates",sb.toString());
		response.setHeader("TCN", "choice");
		response.setHeader("Location", replayUrl);
		//  response.setStatus(302,"Found"); //does'not work
		response.sendError(302, "Found");

	}
%>