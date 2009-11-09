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
<script>
function getState(){
	$.get("/base/indexState.html", function(data){
		  document.getElementById('dialog').style.display = '';
		  console.log(data);
		  if(data == 'TERMINATED'){
			document.location.href = '/base/finish.html'
		  }else{
			setTimeout(getState, 1000);
		  }
	}, "text");
}
</script>
<c:if test="${started == 'true'}">
	<script>getState();</script>
</c:if>
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
	
	<c:set var="active" value="indexing" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Index erstellen</h1>
		<div class="controls">
			<a href="#" onclick="document.location='scheduling.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='finish.html';">�berspringen</a>
			<a href="#" onclick="document.getElementById('indexing').submit();">Jetzt Indizieren</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='scheduling.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='finish.html';">�berspringen</a>
			<a href="#" onclick="document.getElementById('indexing').submit();">Jetzt Indizieren</a>
		</div>
		<div id="content">
			<h2>Sie k�nnen Ihre Daten jetzt indizieren, um im Anschlu� die Suche zu testen.</h2>
			<form action="indexing.html" method="post" id="indexing">
				<table id="konfigForm">
					<tr>
						<td>
							Abh�ngig von der Menge der Daten kann dieser Schritt einige Zeit in Anspruch nehmen.<br/>
							Die Indizierung kann �bersprungen werden, um automatisiert wie unter "Scheduling" angegeben zu erfolgen.
							
							<br/><br/>
							<!-- 
							Index Status: ${state}<br/>
							Anzahl der zu indizierenden Dokumente: ${documentCount}
							 -->
							
						</td>
					</tr>
							
				</table>
			</form> 	
		</div>
		
		<div class="dialog" id="dialog" style="display:none">
			<div class="content">Daten werden indiziert. Bitte haben Sie Geduld.</div>
		</div>	
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>