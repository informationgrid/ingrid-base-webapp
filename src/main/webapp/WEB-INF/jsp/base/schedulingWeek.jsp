<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<table id="konfigForm">
	<tr>
		<td class="leftCol">Stunde:</td>
		<td>
			<select name="hour">
				<c:forEach var="i" begin="1" end="12">
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
	<tr>
		<td class="leftCol">Tageszeit:</td>
		<td>
			<select name="daytime">
				<option value="am">AM</option>
				<option value="am">PM</option>
			</select>
		</td>
	</tr>
	<tr>
		<td class="leftCol">Wochentag:</td>
		<td>
			<select name="daytime">
				<option value="">Montag</option>
				<option value="">Dienstag</option>
				<option value="">Mittwoch</option>
				<option value="">Donnerstag</option>
				<option value="">Freitag</option>
				<option value="">Samstag</option>
				<option value="">Sonntag</option>
			</select>
		</td>
	</tr>
</table>