<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<tr>
	<td class="leftCol">Stunde:</td>
	<td>
		<select name="hour">
			<c:forEach var="i" begin="0" end="23">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select>
	</td>
	<td class="rightCol">Die Stunde der Uhrzeit, an dem der Prozess starten soll.</td>
</tr>
<tr>
	<td class="leftCol">Minute:</td>
	<td>
		<select name="minute">
			<c:forEach var="i" begin="0" end="59" step="15">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select>
	</td>
	<td class="rightCol">Die Minute der Uhrzeit, an dem der Prozess starten soll.</td>
</tr>