<%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.CaptureSearchResults"
%><%@ page import="org.archive.wayback.core.CaptureSearchResult"
%><%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter"
%><%@ page import="java.text.SimpleDateFormat"
%><%@ page import="java.util.TimeZone"
%><%@ page import="java.util.Iterator"
%><%
	UIResults results = UIResults.extractCaptureQuery(request);
	WaybackRequest wbRequest = results.getWbRequest();
	CaptureSearchResults cResults = results.getCaptureResults();
	CaptureSearchResult res = cResults.getClosest();
	String u = wbRequest.getRequestUrl();
	SimpleDateFormat httpformatterl = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss z");
	TimeZone tzo = TimeZone.getTimeZone("GMT");
	httpformatterl.setTimeZone(tzo);
	SimpleDateFormat formatterk = new SimpleDateFormat("yyyyMMddHHmmss");
	formatterk.setTimeZone(tzo);

	ArchivalUrlResultURIConverter uriconverter = (ArchivalUrlResultURIConverter) results
			.getURIConverter();
	Date closestDate = res.getCaptureDate();
	String uriPrefix = uriconverter.getReplayURIPrefix();
	String agguri = results.getContextConfig("aggregationPrefix")
			+ "timebundle/" + u;
	String timemap = " , <"
			+ results.getContextConfig("aggregationPrefix")
			+ "timemap/link/" + u
			+ ">;rel=\"timemap\"; type=\"application/link-format\"";

	String timegate = ",<" + uriPrefix + "timegate/" + u
			+ ">;rel=\"timegate\"";

	Date f = cResults.getFirstResultDate();
	Date l = cResults.getLastResultDate();

	StringBuffer sb = new StringBuffer();

	response.setHeader("Memento-Datetime",
			httpformatterl.format(res.getCaptureDate()));

	String memento = ",<" + uriPrefix + formatterk.format(closestDate)
			+ "/" + u + ">;rel=\"memento\";datetime=\""
			+ httpformatterl.format(closestDate) + "\"";
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

	// calculate closest values for  link header

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
					+ ">;rel=\"next memento\"; datetime=\""
					+ httpformatterl.format(closestright
							.getCaptureDate()) + "\"");
		} else {
			int m_index = sb.lastIndexOf("\"last memento\"");
			sb.insert(m_index + 1, "next ");

		}

	}

	String origlink = ", <" + u + ">;rel=\"original\"";

	response.setHeader("Link", "<" + agguri + ">;rel=\"timebundle\""
			+ origlink + sb.toString() + timemap + timegate);


%>