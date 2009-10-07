<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<tr>
    <td class="leftCol">Wochentag:</td>
    <td>
        <select name="dayOfWeek">
            <option value="1">Montag</option>
            <option value="2">Dienstag</option>
            <option value="3">Mittwoch</option>
            <option value="4">Donnerstag</option>
            <option value="5">Freitag</option>
            <option value="6">Samstag</option>
            <option value="7">Sonntag</option>
        </select>
    </td>
</tr>
<tr>
	<td class="leftCol">Stunde:</td>
	<td>
		<select name="hour">
			<c:forEach var="i" begin="0" end="23">
                <option value="${i}">${i}</option>
            </c:forEach>
		</select>
	</td>
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
</tr>