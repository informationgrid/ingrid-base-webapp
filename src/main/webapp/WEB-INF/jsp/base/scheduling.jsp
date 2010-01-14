<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="de.ingrid.admin.security.IngridPrincipal"%><html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>Portal U Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
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
	
	<c:set var="active" value="scheduling" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Scheduling</h1>
		<div class="controls">
			<a href="#" onclick="document.location='save.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='indexing.html';">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='save.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='indexing.html';">Weiter</a>
		</div>
		<div id="content">
		    <c:if test="${!empty pattern}">
			    <form method="post" action="deletePattern.html">
			        <label>Pattern:</label>
	                <span><b>${pattern}</b></span> <input type="submit" value="l�schen" />
	            </form><br />
            </c:if>
            
			<h2>Geben Sie an, in welchem Zeitabstand Ihre Daten automatisch neu indexiert werden sollen</h2>
			<form method="post" action="scheduling.html" id="scheduling">
				<c:set var="freq" value="${paramValues['freq'][0]}"/>

				<ul class="tabs">
					<li <c:if test="${empty freq}">class="active"</c:if>><a href="scheduling.html">T�glich</a></li>
					<li <c:if test="${freq == 'weekly'}">class="active"</c:if>><a href="scheduling.html?freq=weekly">W�chentlich</a></li>
					<li <c:if test="${freq == 'monthly'}">class="active"</c:if>><a href="scheduling.html?freq=monthly">Monatlich</a></li>
					<li <c:if test="${freq == 'advanced'}">class="active"</c:if>><a href="scheduling.html?freq=advanced">Erweitert</a></li>
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
			            <td class="leftCol"></td>
				        <td><input type="submit" value="speichern" /></td>
				    </tr>
			    </table>
			</form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>