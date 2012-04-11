<%@ include file="header.inc"%>

<form action="<c:url value="/admin"/>" method="GET" id="navForm">
  <label for="surtNavBox">
    <a href="http://crawler.archive.org/articles/user_manual/glossary.html#surt">
      <acronym title="Sort-friendly URI Reordering Transform">SURT</acronym>
      or URL:
    </a>
  </label>
  <input size="50" name="surt" value="<c:out value="${surt}"/>" id="surtNavBox" />
  <input type="submit" value="Go!" />
</form>

<div id="breadcrumbsContainer">
<ul id="breadcrumbs">
	<c:forEach var="node" items="${breadcrumbs}">
		<li><a href="<c:url value="/admin?surt=${node.surt}" />"
			title="<c:out value="${node.surt}" />"><c:out value="${node.name}" /></a></li>
	</c:forEach>
</ul>
</div>

<div id="childSurts">
<ul>
  <c:forEach var="child" items="${childSurts}">
    <li><a href="<c:url value="/admin?surt=${surt}${child}" />"><c:out value="${child}" /></a></li>
  </c:forEach>
</ul>
</div>

<table>
	<thead>
		<tr>
			<th>SURT</th>
			<th>Capture date</th>
			<th>Retrieval date</th>
			<th>Group</th>
			<th>Policy</th>
			<th></th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="rule" items="${rules }">
			<c:choose>
				<c:when test="${rule.editing }">
					<tr class="rule editing">
						<td colspan="6">
						<form action="<c:url value="/admin"/>" method="post"><input
							type="hidden" name="edit"
							value="<c:out value="${rule.rule.id}"/>" />
						<fieldset>
						<p><label for="surt">SURT:</label> <input name="surt"
							id="surt" value="<c:out value="${rule.rule.surt}"/>" /></p>

						<p><label for="who">Group:</label> <input name="who" id="who"
							value="<c:out value="${rule.rule.who }"/>" /></p>

						<p><label for="captureStart">Captured from</label> <input
							name="captureStart" id="captureStart"
							value="<fmt:formatDate value="${rule.rule.captureStart }" type="both" pattern="yyyy-MM-dd HH:mm:ss" />" />
						to <input name="captureEnd" id="captureEnd"
							value="<fmt:formatDate value="${rule.rule.captureEnd }" type="both" pattern="yyyy-MM-dd HH:mm:ss" />" /></p>

						<p><label for="captureStart">Retrieved from</label> <input
							name="retrievalStart" id="retrievalStart"
							value="<fmt:formatDate value="${rule.rule.retrievalStart }" type="both" pattern="yyyy-MM-dd HH:mm:ss"/>" />
						to <input name="retrievalEnd" id="retrievalEnd"
							value="<fmt:formatDate value="${rule.rule.retrievalEnd }" type="both" pattern="yyyy-MM-dd HH:mm:ss"/>" /></p>

						<p><label for="secondsSinceCapture">Applies after
						</label> <input name="secondsSinceCapture"
							id="secondsSinceCapture"
							value="<c:out value="${rule.rule.secondsSinceCapture }"/>" /> seconds since capture</p>

						<p><label for="policy">Policy:</label> <input name="policy"
							id="policy" value="<c:out value="${rule.rule.policy}"/>" /></p>
						<p><label for="privateComment">Private comment:</label><br />
							<textarea name="privateComment" rows="4" cols="50"><c:out value="${rule.rule.privateComment}" /></textarea>
						</p>	
						<p><label for="publicComment">Public comment:</label><br />
							<textarea name="publicComment" rows="4" cols="50"><c:out value="${rule.rule.publicComment}" /></textarea>
						</p>
						
						<div class="priButtons">
              <input type="submit" value="Save" name="saveRule" />
              <input type="submit" value="Cancel" name="cancel" />
            </div>
						<div class="altButtons">
						  <c:if test="${rule.rule.id != -1}">
					      <input type="submit" value="Delete Rule" name="delete" />
					    </c:if>
						</div>
						</fieldset>
						</form>
						<script type="text/javascript">
            setupDateRangeCalendars("captureStart", "captureEnd");
            setupDateRangeCalendars("retrievalStart", "retrievalEnd");            
            </script></td>
					</tr>
				</c:when>
				<c:otherwise>
					<tr class="rule<c:if test="${rule.inherited }"> inherited</c:if>"
						id="rule_<c:out value="${rule.rule.id}"/>">
						<td><c:out value="${rule.rule.surt}" /></td>
						<td><span class="date"
							title="<fmt:formatDate value="${rule.rule.captureStart }" type="both" pattern="yyyy-MM-dd HH:mm:ss" />"><fmt:formatDate
							value="${rule.rule.captureStart }" type="both"
							pattern="yyyy-MM-dd" /></span> to <span class="date"
							title="<fmt:formatDate value="${rule.rule.captureEnd }" type="both" pattern="yyyy-MM-dd HH:mm:ss" />"><fmt:formatDate
							value="${rule.rule.captureEnd }" type="both" pattern="yyyy-MM-dd" /></span></td>
						<td><span class="date"
							title="<fmt:formatDate value="${rule.rule.retrievalStart }" type="both" pattern="yyyy-MM-dd HH:mm:ss" />"><fmt:formatDate
							value="${rule.rule.retrievalStart }" type="both"
							pattern="yyyy-MM-dd" /></span> to <span class="date"
							title="<fmt:formatDate value="${rule.rule.retrievalEnd }" type="both" pattern="yyyy-MM-dd HH:mm:ss" />"><fmt:formatDate
							value="${rule.rule.retrievalEnd }" type="both"
							pattern="yyyy-MM-dd" /></span></td>
						<td><c:out value="${rule.rule.who}" /></td>
						<td><c:out value="${rule.rule.policy}" /></td>
						<td><a
							href="<c:url value="/admin?surt=${rule.encodedSurt}&amp;edit=${rule.rule.id}"/>">Edit</a></td>
					</tr>
				</c:otherwise>
			</c:choose>
		</c:forEach>
		<tr>
			<td colspan="6" class="newrule"><a
				href="<c:url value="/admin?surt=${encodedSurt}&amp;edit=new"/>">Add
			new rule</a></td>
		</tr>
	</tbody>
</table>
<%@ include file="footer.inc"%>
