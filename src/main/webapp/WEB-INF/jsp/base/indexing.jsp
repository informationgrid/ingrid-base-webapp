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
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
		<h1>Konfiguration</h1>
		<div id="language"><a href="#">Englisch</a></div>
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<c:set var="active" value="indexing" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Index erstellen</h1>
		<div class="controls">
			<a href="#" onclick="document.location='welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='finish.html';">Überspringen</a>
			<a href="#" onclick="document.getElementById('indexing').submit();">Jetzt Indizieren</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='finish.html';">Überspringen</a>
			<a href="#" onclick="document.getElementById('indexing').submit();">Jetzt Indizieren</a>
		</div>
		<div id="content">
			<h2>Sie können Ihre Daten jetzt indizieren, um im Anschluß die Suche zu testen.</h2>
			<form action="indexing.html" method="post" id="indexing">
				<table id="konfigForm">
					<tr>
						<td>
							Abhängig von der Menge der Daten kann dieser Schritt einige Zeit in Anspruch nehmen.<br/>
							Die Indizierung kann übersprungen werden, um automatisiert wie unter "Scheduling" angegeben zu erfolgen.
							
							<br/><br/>
							
							Index Status: ${state}<br/>
							Anzahl der zu indizierenden Dokumente: ${count}
							
							<br/>todo: link back scheduling
						</td>
					</tr>
							
				</table>
			</form> 	
		</div>
		
		<div class="dialog">
			<div class="content">Daten werden indiziert. Bitte haben Sie Geduld.</div>
		</div>	
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>