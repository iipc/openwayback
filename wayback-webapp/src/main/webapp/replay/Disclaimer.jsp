<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.replay.UIReplayResult" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIReplayResult results = (UIReplayResult) UIResults.getFromRequest(request);

StringFormatter fmt = results.getFormatter();
CaptureSearchResult result = results.getResult();
String dupeMsg = "";
if(result != null) {
        if(result.isDuplicateDigest()) {
                String dupeDate = result.getDuplicateDigestStoredTimestamp();
                String prettyDate = "";
                if(dupeDate != null) {
                	  Timestamp dupeTS = Timestamp.parseBefore(dupeDate);
                    prettyDate = "(" + 
                    		fmt.format("MetaReplay.captureDateDisplay",
                    				dupeTS.getDate()) + ")";
                }
                dupeMsg = " Note that this document was downloaded, and not saved because it was a duplicate of a previously captured version " + 
                          prettyDate + ". HTTP headers presented here are from the original capture.";
        }
}

Date resultDate = result.getCaptureDate();
String resultUrl = result.getOriginalUrl();

String wmNotice = fmt.format("ReplayView.banner", resultUrl, resultDate);
String wmHideNotice = fmt.format("ReplayView.bannerHideLink");

String contextRoot = request.getScheme() + "://" + request.getServerName() + ":"
+ request.getServerPort() + request.getContextPath();
String jsUrl = contextRoot + "/replay/disclaim.js";
%>
<script type="text/javascript">
  var wmNotice = "<%= wmNotice %><%= dupeMsg %>";
  var wmHideNotice = "<%= wmHideNotice %>";
</script>
<script type="text/javascript" src="<%= jsUrl %>"></script>
