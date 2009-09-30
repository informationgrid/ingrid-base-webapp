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
	
	<c:set var="active" value="heartbeat" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Mit IBus verbinden / abmelden</h1>
		<div class="controls">
			<a href="#" onclick="document.location='save.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='welcome.html';">Abschließen</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='save.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='welcome.html';">Abschließen</a>
		</div>
		<div id="content">
			<h2>Die Kommunikation wird neu gestartet, Ihre Einstellungen zum IBus übertragen und Ihr IPlug angemeldet.</h2>
			<c:if test="${heartBeat.beatable}">
			<form action="heartbeat.html" method="POST" id="heartbeat">
				<table id="konfigForm">
					<tr>
					<c:choose>
						<c:when test="${!heartBeat.enable}">
							<td>
								<input type="hidden" name="start" value="true">
								IPlug nicht verbunden<br/><br/>
								<input type="submit" value="Anmelden"/>
							</td>
						</c:when>
						<c:otherwise>
							<td>
								<input type="hidden" name="start" value="false">
								IPlug angemeldet<br/><br/>
								<input type="submit" value="Abmelden"/>
							</td>
						</c:otherwise>
					</c:choose>
					</tr>
				</table>
			</form>
			</c:if>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>