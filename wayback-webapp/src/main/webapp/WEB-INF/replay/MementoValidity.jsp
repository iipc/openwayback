<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResults" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult"%>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Iterator" %>
<%
  	UIResults results = UIResults.extractCaptureQuery(request);
  	WaybackRequest wbRequest = results.getWbRequest();
  	CaptureSearchResults cResults = results.getCaptureResults();
  	CaptureSearchResult res = cResults.getClosest(wbRequest, true);
  	String u = wbRequest.getRequestUrl();
  	SimpleDateFormat httpformatterl = new SimpleDateFormat(
  			"E, dd MMM yyyy HH:mm:ss z");
  	String agguri = results.getContextConfig("aggregationPrefix")
  			+ "timebundle/" + u;
  	String timemap = " , <"
  			+ results.getContextConfig("aggregationPrefix")
  			+ "timemap/link/" + u
  			+ ">;rel=\"timemap\"; type=\"text/csv\"";
  	ArchivalUrlResultURIConverter uriconverter = 
  		(ArchivalUrlResultURIConverter) results.getURIConverter();

  	String uriPrefix = uriconverter.getReplayURIPrefix();

  	Date f = cResults.getFirstResultDate();
  	Date l = cResults.getLastResultDate();
  	String qvalue = "1.0"; //just example
  	StringBuffer sb = new StringBuffer();
  	response.setHeader("Content-Datetime", httpformatterl.format(res
  			.getCaptureDate()));
  	SimpleDateFormat formatterk = new SimpleDateFormat("yyyyMMddHHmmss");
  	sb.append(", <" + uriPrefix + formatterk.format(f) + "/" + u
  			+ ">;rel=\"first-memento\"; datetime=\""
  			+ httpformatterl.format(f) + "\"");
  	if (!f.equals(l)) {

  		sb.append(", <" + uriPrefix + formatterk.format(f) + "/" + u
  				+ ">;rel=\"last-memento\"; datetime=\""
  				+ httpformatterl.format(f) + "\"");
  	}

  	// calculate closest values for alternates
  	Date closestDate = res.getCaptureDate();
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

  	if (closestleft != null) {
  		if (!closestleft.getCaptureDate().equals(f)) {
  			sb.append(", <" + uriPrefix + formatterk.format(f) + "/"
  					+ u + ">;rel=\"prev-memento\"; datetime=\""
  					+ httpformatterl.format(f) + "\"");
  		}
  	}
  	if (closestright != null) {
  		if (!closestright.getCaptureDate().equals(l)) {
  			sb.append(", <" + uriPrefix + formatterk.format(f) + "/"
  					+ u + ">;rel=\"next-memento\"; datetime=\""
  					+ httpformatterl.format(f) + "\"");
  		}

  	}

  	String origlink = ", <" + u + ">;rel=\"original\"";

  	response.setHeader("Link", "<" + agguri + ">;rel=\"timebundle\""
  			+ origlink + sb.toString() + timemap);
%>