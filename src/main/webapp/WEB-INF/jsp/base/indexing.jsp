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
<script>
function showIndexProcess() {
    $( document ).ready(function() {
        $("#content_info").hide();
        $("#content_index").show();
        $(".btnIndex").addClass("not-active");
        getState();
    });
}

function getState(){
    $.ajaxSetup({ cache: false });
    $.getJSON("../base/liveIndexState.json", {}, function(statusResponse){
          if(!statusResponse.isRunning){
            document.getElementById('dialog_done').style.display = '';
            $("#content_index").html(statusResponse.status.replace(/\n/g,"<br />"));
            $(".btnIndex").removeClass("not-active");
          } else {
            $("#content_index").html(statusResponse.status.replace(/\n/g,"<br />"));
            setTimeout(getState, 1000);
          }
    }, "text");
    $.ajaxSetup({ cache: true });
}
</script>
<c:if test="${started == 'true'}">
    <script>showIndexProcess();</script>
</c:if>
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
    
    <c:set var="active" value="indexing" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">Index erstellen</h1>
        <div class="controls">
            <a href="#" onclick="document.location='../base/scheduling.html';">Zurück</a>
            <a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
            <a href="#" onclick="document.location='../base/finish.html';">Überspringen</a>
            <a href="#" class="btnIndex" onclick="document.getElementById('indexing').submit();">Jetzt Indizieren</a>
        </div>
        <div class="controls cBottom">
            <a href="#" onclick="document.location='../base/scheduling.html';">Zurück</a>
            <a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
            <a href="#" onclick="document.location='../base/finish.html';">Überspringen</a>
            <a href="#" class="btnIndex" onclick="document.getElementById('indexing').submit();">Jetzt Indizieren</a>
        </div>
        <div id="content_info" class="status">
            <h2>Sie können Ihre Daten jetzt indizieren, um im Anschluss die Suche zu testen.</h2>
            <form action="../base/indexing.html" method="post" id="indexing">
                <table id="konfigForm">
                    <tr>
                        <td>
                            Abhängig von der Menge der Daten kann dieser Schritt einige Zeit in Anspruch nehmen.<br/>
                            Die Indizierung kann übersprungen werden, um automatisiert wie unter "Scheduling" angegeben zu erfolgen.
                        </td>
                    </tr>
                            
                </table>
            </form>     
        </div>
        
        <div id="content_index" class="status" style="display:none"></div>
        
        <div class="status" id="dialog_done" style="display:none">
            <div class="content">Die Daten wurden indexiert.</div>
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
