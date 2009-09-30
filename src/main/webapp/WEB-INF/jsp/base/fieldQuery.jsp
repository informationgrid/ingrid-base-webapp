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
<script type="text/javascript" src="../js/base/jquery-1.3.2.min.js"></script>
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
	});
</script>
</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
		<h1>Konfiguration</h1>
		<div id="language"><a href="#">Englisch</a></div>
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<c:set var="active" value="fieldQuery" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Verfügbarkeit der Ergebnisse</h1>
		<div class="controls">
			<a href="#" onclick="document.location='provider.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" class="submit">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='provider.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" class="submit">Weiter</a>
		</div>
		<div id="content">
			<h2>Geben Sie Field Queries an</h2>
			<form:form method="post" action="fieldQuery.html" modelAttribute="fieldQuery">
			     <input type="hidden" name="action" value="submit" />
                 <input type="hidden" name="id" value="" />
				<table id="konfigForm">
					<tr>
						<td class="leftCol">IBus:</td>
						<td>
						    <form:select path="busUrl">
						      <form:options items="${busUrls}" />
						    </form:select>
						    <form:errors path="busUrl" cssClass="error" element="div" />
						</td>
					</tr>
					<tr>
						<td class="leftCol">Regex:</td>
						<td><form:input path="regex" /><form:errors path="regex" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td class="leftCol">Index Feld Name:</td>
						<td><form:input path="key" /><form:errors path="key" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td class="leftCol">Index Feld Wert:</td>
						<td><form:input path="value" /><form:errors path="value" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td class="leftCol">Option:</td>
						<td>
							<form:radiobutton path="option" value="prohibited" label="verboten"/>
							<form:radiobutton path="option" value="required" label="erforderlich"/>
							<form:errors path="option" cssClass="error" element="div" />
						</td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><button type="button" action="add">Hinzufügen</button></td>
					</tr>		
				</table>

				<br/><br/>
				<h3>Vorhandene Field Queries</h3>
				<div id="fieldQueries"></div>
				<table id="addedQueries">
					<tr>
						<th>IBus</th>
						<th>Regex</th>
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
					       <td>${field.key}</td>
					       <td>${field.value}</td>
					       <td>
					           <c:choose>
					               <c:when test="${field.prohibited}">verboten</c:when>
					               <c:when test="${field.required}">erforderlich</c:when>
					           </c:choose>
					       </td>
                           <td><button type="button" action="delete" id="${i}">Entfernen</button></td>
					   </tr>
					   <c:set var="i" value="${i + 1}" />
					</c:forEach>
				</table>
				
			</form:form>
			
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>