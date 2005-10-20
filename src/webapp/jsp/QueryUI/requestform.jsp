<jsp:include page="../../template/UI-header.jsp" />
<FORM ACTION="../../query">
URL:<INPUT TYPE="TEXT" NAME="url" WIDTH="80"><BR>
Exact Date:<INPUT TYPE="TEXT" NAME="date" WIDTH="80"><BR>
Earliest Date:<INPUT TYPE="TEXT" NAME="earliest" WIDTH="80"><BR>
Latest Date:<INPUT TYPE="TEXT" NAME="latest" WIDTH="80"><BR>
Type:
Query<INPUT TYPE="RADIO" NAME="type" VALUE="query" CHECKED="YES">
PathQuery<INPUT TYPE="RADIO" NAME="type" VALUE="pathQuery">
<INPUT TYPE="SUBMIT" VALUE="Submit">
</FORM>
<jsp:include page="../../template/UI-footer.jsp" />
