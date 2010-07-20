<%@   page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.ResultURIConverter"
%><%@ page import="org.archive.wayback.WaybackConstants"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.UrlSearchResult"
%><%@ page import="org.archive.wayback.core.UrlSearchResults"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%>
<jsp:include page="/WEB-INF/global-template/UI-header.jsp" flush="true" />
<%
UIResults results = UIResults.extractUrlQuery(request);
WaybackRequest wbRequest = results.getWbRequest();
UrlSearchResults uResults = results.getUrlResults();
ResultURIConverter uriConverter = results.getURIConverter();
StringFormatter fmt = wbRequest.getFormatter();

String searchString = wbRequest.getRequestUrl();
String staticPrefix = results.getStaticPrefix();
String queryPrefix = results.getQueryPrefix();
String replayPrefix = results.getReplayPrefix();

Date searchStartDate = wbRequest.getStartDate();
Date searchEndDate = wbRequest.getEndDate();

long firstResult = uResults.getFirstReturned();
long lastResult = uResults.getReturnedCount() + firstResult;

long totalCaptures = uResults.getMatchingCount();

%>
<script type="text/javascript" src="<%= staticPrefix %>js/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="<%= staticPrefix %>js/jquery.dataTables.min.js" charset="utf-8"></script>
<script type="text/javascript">
$().ready(function(){
    $('#resultsUrl th.url span').html('&nbsp;&uarr;');
    $('#resultsUrl th').mouseup(function(){
                \$('#resultsUrl th span').html('');
                \$(this).find('span').html('&nbsp;&uarr;');            
                if (\$(this).hasClass('sorting_asc')) {
                    \$(this).find('span').html('&nbsp;&darr;');
                } else if (\$(this).hasClass('sorting_desc')) {
                    \$(this).find('span').html('&nbsp;&uarr;');
                };
            });
           var rowCount = \$('#resultsUrl tbody tr').length;
            if (rowCount < 50) {
                \$('#resultsUrl').dataTable({
                    "bProcessing": true,
                    "aoColumns": [{"sType":"html"},{"sType":"date"},{"sType":"date"},null,null,null],
                    "aaSorting": [ [0,'asc'] ],
                    "bPaginate": false,
                    "bInfo": false,
                    "bFilter": true,
                    "bStateSave": true,
                    "bAutoWidth": false,
                    "oLanguage": {
                        "sSearch": "Filter results (i.e. '.txt'):"
                    }
                });
            } else {
                \$('#resultsUrl').dataTable({
                    "bProcessing": true,
                    "aoColumns": [{"sType":"html"},{"sType":"date"},{"sType":"date"},null,null,null],
                    "aaSorting": [ [0,'asc'] ],
                    "bPaginate": true,
                    "bInfo": true,
                    "sPaginationType": "full_numbers",
                    "bFilter": true,
                    "bStateSave": true,
                    "bAutoWidth": false,
                    "oLanguage": {
                        "sSearch": "Filter results (i.e. '.txt'):"
                    },
                    "iDisplayLength": 50
                });
            }
        });
</script>
        <div id="positionHome">
            <section>
            <div id="logoHome">
                <a href="/index.jsp"><h1><span>Internet Archive's Wayback Machine</span></h1></a>
            </div>
            </section>
            <section>
            <div id="searchHome">
                <form name="form1" method="get" action="<%= queryPrefix %>query">
					<input type="hidden" name="<%= WaybackRequest.REQUEST_TYPE %>" value="<%= WaybackRequest.REQUEST_CAPTURE_QUERY %>">
					<input type="text" name="<%= WaybackRequest.REQUEST_URL %>" value="http://" size="40">
                    <button type="submit" name="Submit">Go Wayback!</button>
                </form>
                <div id="searchAdvHome">
                    <a href="[ADVANCED SEARCH]">Advanced Search</a>
                </div>
            </div>
            </section>
        </div>
        <div id="positionTable">

        <h2 class="green"><%= fmt.format("PathPrefixQuery.showingResults",totalCaptures) %></h2>

    <table id="resultsUrl">
        <thead>
            <tr>
                <th class="url">URL<span></span></th>
                <th>From<span></span></th>
                <th>To<span></span></th>
                <th>Captures<span></span></th>
                <th>Duplicates<span></span></th>
                <th>Uniques<span></span></th>
            </tr>
        </thead>
        <tbody>
<%
Iterator<UrlSearchResult> itr = uResults.iterator();
while(itr.hasNext()) {
  UrlSearchResult result = itr.next();

  String urlKey = result.getUrlKey();
  String originalUrl = result.getOriginalUrl();
  String firstDateTSss = result.getFirstCaptureTimestamp();
  String lastDateTSss = result.getLastCaptureTimestamp();
  long numCaptures = result.getNumCaptures();
  long numVersions = result.getNumVersions();
  long numDupes = result.getNumCaptures() - result.getNumVersions();

  Date firstDate = result.getFirstCaptureDate();
  Date lastDate = result.getLastCaptureDate();
  
  if(numCaptures == 1) {
	  String ts = result.getFirstCaptureTimestamp();
	  String anchor = uriConverter.makeReplayURI(ts,originalUrl);
    %>
            <tr>
                <td class="url">
                    <a onclick="SetAnchorDate('<%= ts %>');" href="<%= anchor %>"><%= urlKey %></a>
                </td>
                <td class="dateFrom"><%= fmt.format("PathPrefixQuery.captureDate",firstDate) %></td>
                <td class="dateTo"><%= fmt.format("PathPrefixQuery.captureDate",lastDate) %></td>
                <td class="captures"><%= numCaptures %></td>
                <td class="dupes"><%= numDupes %></td>
                <td class="uniques"><%= numVersions %></td>
            </tr>
    <!--
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.versionCount",numVersions) %>
    </span>
    <br/>
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.singleCaptureDate",firstDate) %>
    </span>
    -->
    <%
    
  } else {
    String anchor = results.makeCaptureQueryUrl(originalUrl);
    %>
            <tr>
                <td class="url">
                    <a href="<%= anchor %>"><%= urlKey %></a>
                </td>
                <td class="dateFrom"><%= fmt.format("PathPrefixQuery.captureDate",firstDate) %></td>
                <td class="dateTo"><%= fmt.format("PathPrefixQuery.captureDate",lastDate) %></td>
                <td class="captures"><%= numCaptures %></td>
                <td class="dupes"><%= numDupes %></td>
                <td class="uniques"><%= numVersions %></td>
            </tr>
    <!--
    <a href="<%= anchor %>">
      <%= urlKey %>
    </a>
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.versionCount",numVersions) %>
    </span>
    <br/>
    <span class="mainSearchText">
      <%= fmt.format("PathPrefixQuery.multiCaptureDate",numCaptures,firstDate,lastDate) %>
    </span>
    -->
    <%    
  }
  %>
  <%
}

// show page indicators:
int curPage = uResults.getCurPageNum();
if(curPage > uResults.getNumPages()) {
  %>
  <a href="<%= results.urlForPage(1) %>">First results</a>
  <%
} else if(uResults.getNumPages() > 1) {
  %>

  <%
  for(int i = 1; i <= uResults.getNumPages(); i++) {
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
%>
        </tbody>
    </table>
        

<jsp:include page="/WEB-INF/global-template/UI-footer.jsp" flush="true" />