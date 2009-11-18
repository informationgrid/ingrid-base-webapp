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
<script type="text/javascript" src="../js/base/jquery-1.3.2.min.js"></script>
<script type="text/javascript">
	var map = ${jsonMap};

    $(document).ready(function() {
                
        $("#partners").change(function() {
        	var select = $("#providers");
        	select.find("option[value]").remove();
            var partner = $(this).val();
            // at first call it's normally empty / not selected
            if (map[partner]) {
                for(var i = 0; i < map[partner].length; i++) {
                    select.append("<option value='" + map[partner][i].shortName + "'>" + map[partner][i].displayName + "</option>");
                }
            }
        }).trigger('change');
        
        $("#providers").val("${plugDescription.organisationAbbr}");
        
    });
</script> 
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
	
	<c:set var="active" value="general" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Angaben zu Betreiber und Datenquelle</h1>
		<div class="controls">
			<a href="#" onclick="document.location='workingDir.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='workingDir.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div id="content">
			<h2>Allgemeine Angaben zum Betreiber</h2>
			<form:form method="post" action="general.html" modelAttribute="plugDescription"> 
			    <c:if test="${isIgc}"><form:hidden path="proxyServiceURL" /></c:if>
				<table id="konfigForm">
					<tr>
						<td>Partner:</td>
						<td>
							<form:select path="organisation" id="partners"> 
                                <form:option value="" label="bitte w�hlen" />
                                <form:options items="${partners}" itemValue="shortName" itemLabel="displayName" /> 
                            </form:select>
                            <form:errors path="organisation" cssClass="error" element="div" />
						</td>
						<td class="rightCol">Der Partner der dieses IPlug betreibt.</td>
					</tr>
					<tr>
						<td class="leftCol">Name des Anbieters:</td>
						<td>
							 <form:select path="organisationAbbr" id="providers" > 
                                <form:option value="" label="bitte w�hlen" /> 
                            </form:select>
                            <form:errors path="organisationAbbr" cssClass="error" element="div" />
						</td>
						<td class="rightCol">Der Anbieter der dieses IPlug betreibt.</td>
					</tr>
					<tr>
						<td colspan="3"><h3>Ansprechpartner:</h3></td>
					</tr>
					<tr>
						<td>Titel:</td>
						<td>
							<form:input path="personTitle" />
						</td>
						<td class="rightCol">Der Titel des Ansprechpartners (optional).</td>
					</tr> 
					<tr>  
						<td>Nachname:</td>
						<td><form:input path="personSureName" /><form:errors path="personSureName" cssClass="error" element="div" /></td>
						<td class="rightCol">Der Name des Ansprechpartners.</td>
					</tr> 
					<tr>  
						<td>Vorname:</td>
						<td><form:input path="personName" /><form:errors path="personName" cssClass="error" element="div" /></td>
						<td class="rightCol">Der Vorname des Ansprechpartners.</td>
					</tr>
					<tr>
						<td>Telefon:</td>
						<td><form:input path="personPhone" /><form:errors path="personPhone" cssClass="error" element="div" /></td>
						<td class="rightCol">Die Telefonnummer unter der der Ansprechpartner erreichbar ist.</td>
					</tr>
					<tr>
						<td>E-Mail:</td>
						<td><form:input path="personMail" /><form:errors path="personMail" cssClass="error" element="div" /></td>
						<td class="rightCol">Die E-Mail Adresse des Ansprechpartners.</td>
					</tr>
					<tr>
						<td colspan="3"><h3>Datenquelle:</h3></td>
					</tr>					
					<tr>
						<td>Name der Datenquelle:</td>
						<td><form:input path="dataSourceName" /><form:errors path="dataSourceName" cssClass="error" element="div" /></td>
						<td class="rightCol">Der Name der die Datenquelle bezeichnet.</td>
					</tr>
					<tr>
						<td>Kurzbeschreibung:</td>
						<td><form:textarea path="dataSourceDescription" /></td>
						<td class="rightCol">Eine kurze Beschreibung des Inhalts und der Anforderung der Datenquelle (optional).</td>
					</tr>
					<tr>
						<td>Art der Datenquelle:</td>
						<td>
						    <c:forEach items="${dataTypes}" var="type">
						        <form:checkbox path="dataTypes" value="${type.name}" /><fmt:message key="dataType.${type.name}"/><br />
						    </c:forEach>
						    <form:errors path="dataTypes" cssClass="error" element="div" />
						</td>
						<td class="rightCol">Der Datenquellen Typ (mehrere Felder ausw�hlbar).</td>
					</tr>
					<c:if test="${isIgc}">
						<tr>
							<td colspan="3"><h3>IPlug:</h3></td>
						</tr>
						<tr>
							<td>Adresse des iPlugs:</td>
							<td><form:input path="proxyServiceURL" readonly="true" /><form:errors path="proxyServiceURL" cssClass="error" element="div" /><br/>/&lt;Gruppen Name&gt;:&lt;IPlug Name&gt;</td>
							<td class="rightCol">Der bereits angegebene Name des IPlugs.</td>
						</tr>
						<tr>
							<td>Adresse des korrespondierenden IPlugs:</td>
							<td><form:input path="correspondentProxyServiceURL" /><form:errors path="correspondentProxyServiceURL" cssClass="error" element="div" /><br/>/&lt;Gruppen Name&gt;:&lt;IPlug Name&gt;</td>
							<td class="rightCol">Name des korrespondierenden IPlugs</td>
						</tr>
					</c:if>
					<tr>
						<td colspan="2"><h3>Administrationsinterface:</h3></td>
					</tr>					
					<tr>
						<td>URL:</td>
						<td><form:input path="iplugAdminGuiUrl" /><form:errors path="iplugAdminGuiUrl" cssClass="error" element="div" /></td>
						<td class="rightCol">Die Adresse unter der dieses Administrationsinterface erreichbar sein soll.</td>
					</tr>
					<tr>
						<td>Port:</td>
						<td><form:input path="iplugAdminGuiPort" /><form:errors path="iplugAdminGuiPort" cssClass="error" element="div" /></td>
						<td class="rightCol">Der Port unter der dieses Administrationsinterface erreichbar sein soll.</td>
					</tr>
					<tr>
						<td>Administrationskennwort:</td>
						<td><input type="password" name="iplugAdminPassword" value="${plugDescription['IPLUG_ADMIN_PASSWORD']}" /><form:errors path="iplugAdminPassword" cssClass="error" element="div" /></td>
						<td class="rightCol">Das Kennwort mit dessen Hilfe man sich Authentifiziert.</td>
					</tr>			
				</table>
			</form:form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>