<%@ page language="java" pageEncoding="utf-8" contentType="text/css;charset=utf-8"
%><%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%
UIResults results = UIResults.extractReplay(request);
StringFormatter fmt = results.getWbRequest().getFormatter();
Date exactDate = results.getResult().getCaptureDate();
Date now = new Date();
String prettyDateFormat = "{0,date,H:mm:ss MMM d, yyyy}";
String prettyArchiveString = fmt.format(prettyDateFormat,exactDate);
String prettyRequestString = fmt.format(prettyDateFormat,now);
%>
/*
     FILE ARCHIVED ON <%= prettyArchiveString %> AND RETRIEVED FROM THE
     INTERNET ARCHIVE ON <%= prettyRequestString %>.
     JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.

     ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.
     SECTION 108(a)(3)).
*/
intentionally broken stuff here to assist Wayback replay...

