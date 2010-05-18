<%@
 page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@
 page import="java.util.Date"
%><%@
 page import="java.util.Map"
%><%@
 page import="java.util.Set"
%><%@
 page import="java.util.Iterator"
%><%@
 page import="org.archive.wayback.WaybackConstants"
%><%@
 page import="org.archive.wayback.core.CaptureSearchResult"
%><%@
 page import="org.archive.wayback.core.CaptureSearchResults"
%><%@
 page import="org.archive.wayback.core.SearchResult"
%><%@
 page import="org.archive.wayback.core.UIResults"
%><%@
 page import="org.archive.wayback.core.WaybackRequest"
%><%@
 page import="org.archive.wayback.util.StringFormatter"
%><%@
 page import="org.archive.wayback.util.html.SelectHTML"
%><jsp:include page="/WEB-INF/template/CookieJS.jsp" flush="true" /><%
UIResults results = UIResults.extractReplay(request);
WaybackRequest wbr = results.getWbRequest();
Set<String> keys = wbr.keySet();
Iterator<String> keysItr = keys.iterator();
Map<String,String> headers = results.getResource().getHttpHeaders();
%>
<!--
Start of DebugBanner.jsp output
-->
<script type="text/javascript">
function SetCookie(cookieName,cookieValue,nDays) {
  var today = new Date();
  var expire = new Date();
  if (nDays==null || nDays==0) nDays=1;
  expire.setTime(today.getTime() + 3600000*24*nDays);
  document.cookie = cookieName+"="+escape(cookieValue)
    + ";expires="+expire.toGMTString() + ";path=/";
}
function DoCookieThing() {
  var nI = document.getElementById("cookName");
  var vI = document.getElementById("cookValue");
  if(nI != null) {
    if(vI != null) {
      SetCookie(nI.value,vI.value,365);
    }
  }
}
function toggleID(id) {
  var nI = document.getElementById(id);
  if(nI != null) {
    if(nI.style.display == "none") {
      nI.style.display = "block";
    } else {
      nI.style.display = "none";
    }
  }
}
function showHide(id,val) {
  var nI = document.getElementById(id);
  if(nI != null) {
    nI.style.display=val;
  }
}
</script>
<div id="wm-debug-banner" style="display:none; position:relative; z-index:99999; background-color:#ffffff; font-size:10px; text-align:center; width:100%;">
  <button onmouseover="showHide('requestDiv','block');" onmouseout="showHide('requestDiv','none');">Request Parameters</button>
  <div id="requestDiv" style="display:none; position:absolute; background-color:white; border:line;">
  <table style="border:0px solid #000000; margin:0px; padding:0px; border-spacing:0px; color:black; width:100%;">
<%
while(keysItr.hasNext()) {
	 String key = keysItr.next();
	 String val = wbr.get(key);
	 %>
    <tr>
      <td>
        <%= key %>
      </td>
      <td>
        <%= val %>
      </td>
    </tr>
	 <%
}
%>
  </table>
  </div>
  <button  onmouseover="showHide('resultDiv','block');" onmouseout="showHide('resultDiv','none');">Result Data</button>
  <div id="resultDiv" style="display:none; position:absolute; background-color:white; border:line;" class="fdfdfd">
  <table style="border:0px solid #000000; margin:0px; padding:0px; border-spacing:0px; color:black; width:100%;">
<%
CaptureSearchResult result = results.getResult();
Map<String,String> resultMap = result.toCanonicalStringMap();
keysItr = resultMap.keySet().iterator();
while(keysItr.hasNext()) {
	   String key = keysItr.next();
	   String val = resultMap.get(key);
	   %>
	    <tr>
	      <td>
	        <%= key %>
	      </td>
	      <td>
	        <%= val %>
	      </td>
	    </tr>
	   <%
	}
%>
    </table>
  </div> 
  Set Anchor Window:
  <jsp:include page="/WEB-INF/template/AnchorWindow.jsp" flush="true" />
  <button onclick="toggleID('cookieDiv');">CookieForm</button>
   <div id="cookieDiv" style="position:absolute; display:none; background-color:white; color:black;">
  <form name="setCookie">
    <table border=0 cellpadding=3 cellspacing=3>
      <tr>
        <td>Cookie Name:&nbsp;</td>
        <td><input name=t1 type=text size=20 value="cookieName"></td>
      </tr>
      <tr>
        <td>Cookie Value:&nbsp;</td>
        <td><input name=t2 type=text size=20 value="cookieValue"></td>
      </tr>
      <tr>
        <td>Must expire in:&nbsp;</td>
        <td><input name=t3 type=text size=3 value="5"> days from today</td>
      </tr>
      <tr>
        <td></td>
        <td>
          <input name=b1 type=button value="Set Cookie" 
            onClick="SetCookie(this.form.t1.value,this.form.t2.value,this.form.t3.value);">
        </td>
      </tr>
    </table>
  </form>
  </div>
</div>
<script type="text/javascript" src="<%= results.getStaticPrefix() %>js/disclaim-element.js" ></script>
<script type="text/javascript">
  var debugBanner = document.getElementById("wm-debug-banner");
  if(debugBanner != null) {
    disclaimElement(debugBanner);
  }
</script>
<!--
End of DebugBanner.jsp output
-->