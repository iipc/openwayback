<%@ page contentType="text/plain" pageEncoding="UTF-8" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ page session="false" 
%><fmt:formatNumber type="number" pattern="###.#" value="${count / 1000000000}" />