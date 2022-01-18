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
<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $("button[action], .submit").click(function() {
        	// get form
            var form = $("#communication");
            // get button action
            var action = $(this).attr("action");
            // set request action
            if(action) {
                form.find("input[name='action']").val(action);
            }
            // get button id
            var id = $(this).attr("id");
            // set request id
            if(id) {
                form.find("input[name='id']").val(id);
            }
            // submit form
            form.submit();
        });
    });
</script>
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
	
	<c:set var="active" value="communication" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Hinzufügen des iBus</h1>
		<div class="controls">
			<a href="#" onclick="document.location='../base/welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='../base/welcome.html';" class="submit">Speichern und Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='../base/welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='../base/welcome.html';" class="submit">Speichern und Weiter</a>
		</div>
		<div id="content">
		
            <c:if test="${noBus}">
                <div class="error">
                    <p>Es konnte keine Verbindung zu einem der hinzugefügten Busse hergestellt werden.
                    Es muss mindestens der Standard-iBus verbunden sein, um die Konfiguration erfolgreich abzuschließen.</p>
                    <p>Bitte überprüfen Sie Ihre Angaben.</p>
                    <button onclick="document.location='workingDir.html';">Offline fortfahren</button>
                </div>
                <br />
            </c:if>
		
            <form:form method="post" action="../base/communication.html" modelAttribute="communication">
                <input type="hidden" name="action" value="submit" />
                <input type="hidden" name="id" value="" />
                <table id="konfigForm">
                    <tr>
                        <td colspan="2"><h3>Ihr iPlug:</h3></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Eigene Proxy Service Url:</td>
                        <td>
                            <div class="input full">
                                <input type="text" name="proxyServiceUrl" value="${communication.proxyServiceUrl}" /><br />
                            </div>
                            <span>Der Name mit dem man diesen iPlug identifiziert.</span>
                            <form:errors path="proxyServiceUrl" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2"><h3>iBus Angaben:</h3></td>
                    </tr>
                    <tr>
                        <td class="leftCol">iBus Proxy Service Url:</td>
                        <td>
                            <div class="input full">
                                <input type="text" name="busProxyServiceUrl" value="${communication.busProxyServiceUrl}" /><br />
                            </div>
                            <span>Der Name des iBus mit dem sich der iPlug verbinden soll.</span>
                            <form:errors path="busProxyServiceUrl" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">IP:</td>
                        <td>
                            <div class="input full">
                                <input type="text" name="ip" value="${communication.ip}" /><br />
                            </div>
                            <span>Die IP-Adresse des iBus.</span>
                            <form:errors path="ip" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">Port:</td>
                        <td>
                            <div class="input full">
                                <input type="text" name="port" value="${communication.port}" /><br />
                            </div>
                            <span>Der Port des iBus.</span>
                            <form:errors path="port" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">&nbsp;</td>
                        <td><button type="button" action="add"><c:choose><c:when test="${!empty busses}">Hinzufügen</c:when><c:otherwise>Speichern</c:otherwise></c:choose></button></td>
                    </tr>
                </table>
                
	            <c:if test="${!empty busses}">
	                   <h3>Vorhandene iBusse</h3>
		              <table class="data">
		                  <tr>
		                      <th>iBus Url</th>
		                      <th>IP</th>
		                      <th>Port</th>
		                      <th>Connected</th>
		                      <th>&nbsp;</th>
		                      <th>&nbsp;</th>
		                  </tr>
		                  <c:set var="busIndex" value="0" />
		                  <c:forEach items="${busses}" var="bus">
		                      <tr>
		                          <td>${bus.busProxyServiceUrl}</td>
		                          <td>${bus.ip}</td>
		                          <td>${bus.port}</td>
		                          <td>${bus.isConnected}</td>
		                          <td>
		                              <c:choose>
		                                  <c:when test="${busIndex == 0}">(Standard)</c:when>
		                                  <c:otherwise><button type="button" action="set" id="${busIndex}"/>Als Standard</button></c:otherwise>
		                              </c:choose>
		                          </td>
		                          <td><button type="button" action="delete" id="${busIndex}"/>Löschen</button></td>
		                      </tr>
		                      <c:set var="busIndex" value="${busIndex + 1}" />
		                  </c:forEach>
		              </table>
	            </c:if>
            </form:form>
		
	    </div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
