<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.replay.UIReplayResult" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%

UIReplayResult uiResults = (UIReplayResult) UIResults.getFromRequest(request);
StringFormatter fmt = uiResults.getFormatter();

String origUrl = uiResults.getOriginalUrl();
String urlKey = uiResults.getUrlKey();
String archiveID = uiResults.getArchiveID();
Timestamp captureTS = uiResults.getCaptureTimestamp();
String capturePrettyDateTime = fmt.format("MetaReplay.captureDateDisplay",
	captureTS.getDate());
String mimeType = uiResults.getMimeType();
String digest = uiResults.getDigest();
Map<String,String> headers = uiResults.getHttpHeaders();

%>
<html>
	<head>
		<title>
			<%= fmt.format("MetaReplay.title") + urlKey +" / " +
				capturePrettyDateTime %>
		</title>
	</head>
	<body>
		<h2>
			<%= fmt.format("MetaReplay.title") %>
		</h2>
		<table>
			<tr>
				<td class="field-cell">
					<%= fmt.format("MetaReplay.originalURL") %>
				</td>
				<td class="value-cell">
					<b>
						<%= origUrl %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					<%= fmt.format("MetaReplay.URLKey") %>
				</td>
				<td class="value-cell">
					<b>
						<%= urlKey %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					<%= fmt.format("MetaReplay.captureDate") %>
				</td>
				<td class="value-cell">
					<b>
						<%= capturePrettyDateTime %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					<%= fmt.format("MetaReplay.archiveID") %>
				</td>
				<td class="value-cell">
					<b>
						<%= archiveID %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					<%= fmt.format("MetaReplay.MIMEType") %>
				</td>
				<td class="value-cell">
					<b>
						<%= mimeType %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					<%= fmt.format("MetaReplay.digest") %>
				</td>
				<td class="value-cell">
					<b>
						<%= digest %>
					</b>
				</td>
			</tr>
		</table>
		<p>
			<h2>
				<%= fmt.format("MetaReplay.HTTPHeaders") %>
			</h2>
			<table>
			<%
			Iterator<String> itr = headers.keySet().iterator();
			while(itr.hasNext()) {
				String key = itr.next();
				String value = headers.get(key);
				%>
				<tr>
					<td class="field-cell">
						<%= key %>
					</td>
					<td class="value-cell">
						<b>
							<%= value %>
						</b>
					</td>
				</tr>
				<%
			}
			%>
			</table>
		
	</body>
</html>

