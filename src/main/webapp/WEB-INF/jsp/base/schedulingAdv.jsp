<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2018 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  http://ec.europa.eu/idabc/eupl5
  
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
	<td class="leftCol">Cron Pattern:</td>
	<td>
	    <c:if test="${empty pattern}">
	      <c:set var="pattern" value="* * * * *" />
	    </c:if>
        <div class="input full">
		  <input type="text" name="pattern" value="${pattern}" />
        </div>
		<br/>
		<label>Minute(0-59) Stunde(0-23) Tag(1-31) Monat(0-11) Wochetag(1-7)</label>
		<br />
		<span>Ein  erweitertes Cron Patter. Weitere Informationen <a target="_blank" href="http://help.sap.com/saphelp_xmii120/helpdata/de/44/89a17188cc6fb5e10000000a155369/content.htm">hier</a>.</span>
	</td>
</tr>
