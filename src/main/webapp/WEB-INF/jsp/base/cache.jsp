<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
    
    <c:set var="active" value="cache" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">Caching Einstellungen</h1>
        <div id="content">
            <br />
	        <form:form action="../base/cache.html" method="post" modelAttribute="cache">
                <table id="konfigForm">
                    <tr>
                        <td class="leftCol">Cache:</td>
                        <td><form:radiobutton path="active" value="true" label="aktiviert" /><form:radiobutton path="active" value="false" label="deaktiviert" /></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Cache-Time (in min):</td>
                        <td>
                            <div class="input full">
                                <form:input path="lifeTime" />
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">Cache-Elements:</td>
                        <td>
                            <div class="input full">
                                <form:input path="elementsCount"/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">Cache-Speicher:</td>
                        <td><form:radiobutton path="diskStore" value="false" label="RAM" /><form:radiobutton path="diskStore" value="true" label="Festplatte" /></td>
                    </tr>
                    <tr>
                        <td class="leftCol">&nbsp;</td>
                        <td><input type="submit" value="Cache bearbeiten" /></td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
