<%@   page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.WaybackConstants"
%><%@ page import="org.archive.wayback.core.CaptureSearchResult"
%><%@ page import="org.archive.wayback.core.CaptureSearchResults"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><jsp:include page="/WEB-INF/template/UI-header.jsp" flush="true" />
<%

UIResults results = UIResults.extractCaptureQuery(request);
WaybackRequest wbRequest = results.getWbRequest();
CaptureSearchResults cResults = results.getCaptureResults();
StringFormatter fmt = wbRequest.getFormatter();

String searchString = wbRequest.getRequestUrl();

long resultCount = cResults.getReturnedCount();
Date searchStartDate = wbRequest.getStartDate();
Date searchEndDate = wbRequest.getEndDate();

Iterator<CaptureSearchResult> itr = cResults.iterator();
%>
  <%= fmt.format("PathQuery.resultsSummary",resultCount,searchString) %>
  <br></br>
  <%= fmt.format("PathQuery.resultRange",searchStartDate,searchEndDate) %>
  <%= fmt.format("PathQuery.anchorWindowText") %>
  <jsp:include page="/WEB-INF/template/AnchorWindow.jsp" flush="true" />
  <hr></hr>
  <%
  boolean first = false;
  String lastMD5 = null;
  String lastUrl = null;
  while(itr.hasNext()) {
	  CaptureSearchResult result = (CaptureSearchResult) itr.next();

    String url = result.getUrlKey();
	if(url != lastUrl) {
		lastMD5 = null;		
	}
    String prettyDate = result.getCaptureTimestamp();
    String origHost = result.getOriginalHost();
    String MD5 = result.getDigest();
    String redirectFlag = (0 == result.getRedirectUrl().compareTo("-")) 
      ? "" : fmt.format("PathQuery.redirectIndicator");
    String httpResponse = result.getHttpCode();
    String mimeType = result.getMimeType();

    String arcFile = result.getFile();
    String arcOffset = String.valueOf(result.getOffset());

    String replayUrl = results.resultToReplayUrl(result);

    boolean updated = false;
    if(lastMD5 == null) {
      lastMD5 = MD5;
      updated = true;
    } else if(0 != lastMD5.compareTo(MD5)) {
      updated = true;
      lastMD5 = MD5;
    }
    if(updated) {
      %>
      <a onclick="SetAnchorDate('<%= result.getCaptureTimestamp() %>');" href="<%= replayUrl %>"><%= prettyDate %></a>
      <span style="color:black;"><%= origHost %></span>
      <span style="color:gray;"><%= httpResponse %></span>
      <span style="color:brown;"><%= mimeType %></span>
  <!--
      <span style="color:red;"><%= arcFile %></span>
      <span style="color:red;"><%= arcOffset %></span>
  -->
      <%= redirectFlag %>
      <%= fmt.format("PathQuery.newVersionIndicator") %>

      <br/>
      <%
    } else {
      %>
      &nbsp;&nbsp;&nbsp;<a onclick="SetAnchorDate('<%= result.getCaptureTimestamp() %>');" href="<%= replayUrl %>"><%= prettyDate %></a>
      <span style="color:green;"><%= origHost %></span>
  <!--
      <span style="color:red;"><%= arcFile %></span>
      <span style="color:red;"><%= arcOffset %></span>
  -->
      <br/>
      <%
    }
  } 

// show page indicators:
int curPage = cResults.getCurPageNum();
if(curPage > cResults.getNumPages()) {
  %>
  <hr></hr>
  <a href="<%= results.urlForPage(1) %>">First results</a>
  <%
} else if(cResults.getNumPages() > 1) {
  %>
  <hr></hr>
  <%
  for(int i = 1; i <= cResults.getNumPages(); i++) {
    if(i == curPage) {
      %>
      <b><%= i %></b>
      <%    
    } else {
      %>
      <a href="<%= results.urlForPage(i) %>"><%= i %></a>
      <%
    }
  }
}
%><jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />