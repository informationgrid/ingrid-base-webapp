<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2025 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.2 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  https://joinup.ec.europa.eu/software/page/eupl
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  --%>
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<tr>
	<td class="leftCol">Tag und Uhrzeit:</td>
	<td>
		<label>am&nbsp;</label>
        <div class="input inline">
		<select class="auto" name="dayOfMonth">
            <c:forEach var="i" begin="1" end="31">
                <option value="${i}">${i}.</option>
            </c:forEach>
        </select>
        </div>
        <label>&nbsp;Tag des Monats um&nbsp;</label>

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
<tr>
	<td class="leftCol">An mehreren Tagen:</td>
	<td>
		<input type="hidden" name="_dayOfMonth" value=""/>
		<table id="multiple">
			<c:forEach var="w" begin="0" end="4">
				<tr>
					<c:forEach var="d" begin="1" end="7">
						<c:choose>
							<c:when test="${w == 4}">
								<c:if test="${d <= 3}">
									<td><div value="${w*7 + d}"></div></td>
								</c:if>
							</c:when>
							<c:otherwise>
								<td><div value="${w*7 + d}"></div></td>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</td>
</tr>
