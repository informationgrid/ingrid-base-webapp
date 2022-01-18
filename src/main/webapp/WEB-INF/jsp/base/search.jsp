<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="de.ingrid.admin.security.IngridPrincipal"%><html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>InGrid iPlug Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" alt="InGrid" />
		<h1>Konfiguration</h1>
		<%
		java.security.Principal  principal = request.getUserPrincipal();
		if(principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
		%>
			<div id="language"><a href="../base/auth/logout.html">Logout</a></div>
		<%
		}
		%>
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<c:set var="active" value="search" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Suche testen</h1>
		<div id="content">
			<br />
			<h2>Sie können das iPlug jetzt testen. Geben Sie dazu eine Suchanfrage ein!</h2>
			<form method="get" action="../base/search.html">
				<table id="konfigForm">
					<tr>
						<td class="leftCol">Suchbegriff:</td>
						<td>
                            <div class="input full">
                                <input type="text" name="query" value=""/>
                            </div>
                        </td>
					</tr>
					<tr>
						<td class="leftCol">&nbsp;</td>
						<td><input type="submit" value="Suchen"/></td>
					</tr>
				</table>
			</form>
			
			<c:if test="${!empty hits}">
				<div class="hitCount">Ergebnisse 1-${hitCount} von ${totalHitCount} für "${query}"</div>
				
				<c:forEach items="${hits}" var="hit">
					<div class="searchResult">
					   <h3>
					       <c:choose>
					           <c:when test="${details}">
<%-- 								   <a href="../base/searchDetails.html?id=${hit.key}">${hit.value['title']}</a> --%>
                                    ${hit.value['title']} (<a href="../base/searchDetails.html?id=${hit.key}">raw result</a>) 
					           </c:when>
                               <c:when test="${hit.value['url'] != null && hit.value['url'] != ''}">
                                   <a href="${hit.value['url']}">${hit.value['title']}</a>
                               </c:when>
					           <c:otherwise>
					               <a href="#">${hit.value['title']}</a>
					           </c:otherwise>
					       </c:choose>
					   </h3>
					   <span>${hit.value['abstract']}</span>
					</div>
				</c:forEach>
				<br /><br />
			</c:if>
			
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
