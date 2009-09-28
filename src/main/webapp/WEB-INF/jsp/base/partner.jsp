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
         $("#add_partner").click(function() {
             var id = $("#partner").val();   // selected partner
             var name = $("#partner option[value='"+id+"']").html();    // name of partner
            	addPartner(id, name);
         });

         <c:forEach items="${plugDescription.partner}" var="partner">
             <c:forEach items="${partnerList}" var="current"> <c:if test="${current.shortName == partner}"> addPartner('${current.shortName}', '${current.displayName}'); </c:if> </c:forEach>
         </c:forEach> 
     });

     function addPartner(id, name) {
     	// only if partner is selected and isnt already added
      var partners = $("#partners"); // partners div
         if((id != '-') && (partners.find("div[id='"+id+"']").length == 0)) {
       var num = partners.find("div").length + 1;  // number of this partner
       // append div, input, text and button
       partners.append("<div id='"+id+"'><input type='hidden' name='partners' value='"+id+"' /><b>"+num+". Partner:</b> "+name+" <button type='button'>Entfernen</button></div>");
       // add remove action to button
       partners.find("div[id='"+id+"'] button").click(function() {
           // find surviving partners
           var divs = $(this).parent().parent().find("div").not("[id="+id+"]");
           // remove partner
           $(this).parent().remove();
           // rename all other partners
           var num = 1;
           divs.each(function() {
               $(this).find("b").html(num+". Partner:");
               num++;
           });
       });
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
				<table id="konfigForm">
					<tr>
						<td class="leftCol">Partner:</td>
						<td>
							<form:select path="" id="partner"> 
                                <form:option value="-" label="bitte wählen" />
                                <form:options items="${partnerList}" itemValue="shortName" itemLabel="displayName" />
                            </form:select>
                            <br/><br/>
                            <button type="button" id="add_partner">Hinzufügen</button>
                            <br/>
                            <br/>
                            <div id="partners"></div>
						</td>
					</tr>
							
				</table>
			</form:form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>