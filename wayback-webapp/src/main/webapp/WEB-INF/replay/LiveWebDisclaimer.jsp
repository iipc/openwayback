<%@
 page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@
 page import="org.archive.wayback.core.CaptureSearchResult"
%><%@
 page import="org.archive.wayback.core.UIResults"
%><%@
 page import="org.archive.wayback.core.WaybackRequest"
%><%@
 page import="org.archive.wayback.util.StringFormatter"
%><%
UIResults results = UIResults.extractReplay(request);
CaptureSearchResult result = results.getResult();
WaybackRequest wbr = results.getWbRequest();
StringFormatter fmt = wbr.getFormatter();
String contextRoot = results.getStaticPrefix();

String urlString = fmt.escapeHtml(wbr.getRequestUrl());
String prettyDateTime = 
	fmt.format("MetaReplay.captureDateDisplay", result.getCaptureDate());
%>
<!-- Start of LiveWebDisclaimer.jsp output -->
<div id="wm-disclaim-banner" style="display:none; position:relative; z-index:99999; background-color:#ffffff; font-size:10px; text-align:center; width:100%;">
	The URL you requested, <%= urlString %> does not exist in this archive. 
	Wayback is showing you a document captured <b>very recently</b> from the live web. The version of the document
	you are now seeing, archived at <%= prettyDateTime %>, will become part of
	the permanent archive after it has been added to the archive index.
</div>
<script type="text/javascript" src="<%= contextRoot %>js/disclaim-element.js" ></script>
<script type="text/javascript">
  var wmDisclaimBanner = document.getElementById("wm-disclaim-banner");
  if(wmDisclaimBanner != null) {
    disclaimElement(wmDisclaimBanner);
  }
</script>
<!-- End of LiveWebDisclaimer.jsp output -->