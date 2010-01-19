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
    $(document).ready(function() {
        $("button[action], .submit").click(function() {
            // get form
            var form = $("#plugDescription");
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
	
	<c:set var="active" value="partner" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Hinzufügen der Partner</h1>
		<div class="controls">
			<a href="#" onclick="document.location='general.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='general.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div id="content">
			<h2>Geben Sie mindestens einen Partner an, für den diese Datenquelle konfiguriert wird.</h2>
			<form:form method="post" action="partner.html" modelAttribute="plugDescription">
			     <input type="hidden" name="action" value="submit" />
			     <input type="hidden" name="id" value="" />
				<table id="konfigForm">
					<tr>
						<td class="leftCol">Partner:</td>
						<td>
							<select name="partner"> 
                                <option value="">bitte wählen</option>
                                <c:forEach items="${partnerList}" var="partner">
                                    <option value="${partner.shortName}">${partner.displayName}</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td class="rightCol">
                            <button type="button" action="add">Hinzufügen</button>
                        </td>
                    </tr>
                    <tr><td colspan=3><br /><hr /><br /></td></tr>
                    <tr>
                        <td>
                            <span>Weitere Partner dieses iPlugs:</span>
                        </td>
                        <td>
                            <form:errors path="partners" cssClass="error" element="div" />

                            <!-- <div id="partners">-->
                                <c:set var="i" value="1" />
                                <c:forEach items="${partners}" var="partner">
                                    <b>${i}. Partner:</b> ${partner.displayName}</td><td><button type="button" action="delete" id="${partner.shortName}">Entfernen</button></td></tr><tr><td></td><td>
                                    <c:set var="i" value="${i + 1}" />
                                </c:forEach>
                            <!-- </div>-->
						</td>
					</tr>
							
				</table>
			</form:form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>