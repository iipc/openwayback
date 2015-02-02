<%@ page contentType="text/html" pageEncoding="UTF-8" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="spring" uri="http://www.springframework.org/tags"
%><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" 
%><%@ page session="false" 
%><html>
<head>
    <style>
    
    .main {
      font-size: larger;
      margin: 0px auto;
      padding: 4px; 
      width: 800px; 
      padding-bottom: 20px; 
      text-align: center
    }
    
    .main input {
      font-size: larger;
      margin: 4px;
    }
    
    .search {
      text-align: left;
      width: 800px;
      font-size: larger;
      margin-top: 16px;
    }
    
    .dateInfo {
      font-style: italic; 
      text-align: center;
    }
    
    .info {
      float: right;
      font-style: italic;
    }
    </style>
    <meta http-equiv="refresh" content="1200">
</head>
<body>
  <div id="queryDiv" class="main">
    <h2>How Many Captures in Wayback</h2>

      <form:form id="searchForm" method="get">
      
      Enter url/host/domain:<br/>
      <input name="url" id="url" type="text" placeholder="Everything" value="${url}" />
      <br/>
      <c:if test="${matchType == 'exact' and not empty url}">
        <br/>Date range: (M/D/Y or M/Y or Y)<br/>
        From <input name="from" id="from" size="9" type="text" placeholder="The Past" value="${from}" />
        Until <input name="to" id="to" size="9" type="text" placeholder="The Present" value="${to}" />
        <br/>
      </c:if>
      <br/>
      <input id="doQuery" type="submit" value="Search" />
      
      <!-- The Count -->
      <h1 id="count"><fmt:formatNumber type="number" value="${count}" /></h1>
      
      <span class="dateInfo">
          <c:choose>
          <c:when test="${matchType == 'exact' and not empty url and count == '1'}">
               On <b><fmt:formatDate type="both" pattern="MMMM dd, yyyy HH:mm:ss" timeZone="GMT" value="${first}"/></b>
          </c:when>            
          <c:when test="${matchType == 'exact' and not empty url and count != '0'}">
          Between <b><fmt:formatDate type="both" pattern="MMMM dd, yyyy HH:mm:ss" timeZone="GMT" value="${first}"/></b>
              and <b><fmt:formatDate type="both" pattern="MMMM dd, yyyy HH:mm:ss" timeZone="GMT" value="${last}"/></b>
          </c:when>  
          </c:choose>          
      </span><br/>
      <div class="search">
        <form:radiobutton path="matchType" onclick="submit()" value="exact"/>Exact
        <span class="info" style="display: ${matchType == 'exact' ? 'inline' : 'none'};">Exact captures of <b>${url}</b></span>
        <br/>
         
        <form:radiobutton path="matchType" onclick="submit()" value="prefix"/>Prefix       
        <span class="info" style="display: ${matchType == 'prefix' ? 'inline' : 'none'};">Captures starting with <b>${url}/*</b></span>
        <br/>
         
        <form:radiobutton path="matchType" onclick="submit()" value="host"/>Host
        <span class="info" style="display: ${matchType == 'host' ? 'inline' : 'none'};">Captures from host <b>${host}</b></span>
        <br/>
          
        <form:radiobutton path="matchType" onclick="submit()" value="domain"/>Domain
        <span class="info" style="display: ${matchType == 'domain' ? 'inline' : 'none'};">Captures from all (sub)domains <b>*.${host}</b></span>
        <br/>
      </div>
      </form:form>
  </div>
  <br/>
  <br/>
  <span class="info">v1.0.4</span>
</body>
</html>