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
    /*$(document).ready(function() {
        $("#add").click(function() {
            var bus_url = $("select[name='bus_url']").val();
            var regex = $("input[name='regex']").val();
            var key = $("input[name='key']").val();
            var value = $("input[name='value']").val();
            var prohibited = $("input[name='prohibited']").attr("checked");
            var required = $("input[name='required']").attr("checked");
        	addFieldQuery(bus_url, regex, key, value, prohibited, required);
        });

        <c:if test="${!empty fields}">
         <c:forEach items="${fields}" var="field">
             addFieldQuery("${field['bus_url']}", "${field['regex']}", "${field['key']}", "${field['value']}", "${field['prohibited']}", "${field['required']}");
         </c:forEach>
        </c:if>
    });

    function addFieldQuery(bus_url, regex, key, value, prohibited, required) {
        var table = $("#addedQueries");
        var input = $("#fieldQueries");
        var count = input.children().length;
        // add to table
        table
            .append($("<tr id='tr_"+count+"'></tr>")
                .append("<td>"+bus_url+"</td>")
                .append("<td>"+regex+"</td>")
                .append("<td>"+key+"</td>")
                .append("<td>"+value+"</td>")
                .append("<td>"+(prohibited?"ja":"nein")+"</td>")
                .append("<td>"+(required?"ja":"nein")+"</td>")
                .append($("<td></td>")
                    .append($("<button type='button'>Entfernen</button>")
                        .click(function() {
                            $("#tr_"+count).remove();
                            $("#input_"+count).remove();
                        })
                    )
                )
            );
        // add to parameters
        input.append("<input id='input_"+count+"' type='hidden' name='fieldQuery' value='"+bus_url+";"+regex+";"+key+";"+value+";"+prohibited+";"+required+"' />");
        // reset inputs
        table.find("input").not("[type='checkbox']").not("[name='regex']").val("");
        table.find("input[name='regex']").val(".*");
        table.find("input[type='checkbox']").removeAttr("checked");
    }*/
</script>
</head>
<body>
	<div id="header">
		<img src="../images/logo.gif" width="168" height="60" alt="Portal U" />
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
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='provider.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div id="content">
			<h2>Geben Sie Field Queries an</h2>
			<form:form method="post" action="fieldQuery.html" modelAttribute="plugDescription">
				<table id="konfigForm">
					<tr>
						<td class="leftCol">IBus:</td>
						<td>
							<select name="bus_url">
                                <c:forEach items="${busUrls}" var="bus">
                                    <option value="${bus}">${bus}</optoin>
                                </c:forEach>
                            </select>
						</td>
					</tr>
					<tr>
						<td class="leftCol">Regex:</td>
						<td><input type="text" name="regex" value=".*" /></td>
					</tr>
					<tr>
						<td class="leftCol">Index Feld Name:</td>
						<td><input type="text" name="key" /></td>
					</tr>
					<tr>
						<td class="leftCol">Index Feld Wert:</td>
						<td><input type="text" name="value" /></td>
					</tr>
					<tr>
						<td class="leftCol">Verboten:</td>
						<td><input type="checkbox" name="prohibited" value="true" /></td>
					</tr>
					<tr>
						<td class="leftCol">Erforderlich:</td>
						<td><input type="checkbox" name="required" value="true" /></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><button type="button" id="add">Hinzufügen</button></td>
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
						<th>Verboten</th>
						<th>Erforderlich</th>
						<th>&nbsp;</th>
					</tr>
					<c:forEach items="">
					</c:forEach>
				</table>
				
			</form:form>
			
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>