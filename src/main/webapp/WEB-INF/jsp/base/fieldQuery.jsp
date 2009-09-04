<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
    <head>
        <title>Fieldquery Hinzufügen</title>
        <link rel="stylesheet" type="text/css" href="../css/yui/reset-fonts-grids/reset-fonts-grids.css">
        <script type="text/javascript" src="../js/jquery-1.3.2.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {
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
                var table = $("#addFieldQuery");
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
            }
        </script>
    </head>
    <body>
        <!-- the id on the containing div determines the page width. -->
        <!-- #doc = 750px; #doc2 = 950px; #doc3 = 100%; #doc4 = 974px -->
        <div id="doc">      
            <div id="hd">
                <h2>Verfügbarkeit der Ergebnisse</h2>
                <span>Geben Sie Field Queries an.</span>
            </div>
            <div id="bd">
                <!-- Use Standard Nesting Grids and Special Nesting Grids to subdivid regions of your layout. -->
                <!-- Special Nesting Grid B tells three children to split space evenly -->
                <div class="yui-gb">
                    <!-- the first child of a Grid needs the "first" class -->
                    <div class="yui-u first">
                        <span>A</span>
                    </div>  
                    <div class="yui-u">

                        <form:form method="post" action="fieldQuery.html" modelAttribute="plugDescription">
                            <div>
                                <table id="addFieldQuery">
                                    <tr>
                                        <th>BusUrl</th>
                                        <th>Regex</th>
                                        <th>Index Feld Name</th>
                                        <th>Index Feld Wert</th>
                                        <th>Verboten</th>
                                        <th>Erforderlich</th>
                                        <th></th>
                                    </tr>
                                    <tr>
                                        <td><select name="bus_url">
                                            <c:forEach items="${busUrls}" var="bus">
                                                <option value="${bus}">${bus}</optoin>
                                            </c:forEach>
                                        </select></td>
                                        <td><input type="text" name="regex" value=".*" /></td>
                                        <td><input type="text" name="key" /></td>
                                        <td><input type="text" name="value" /></td>
                                        <td><input type="checkbox" name="prohibited" value="true" /></td>
                                        <td><input type="checkbox" name="required" value="true" /></td>
                                        <td><button type="button" id="add">Hinzufügen</button></td>
                                    </tr>
                                </table>
                                <div id="fieldQueries">
                                </div>
                            </div>
                        
                            <div>
                                <button type="button" onclick="document.location='partner.html';">Zurück</button>
                                <button type="button" onclick="document.location='welcome.html';">Abbrechen</button>
                                <input type="submit" value="Weiter" />
                            </div>
                        </form:form>

                    </div>
                    <div class="yui-u">
                        <span>C</span>
                    </div>
                </div>
            </div>
            <div id="ft">
                <span>Footer</span>
            </div>
        </div>
    </body>
</html>