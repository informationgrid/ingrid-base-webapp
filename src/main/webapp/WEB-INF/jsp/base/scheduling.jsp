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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>InGrid iPlug Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript"><!--
	$(document).ready(function() {
		var divs = $("table#multiple div[value]").each(function() {
			var el = $(this);
			el.text(el.attr("value") + ".");
			el.addClass("value");
		}).toggle(function() {
			$(this).addClass("marked");
		}, function() {
			$(this).removeClass("marked");
		});

		var form = $("form#submitform").submit(function() {
			var marked = divs.filter(".marked");
			if (marked.size() > 0) {
				var value = "";
				marked.each(function(i) {
					value += $(this).attr("value");
					if (i < (marked.size() - 1)) value += ",";
				});
				$("input[name='_dayOfMonth']").attr("name", "dayOfMonth").val(value);
				$("select[name='dayOfMonth']").attr("name", "_dayOfMonth");
			}
		});
	});
// --></script>
</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" alt="InGrid" />
		<h1>Konfiguration</h1>
		<security:authorize access="isAuthenticated()">
			<div id="language"><a href="../base/auth/logout.html">Logout</a></div>
		</security:authorize>
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<c:set var="active" value="scheduling" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Scheduling</h1>
		<div class="controls">
			<a href="#" onclick="document.location='../base/save.html';">Zurück</a>
			<a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='../base/indexing.html';">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='../base/save.html';">Zurück</a>
			<a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='../base/indexing.html';">Weiter</a>
		</div>
		<div id="content">
		    <c:if test="${!empty pattern}">
			    <form method="post" action="../base/deletePattern.html">
			        <label>Pattern:</label>
	                <span><b>${pattern}</b></span> <input type="submit" value="löschen" />
	            </form><br />
            </c:if>
            
            <c:if test="${!empty error}">
                <div class="error" ><fmt:message key="Scheduling.pattern.invalid" /></div>
            </c:if>
            
			<h2>Geben Sie an, in welchem Zeitabstand Ihre Daten automatisch neu indexiert werden sollen</h2>
			<form id="submitform" method="post" action="../base/scheduling.html" id="scheduling">
				<c:set var="freq" value="${paramValues['freq'][0]}"/>

				<ul class="tabs">
					<li <c:if test="${empty freq}">class="active"</c:if>><a href="../base/scheduling.html">Täglich</a></li>
					<li <c:if test="${freq == 'weekly'}">class="active"</c:if>><a href="../base/scheduling.html?freq=weekly">Wöchentlich</a></li>
					<li <c:if test="${freq == 'monthly'}">class="active"</c:if>><a href="../base/scheduling.html?freq=monthly">Monatlich</a></li>
					<li <c:if test="${freq == 'advanced'}">class="active"</c:if>><a href="../base/scheduling.html?freq=advanced">Erweitert</a></li>
				</ul>
				
				<table id="konfigForm" style="clear:both">
					<c:choose>
						<c:when test="${freq == 'weekly'}">
							<c:import url="schedulingWeek.jsp"/>
						</c:when>
						<c:when test="${freq == 'monthly'}">
							<c:import url="schedulingMonth.jsp"/>
						</c:when>
						<c:when test="${freq == 'advanced'}">
							<c:import url="schedulingAdv.jsp"/>
						</c:when>
						<c:otherwise>
							<c:import url="schedulingDay.jsp"/>
						</c:otherwise>
					</c:choose>
					<tr>
			            <td class="leftCol">&nbsp;</td>
			            <td><input type="submit" value="speichern" /></td>
				    </tr>
			    </table>
			</form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
