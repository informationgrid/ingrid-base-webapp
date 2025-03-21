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
    
    <c:set var="active" value="commSetup" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">Kommunikations Setup</h1>
        <div id="content">
            <br />
	        <p>Hier sehen sie den Status der Verbindung zu den iBus(sen).</p>
            
            <span><b>Status:</b>
	            <c:choose>
	                <c:when test="${connected}"><span class="success">verbunden</span></c:when>
		            <c:otherwise><span class="error">nicht verbunden</span></c:otherwise>
	            </c:choose>
            </span><br />
            <br />
            <c:choose>
                <c:when test="${connected}">
                    <form action="../base/commSetup.html" method="post">
                        <input type="hidden" name="action" value="shutdown" />
                        <input type="submit" name="submit" value="Verbindung trennen" />
                    </form><br />
                    <form action="../base/commSetup.html" method="post">
                        <input type="hidden" name="action" value="restart" />
                        <input type="submit" name="submit" value="Verbindung neu starten" />
                    </form>
                 </c:when>
                 <c:otherwise>
                    <form action="../base/commSetup.html" method="post">
                        <input type="hidden" name="action" value="start" />
                        <input type="submit" name="submit" value="Verbindung starten" />
                    </form>
                 </c:otherwise>
             </c:choose>
             <br />
             <p>Sie haben die Möglichkeit die Verbindung zu trennen, aufzubauen und neu zu starten.
             <br />Üblicherweise sollte hier "<span class="success">verbunden</span>" zu sehen sein.<br />
             Ist dies nicht der Falll, sondern erscheint hier "<span class="error">nicht verbunden</span>", besteht mindestens zu einem der iBusse keine Verbindung.</p>
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
