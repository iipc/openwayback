<jsp:include page="../../template/UI-header.jsp" />
<h2>Wayback Search form:</h2>
<p>The URL field is required. All date fields are optional.<br></br>
To search for a single URL only, use the Query Type.<br></br>
To search for all URLs beginning with a prefix URL, use PathQuery Type.<br></br>
</p>
<hr>
<table>
<form action="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() %>/query">
<tr><td>URL:</td><td><input type="TEXT" name="url" WIDTH="80"></td></tr>
<tr><td>Exact Date:</td><td><input type="TEXT" name="exactdate" WIDTH="80"></td></tr>
<tr><td>Earliest Date:</td><td><input type="TEXT" name="startdate" WIDTH="80"></td></tr>
<tr><td>Latest Date:</td><td><input type="TEXT" name="enddate" WIDTH="80"></td></tr>
<tr>
	<td>Type:</td>
	<td>
		Query <input type="RADIO" name="type" value="urlquery" CHECKED="YES">
		PathQuery <input type="RADIO" name="type" value="urlprefixquery">
	</td>
</tr>
<tr><td colspan="2" align="left"><input type="SUBMIT" value="Submit"></td></tr>
</form>
</table>
<jsp:include page="../../template/UI-footer.jsp" />
