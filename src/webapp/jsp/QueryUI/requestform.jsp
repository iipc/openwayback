<jsp:include page="../../template/UI-header.jsp" />
<h2>Wayabck Search form:</h2>
<p>The URL field is required. All date fields are optional.<br>
To search for a single URL only, use the Query Type.<br>
To search for all URLs beginning with a prefix URL, use PathQuery Type.<br>
</p>
<hr>
<table>
<FORM ACTION="../../query">
<tr><td>URL:</td><td><INPUT TYPE="TEXT" NAME="url" WIDTH="80"></td></tr>
<tr><td>Exact Date:</td><td><INPUT TYPE="TEXT" NAME="date" WIDTH="80"></td></tr>
<tr><td>Earliest Date:</td><td><INPUT TYPE="TEXT" NAME="earliest" WIDTH="80"></td></tr>
<tr><td>Latest Date:</td><td><INPUT TYPE="TEXT" NAME="latest" WIDTH="80"></td></tr>
<tr>
	<td>Type:</td>
	<td>
		Query <INPUT TYPE="RADIO" NAME="type" VALUE="query" CHECKED="YES">
		PathQuery <INPUT TYPE="RADIO" NAME="type" VALUE="pathQuery">
	</td>
</tr>
<tr><td colspan="2" align="left"><INPUT TYPE="SUBMIT" VALUE="Submit"></td></tr>
</FORM>
</table>
<jsp:include page="../../template/UI-footer.jsp" />
