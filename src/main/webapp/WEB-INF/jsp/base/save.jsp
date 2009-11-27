<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="de.ingrid.admin.security.IngridPrincipal.SuperAdmin"%>
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
			<div id="language"><a href="<%=request.getContextPath()%>/base/auth/logout.html">Logout</a></div>
		<%
		}
		%>
		
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<c:set var="active" value="save" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Konfiguration Speichern</h1>
		<div class="controls">
			<a href="#" onclick="history.back();">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('save').submit();">Speichern</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="history.back();">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('save').submit();">Speichern</a>
		</div>
		<div id="content" style="position: relative; text-align:center; top:100px">
			<form:form action="save.html" method="post" id="save">
			     <button type="button" onclick="document.getElementById('save').submit();">Speichern</button>
			     <p>
			         <h2>Die Konfiguration ist abgeschlossen. Ihre Angaben werden gespeichert.</h2>
			     </p>
			</form:form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>