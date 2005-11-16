<jsp:include page="../../template/UI-header.jsp" />
<form action="../../replay">
URL:<input type="TEXT" name="url" width="80"><br></br>
Exact Date:<input type="TEXT" name="exactdate" width="80"><br></br>
Earliest Date:<input type="TEXT" name="startdate" width="80"><br></br>
Latest Date:<input type="TEXT" name="enddate" width="80"><br></br>
<input type="HIDDEN" name="type" value="replay">
<input type="SUBMIT" value="Submit">
</form>
<jsp:include page="../../template/UI-footer.jsp" />
