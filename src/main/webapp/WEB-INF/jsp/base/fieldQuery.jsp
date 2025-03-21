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
<script type="text/javascript">
    $(document).ready(function() {
        $("button[action], .submit").click(function() {
            // get form
            var form = $("#fieldQuery");
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
        
        $("#behaviour").change(function(value) {
            if ($(this).val() === "modify") {
                $('#rowIndexFieldName').show();
                $('#rowIndexFieldValue').show();
                $('#rowOption').show();
            } else {
                $('#rowIndexFieldName').hide();
                $('#rowIndexFieldValue').hide();
                $('#rowOption').hide();
            }
        });
    });
</script>
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
    
    <c:set var="active" value="fieldQuery" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">Verfügbarkeit der Ergebnisse</h1>
        <div class="controls">
            <a href="#" onclick="document.location='../base/provider.html';">Zurück</a>
            <a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
            <a href="#" class="submit">Weiter</a>
        </div>
        <div class="controls cBottom">
            <a href="#" onclick="document.location='../base/provider.html';">Zurück</a>
            <a href="#" onclick="document.location='../base/welcome.html';">Abbrechen</a>
            <a href="#" class="submit">Weiter</a>
        </div>
        <div id="content">
            <p>Hier haben Sie die Möglichkeit zu steuern, wie das iPlug auf Anfragen von unterschiedlichen iBussen reagieren soll.</p>
            <h2>Geben Sie Field Queries an</h2>
            <form:form method="post" action="../base/fieldQuery.html" modelAttribute="fieldQuery">
                 <input type="hidden" name="action" value="submit" />
                 <input type="hidden" name="id" value="" />
                <table id="konfigForm">
                    <tr>
                        <td class="leftCol">iBus:</td>
                        <td>
                            <div class="input full">
                                <form:select path="busUrl">
                                    <form:options items="${busUrls}" />
                                </form:select>
                            </div>
                            <span>Auf welchen iBus diese Regel angewendet werden soll.</span>
                            <form:errors path="busUrl" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">Regex:</td>
                        <td>
                            <div class="input full">
                                <form:input path="regex" />
                            </div>
                            <span>Der reguläre Ausruck, der die Anfrage erfüllen muss, damit die Regel angewedet wird.</span><form:errors path="regex" cssClass="error" element="div" /></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Verhalten:</td>
                        <td>
                            <div class="input full">
                                <select id="behaviour" name="behaviour">
                                    <option value="allow">Erlauben</option>
                                    <option value="deny">Ablehnen</option>
                                    <option value="modify" selected="selected">Modifizieren</option>
                                </select>
                            </div>
                            <span>Wie soll die Anfrage behandelt werden? Ablehnen mit 0 Treffern, Erlauben oder Modifizieren. Beim 'Erlauben' werden alle Anfragen verworfen, die nicht mit diesem regulären Ausdruck übereinstimmen! Werden mehrerer solcher 'Erlauben'-Regeln eingetragen, so müssen alle diese Regeln mit der Anfrage übereinstimmen, ansonsten wird die Anfrage abgewiesen.</span>
                        </td>
                    </tr>
                    <tr id="rowIndexFieldName">
                        <td class="leftCol">Index Feld Name:</td>
                        <td>
                            <div class="input full">
                                <form:input path="key" />
                            </div>
                            <span>Der Name des Feldes mit dem die Anfrage erweitert werden soll.</span><form:errors path="key" cssClass="error" element="div" /></td>
                    </tr>
                    <tr id="rowIndexFieldValue">
                        <td class="leftCol">Index Feld Wert:</td>
                        <td>
                            <div class="input full">
                                <form:input path="value" />
                            </div>
                            <span>Der Wert des Feldes mit dem die Anfrage erweitert werden soll.</span><form:errors path="value" cssClass="error" element="div" /></td>
                    </tr>
                    <tr id="rowOption">
                        <td class="leftCol">Option:</td>
                        <td>
                            <form:radiobutton path="option" value="prohibited" label="verboten"/>
                            <form:radiobutton path="option" value="required" label="erforderlich"/><br />
                            <span>Einstellung, ob das Feld erforderlich sein muss oder es nicht vorhanden sein darf.</span>
                            <form:errors path="option" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <hr />
                            <button type="button" action="add">Hinzufügen</button>
                        </td>
                    </tr>
                </table>

                <c:if test="${!empty fields}">
                    <h3>Vorhandene Field Queries</h3>
                    <table class="data">
                        <tr>
                            <th>iBus</th>
                            <th>Regex</th>
                            <th>Verhalten</th>
                            <th>Index Feld Name</th>
                            <th>Index Feld Wert</th>
                            <th>Option</th>
                            <th>&nbsp;</th>
                        </tr>
                        <c:set var="i" value="0" />
                        <c:forEach items="${fields}" var="field">
                           <tr>
                               <td>${field.busUrl}</td>
                               <td>${field.regex}</td>
                               <c:choose>
                                   <c:when test="${field.key == 'metainfo' && field.value == 'query_deny'}"><td colspan="4"><b>Diese Query komplett ablehnen</b></td></c:when>
                                   <c:when test="${field.key == 'metainfo' && field.value == 'query_allow'}"><td colspan="4"><b>Nur diese Query erlauben</b></td></c:when>
                                   <c:otherwise>
                                       <td>Modifizieren</td>
                                       <td>${field.key}</td>
                                       <td>${field.value}</td>
                                       <td>
                                           <c:choose>
                                               <c:when test="${field.prohibited}">verboten</c:when>
                                               <c:when test="${field.required}">erforderlich</c:when>
                                           </c:choose>
                                       </td>
                                   </c:otherwise>
                               </c:choose>
                               <td><button type="button" action="delete" id="${i}">Entfernen</button></td>
                           </tr>
                           <c:set var="i" value="${i + 1}" />
                        </c:forEach>
                    </table>
                </c:if>
                
            </form:form>
            
            <h4><u>Beispiele:</u></h4>
            <p style="margin-bottom: 30px;">Für Anfragen des iBus <i>"/test-bus:gross-gewaesser"</i>, die das Wort <i>"wasser"</i> enthalten, wollen sie NUR diejenigen Treffer zurückliefern, die im Index das Feld <i>"Typ"</i> den Wert <i>"See"</i> haben.<br />
            Demnach müssen sie folgende angaben machen: <b>iBus</b>: <i>/test-bus:gross-gewaesser</i>, <b>Regex</b>: <i>wasser</i>, <b>Index Feld Name</b>: <i>Typ</i>, <b>Index Feld Wert</b>: <i>See</i>, <b>Option</b>: <i>erforderlich</i>.<br />
            <br />
            Für Anfragen des iBus <i>"/test-bus:ansprechpartner"</i>, die <i>Telefonnummern</i> enthalten, wollen sie NICHT diejenigen Treffer zurückliefern, die im Index das Feld <i>"Freigabe"</i> den Wert <i>"privat"</i> haben.<br />
            Die Angaben könnten lauten: <b>iBus</b>: <i>/test-bus:messwerte</i>, <b>Regex</b>: <i>[0-9]+</i>, <b>Index Feld Name</b>: <i>Freigabe</i>, <b>Index Feld Wert</b>: <i>privat</i>, <b>Option</b>: <i>verboten</i>.</p>
            
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
