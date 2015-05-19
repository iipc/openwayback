<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%
UIResults results = UIResults.extractReplay(request);

StringFormatter fmt = results.getWbRequest().getFormatter();
CaptureSearchResult result = results.getResult();
String dupeMsg = "";
if(result != null) {
        if(result.isDuplicateDigest()) {
                Date dupeDate = result.getDuplicateDigestStoredDate();
                String prettyDate = "";
                if(dupeDate != null) {
                    prettyDate = "(" + 
                    		fmt.format("MetaReplay.captureDateDisplay",
                    				dupeDate) + ")";
                }
                dupeMsg = fmt.format("ReplayView.disclaimerText", dupeDate);
        }
}

Date resultDate = result.getCaptureDate();
String resultUrl = result.getOriginalUrl();

String wmNotice = fmt.format("ReplayView.banner", resultUrl, resultDate);
String wmHideNotice = fmt.format("ReplayView.bannerHideLink");
%>
<script type="text/javascript">
  var wmNotice = "<%= wmNotice %><%= dupeMsg %>";
  var wmHideNotice = "<%= wmHideNotice %>";
</script>
<script type="text/javascript" src="<%= results.getStaticPrefix() %>js/disclaim.js"></script>
