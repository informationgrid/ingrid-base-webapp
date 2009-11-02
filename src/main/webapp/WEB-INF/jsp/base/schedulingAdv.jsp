<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<tr>
	<td class="leftCol">Cron Pattern:</td>
	<td>
	    <c:if test="${empty pattern}">
	      <c:set var="pattern" value="* * * * *" />
	    </c:if>
		<input type="text" name="pattern" value="${pattern}" />
		<br/>
		<label>Minute(0-59) Stunde(0-23) Tag(1-31) Monat(1-12) Wochetag(1-7)</label>
	</td>
</tr>