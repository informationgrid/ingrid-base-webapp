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
	
	<c:set var="active" value="search" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Suche testen (Detailansicht)</h1>
		<div class="controls">
			<a href="#" onclick="document.location='search.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='finish.html';">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='search.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='finish.html';">Weiter</a>
		</div>
		<div id="content">
			<h2>Sie können den erstellten Index testen. Geben Sie eine Suchanfrage ein</h2>
			<form method="get" action="search.html">
				<table id="konfigForm">
					<tr>
						<td class="leftCol">Suchbegriff:</td>
						<td><input type="text" name="query" value="query"/></td>
					</tr>
					<tr>
						<td class="leftCol">&nbsp;</td>
						<td><input type="submit" value="Suchen"/></td>
					</tr>
					<tr>
						<td colspan="2"><h3>Details</h3></td>
					</tr>
					<tr>
						<td class="leftCol">Field Name:</td>
						<td>Value</td>
					</tr>
					<tr>
						<td class="leftCol">Field Name 2:</td>
						<td>Value</td>
					</tr>
					<tr>
						<td class="leftCol">Field Name 3:</td>
						<td>Value</td>
					</tr>
				</table>
			</form>
			
			
			<br/><br/><br/>
			
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>