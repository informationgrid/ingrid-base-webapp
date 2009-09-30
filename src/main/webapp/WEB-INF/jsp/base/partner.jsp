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
<script type="text/javascript" src="../js/jquery-1.3.2.min.js"></script>
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
		<img src="../images/logo.gif" width="168" height="60" alt="Portal U" />
		<h1>Konfiguration</h1>
		<div id="language"><a href="#">Englisch</a></div>
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<c:set var="active" value="partner" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Hinzuf�gen der Partner</h1>
		<div class="controls">
			<a href="#" onclick="document.location='general.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='general.html';">Zur�ck</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div id="content">
			<h2>Geben Sie mindestens einen Partner an, f�r den diese Datenquelle konfiguriert wird.</h2>
			<form:form method="post" action="partner.html" modelAttribute="plugDescription">
			     <input type="hidden" name="action" value="submit" />
			     <input type="hidden" name="id" value="" />
				<table id="konfigForm">
					<tr>
						<td class="leftCol">Partner:</td>
						<td>
							<select name="partner"> 
                                <option value="">bitte w�hlen</option>
                                <c:forEach items="${partnerList}" var="partner">
                                    <option value="${partner.shortName}">${partner.displayName}</option>
                                </c:forEach>
                            </select>
                            <form:errors path="partners" cssClass="error" element="div" />
                            <br/>
                            <br/>
                            <button type="button" action="add">Hinzuf�gen</button>
                            <br/>
                            <br/>
                            <div id="partners">
                                <c:set var="i" value="1" />
                                <c:forEach items="${partners}" var="partner">
                                    <b>${i}. Partner:</b> ${partner.displayName} <button type="button" action="delete" id="${partner.shortName}">Entfernen</button><br />
                                    <c:set var="i" value="${i + 1}" />
                                </c:forEach>
                            </div>
						</td>
					</tr>
							
				</table>
			</form:form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>