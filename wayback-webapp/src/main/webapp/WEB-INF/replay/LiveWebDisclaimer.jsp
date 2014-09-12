<%@   page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.core.CaptureSearchResult"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.util.url.UrlOperations"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%
UIResults results = UIResults.extractReplay(request);
CaptureSearchResult result = results.getResult();
WaybackRequest wbRequest = results.getWbRequest();
StringFormatter fmt = wbRequest.getFormatter();

String staticPrefix = results.getStaticPrefix();
String queryPrefix = results.getQueryPrefix();
String replayPrefix = results.getReplayPrefix();

String searchUrl = 
	UrlOperations.stripDefaultPortFromUrl(wbRequest.getRequestUrl());
String searchUrlSafe = fmt.escapeHtml(searchUrl);

Date captureDate = result.getCaptureDate();
Date now = new Date();
long capMSSE = captureDate.getTime();
long nowMSSE = now.getTime();
long ageMS = nowMSSE - capMSSE;
float mins = ageMS / (60000f);
long ageMins = Math.round(mins);

String prettyDateTime = 
	fmt.format("MetaReplay.captureDateDisplay", result.getCaptureDate());
%>
<!-- Start of LiveWebDisclaimer.jsp output -->

<style type="text/css">body{margin-top:0!important;padding-top:0!important;min-width:800px!important;}#wm-ipp a:hover{text-decoration:underline!important;}</style>
<div id="wm-ipp" style="display:none; position:relative;padding:0 5px;min-height:70px;min-width:800px; z-index:9000;">
<div id="wm-ipp-inside" style="position:fixed;padding:0!important;margin:0!important;width:97%;min-width:780px;border:5px solid #000;border-top:none;background-image:url(<%= staticPrefix %>images/toolbar/wm_tb_bk_trns.png);text-align:center;-moz-box-shadow:1px 1px 3px #333;-webkit-box-shadow:1px 1px 3px #333;box-shadow:1px 1px 3px #333;font-size:11px!important;font-family:'Lucida Grande','Arial',sans-serif!important;">
   <table style="border-collapse:collapse;margin:0;padding:0;width:100%;"><tbody><tr>
   <td style="padding:10px;vertical-align:top;min-width:110px;">
   <a href="<%= queryPrefix %>" title="Wayback Machine home page" style="background-color:transparent;border:none;"><img src="<%= staticPrefix %>images/toolbar/wayback-toolbar-logo.png" alt="Wayback Machine" width="110" height="39" border="0"/></a>
   </td>
   <td style="padding:0!important;text-align:center;vertical-align:top;width:100%;">

       <table style="border-collapse:collapse;margin:0 auto;padding:0;width:570px;"><tbody><tr>
       <td style="padding:3px 0;" colspan="2">
           <form target="_top" method="get" action="<%= queryPrefix %>query" name="wmtb" id="wmtb" style="margin:0!important;padding:0!important;"><input type="text" name="<%= WaybackRequest.REQUEST_URL %>" id="wmtbURL" value="<%= searchUrlSafe %>" style="width:400px;font-size:11px;font-family:'Lucida Grande','Arial',sans-serif;"/><input type="hidden" name="<%= WaybackRequest.REQUEST_TYPE %>" value="<%= WaybackRequest.REQUEST_REPLAY_QUERY %>"><input type="hidden" name="<%= WaybackRequest.REQUEST_DATE %>" value="<%= result.getCaptureTimestamp() %>"><input type="submit" value="Go" style="font-size:11px;font-family:'Lucida Grande','Arial',sans-serif;margin-left:5px;"/><span id="wm_tb_options" style="display:block;"></span></form>
       </td>
       </tr>
       <tr>
       <td style="padding:0!important;">
           <table style="border-collapse:collapse;margin:0;padding:0;width:100%;"><tbody><tr>
               <td rowspan="2" style="padding-right:10px!important;"><img src="<%= staticPrefix %>images/toolbar/icon_alert.png" width="24" height="24" border="0" alt="Alert!"/></td>
               <td style="font-family:'Arial',sans-serif;font-size:14px;font-weight:700;color:#2eaeec; text-align:left;">The Wayback Machine hasn't archived a capture for that URL.</td>
           </tr>
           <tr>
               <td style="font-family:'Georgia',serif;font-size:13px;color:#333; text-align:left;">Here's a capture taken <%= ageMins %> minutes ago from the live web that will become part of the permanent archive in the next few months.</td>
           </tr></tbody></table>
       </td>
       </tr></tbody></table>
   </td>
   <td style="text-align:right;padding:5px;width:65px;font-size:11px!important;">
       <a href="javascript:;" onclick="document.getElementById('wm-ipp').style.display='none';" style="display:block;padding-right:18px;background:url(<%= staticPrefix %>images/toolbar/wm_tb_close.png) no-repeat 100% 0;color:#33f;font-family:'Lucida Grande','Arial',sans-serif;margin-bottom:23px;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.closeTitle") %>"><%= fmt.format("ToolBar.closeText") %></a>
       <a href="<%= fmt.format("UIGlobal.helpUrl") %>" style="display:block;padding-right:18px;background:url(<%= staticPrefix %>images/toolbar/wm_tb_help.png) no-repeat 100% 0;color:#33f;font-family:'Lucida Grande','Arial',sans-serif;background-color:transparent;border:none;" title="<%= fmt.format("ToolBar.helpTitle") %>"><%= fmt.format("ToolBar.helpText") %></a>
   </td>
   </tr></tbody></table>

</div>
</div>
<script type="text/javascript" src="<%= staticPrefix %>js/disclaim-element.js" ></script>
<script type="text/javascript">
  var wmDisclaimBanner = document.getElementById("wm-ipp");
  if(wmDisclaimBanner != null) {
    disclaimElement(wmDisclaimBanner);
  }
</script>
<!-- End of LiveWebDisclaimer.jsp output -->
