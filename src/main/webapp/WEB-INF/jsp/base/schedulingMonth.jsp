<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<tr>
	<td class="leftCol">Tag und Uhrzeit:</td>
	<td>
		<label>am&nbsp;</label>
		<select class="auto" name="dayOfMonth">
            <c:forEach var="i" begin="1" end="31">
                <option value="${i}">${i}.</option>
            </c:forEach>
        </select>
        <label>&nbsp;Tag des Monats um&nbsp;</label>
	
		<select class="auto" name="hour">
			<c:forEach var="i" begin="0" end="23">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select>
		<label>:</label>
		
		<select class="auto" name="minute">
			<c:forEach var="i" begin="0" end="59" step="15">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select>
		<label>&nbsp;Uhr</label>
	</td>
</tr>