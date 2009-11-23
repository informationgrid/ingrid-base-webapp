<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<tr>
    <td class="leftCol">Tag im Monat:</td>
    <td>
        <select name="dayOfMonth">
            <c:forEach var="i" begin="1" end="31">
                <option value="${i}">${i}.</option>
            </c:forEach>
           </select><br />
           <span>Der Tag im Monat, an dem der Prozess starten soll.</span>
       </td>
   </tr>
<tr>
	<td class="leftCol">Stunde:</td>
	<td>
		<select name="hour">
			<c:forEach var="i" begin="0" end="23">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select><br />
        <span>Die Stunde des Tages, an dem der Prozess starten soll.</span>
	</td>
</tr>
<tr>
	<td class="leftCol">Minute:</td>
	<td>
		<select name="minute">
			<c:forEach var="i" begin="0" end="59" step="15">
				<option value="${i}">${i}</option>
			</c:forEach>
		</select><br />
           <span>Die Minute der Stunde, an dem der Prozess starten soll.</span>
	</td>
</tr>