<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<tr>
	<td class="leftCol">Uhrzeit:</td>
	<td>
		<label>um&nbsp;</label>
        <div class="input inline">
		<select class="auto" name="hour">
			<c:forEach var="i" begin="0" end="23">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select>
        </div>
		<label>:</label>
		
        <div class="input inline">
		<select class="auto" name="minute">
			<c:forEach var="i" begin="0" end="59" step="15">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select>
        </div>
		<label>&nbsp;Uhr</label>
	</td>
</tr>