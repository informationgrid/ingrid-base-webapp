<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
			<div id="language"><a href="../base/auth/logout.html">Logout</a></div>
		<%
		}
		%>
		
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">iPlug muss neu gestartet werden!</h1>
		<div id="content" style="position: relative; text-align:center; top:100px">
			<p>
			    <h2>Ihre Angaben wurden gespeichert. Da der Port der Administrationsseite ge�ndert wurde,
			    m�ssen Sie nun das iPlug neu starten.</h2>
			    Rufen Sie daf�r "sh start.sh restart" von der Kommandozeile im Wurzelverzeichnis des iPlugs auf!
			</p>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
