<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.archive.wayback.core.Timestamp" %>
<%@ page import="org.archive.wayback.replay.UIReplayResult" %>
<%

UIReplayResult uiResult = (UIReplayResult) request.getAttribute("ui-result");
String origUrl = uiResult.getOriginalUrl();
String urlKey = uiResult.getUrlKey();
String archiveID = uiResult.getArchiveID();
Timestamp captureTS = uiResult.getCaptureTimestamp();
String capturePrettyDateTime = captureTS.prettyDateTime();
String mimeType = uiResult.getMimeType();
String digest = uiResult.getDigest();
Properties headers = uiResult.getHttpHeaders();

%>
<html>
	<head>
		<title>
			Metadata for <%= urlKey +" / " + capturePrettyDateTime %>
		</title>
	</head>
	<body>
		<h2>
			Document Metadata
		</h2>
		<table>
			<tr>
				<td class="field-cell">
					Original URL
				</td>
				<td class="value-cell">
					<b>
						<%= origUrl %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					URL Key
				</td>
				<td class="value-cell">
					<b>
						<%= urlKey %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					Capture Date
				</td>
				<td class="value-cell">
					<b>
						<%= capturePrettyDateTime %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					Archive ID
				</td>
				<td class="value-cell">
					<b>
						<%= archiveID %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					Mime Type
				</td>
				<td class="value-cell">
					<b>
						<%= mimeType %>
					</b>
				</td>
			</tr>
			<tr>
				<td class="field-cell">
					Digest
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
				HTTP Headers
			</h2>
			<table>
			<%
			for (Enumeration e = headers.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				String value = (String) headers.get(key);
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

