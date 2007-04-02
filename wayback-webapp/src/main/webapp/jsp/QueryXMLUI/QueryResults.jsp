<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.archive.wayback.WaybackConstants" %>
<%@ page import="org.archive.wayback.core.SearchResults" %>
<%@ page import="org.archive.wayback.core.SearchResult" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.query.UIQueryResults" %>
<%
UIQueryResults uiResults = (UIQueryResults) UIResults.getFromRequest(request);
SearchResults results = uiResults.getResults();
Iterator itr = uiResults.resultsIterator();
%>
<wayback>
	<request>
<%
	Properties p = results.getFilters();
	for (Enumeration e = p.keys(); e.hasMoreElements();) {
		String key = UIQueryResults.encodeXMLEntity((String) e.nextElement());
		String value = UIQueryResults.encodeXMLContent((String) p.get(key));
		%>
		<<%= key %>><%= value %></<%= key %>>
		<%
	}
%>
	</request>
	<results>
<%
	while(itr.hasNext()) {
		%>
		<result>
		<%
		SearchResult result = (SearchResult) itr.next();
		Properties p2 = result.getData();
		for (Enumeration e = p2.keys(); e.hasMoreElements();) {
			// TODO: encode!
			String key = UIQueryResults.encodeXMLEntity((String) e.nextElement());
			String value = UIQueryResults.encodeXMLContent((String) p2.get(key));
			%>
			<<%= key %>><%= value %></<%= key %>>
			<%
		}
		%>
		</result>
		<%
	}
%>	
	</results>
</wayback>
