<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>Portal U Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/portal_u.css" type="text/css" media="all" />
<script type="text/javascript">
 var map = ${jsonMap};
 
 function updateProviders() {
  var select = document.getElementById('provider');
  reset(select);
  var partner = document.getElementById('partner').value;
  for(var i = 0; i < map[partner].length; i++) {
  	select.options[select.length] = new Option(map[partner][i].displayName, map[partner][0].shortName);
  }
 }

 function reset(select) {
  for(var i = (select.length - 1); i > 0; i--) {
   select.options[i] = null;
  }
 }
</script> 
</head>
<body>
	<div id="header">
		<img src="../images/logo.gif" width="168" height="60" alt="Portal U" />
		<h1>Konfiguration</h1>
		<div id="language"><a href="#">Englisch</a></div>
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
			<p> Mit * gezeichnete Felder sind optional</p>
			<h2>Allgemeine Angaben zum Betreiber</h2>
			<form:form method="post" action="general.html" modelAttribute="plugDescription"> 
				<table id="konfigForm">
					<tr>
						<td>Partner:</td>
						<td>
							<form:select path="partner" onchange="updateProviders();"> 
                                <form:option value="-" label="bitte w�hlen" />
                                <form:options items="${partners}" itemValue="shortName" itemLabel="displayName" /> 
                            </form:select>
						</td>
					</tr>
					<tr>
						<td class="leftCol">Name des Anbieters:</td>
						<td>
							 <form:select path="provider"> 
                                <form:option value="-" label="bitte w�hlen" /> 
                            </form:select>
						</td>
					</tr>
					<tr>
						<td colspan="2"><h3>Ansprechpartner:</h3></td>
					</tr>
					<tr>
						<td>Titel*:</td>
						<td>
							<form:input path="personTitle" />
						</td>
					</tr> 
					<tr>  
						<td>Nachname:</td>
						<td><form:input path="personSureName" /></td>
					</tr> 
					<tr>  
						<td>Vorname:</td>
						<td><form:input path="personName" /></td>
					</tr>
					<tr>
						<td>Telefon:</td>
						<td><form:input path="personPhone" /></td>
					</tr>
					<tr>
						<td>E-Mail:</td>
						<td><form:input path="personMail" /></td>
					</tr>
					<tr>
						<td colspan="2"><h3>Datenquelle:</h3></td>
					</tr>					
					<tr>
						<td>Name der Datenquelle:</td>
						<td><form:input path="dataSourceName" /></td>
					</tr>
					<tr>
						<td>Kurzbeschreibung*:</td>
						<td><form:textarea path="dataSourceDescription" /></td>
					</tr>
					<tr>
						<td>Art der Datenquelle:</td>
						<td>
						 	<table class="check">
						 		<tr>
						 			<td>
									 	<form:checkboxes items="${datatypes}" path="dataTypes" itemLabel="displayName" itemValue="name" />
						 			</td>
						 		</tr>
						 	</table>
						 	<!-- table class="check">
						 		<tr>
						 			<td><input type="checkbox" name="date" value="januar_29" /> UDK/UOK</td>
						 			<td><input type="checkbox" name="date" value="januar_29" /> Forschungsdaenbank</td>
						 			<td><input type="checkbox" name="date" value="januar_29" /> Metadatenbank</td>						 									 			
						 		</tr>
						 		<tr>
						 			<td><input type="checkbox" name="date" value="januar_29" /> UDK Adressen</td>
						 			<td><input type="checkbox" name="date" value="januar_29" /> Forschungsdaenbank</td>
						 			<td><input type="checkbox" name="date" value="januar_29" /> Metadatenbank</td>						 									 			
						 		</tr>
						 		<tr>
						 			<td><input type="checkbox" name="date" value="januar_29" /> CSW</td>
						 			<td><input type="checkbox" name="date" value="januar_29" /> Forschungsdaenbank</td>
						 			<td><input type="checkbox" name="date" value="januar_29" /> Metadatenbank</td>						 									 			
						 		</tr>						 								 		
							</table-->
						</td>
					</tr>
					<tr>
						<td colspan="2"><h3>Iplug:</h3></td>
					</tr>
					<tr>
						<td>Adresse des iPlugs:</td>
						<td><form:input path="proxyServiceURL" /><br/>/&lt;Gruppen Name&gt;:&lt;IPlug Name&gt;</td>
					</tr>
					<tr>
						<td>Adresse des korrespondierenden iPlugs:</td>
						<td><form:input path="correspondentProxyServiceURL" /><br/>/&lt;Gruppen Name&gt;:&lt;IPlug Name&gt;</td>
					</tr>
					<tr>
						<td colspan="2"><h3>Administrationsinterface:</h3></td>
					</tr>					
					<tr>
						<td>URL:</td>
						<td><form:input path="iplugAdminGuiUrl" /></td>
					</tr>
					<tr>
						<td>Port:</td>
						<td><form:input path="iplugAdminGuiPort" /></td>
					</tr>
					<tr>
						<td>Administrationskennwort:</td>
						<td><form:password path="iplugAdminPassword" /> </td>
					</tr>			
				</table>
			</form:form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>