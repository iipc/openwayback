<%@
 page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@
 page import="org.archive.wayback.core.WaybackRequest"
%><script type="text/javascript">
function SetCookie(cookieName,cookieValue,nDays) {
  var today = new Date();
  var expire = new Date();
  if (nDays==null || nDays==0) nDays=1;
  expire.setTime(today.getTime() + 86400000*nDays);
  document.cookie = cookieName+"="+escape(cookieValue)
    + ";expires="+expire.toGMTString() + ";path=/";
}
function SetAnchorDate(date) {
  SetCookie("<%= WaybackRequest.REQUEST_ANCHOR_DATE %>",date,365);
}
function SetAnchorWindow(maxSeconds) {
  SetCookie("<%= WaybackRequest.REQUEST_ANCHOR_WINDOW %>",maxSeconds,365);
}
</script>