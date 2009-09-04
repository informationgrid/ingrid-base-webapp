<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
    <head>
        <title>Partner Hinzufügen</title>
        <link rel="stylesheet" type="text/css" href="../css/yui/reset-fonts-grids/reset-fonts-grids.css">
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
        <!-- the id on the containing div determines the page width. -->
        <!-- #doc = 750px; #doc2 = 950px; #doc3 = 100%; #doc4 = 974px -->
        <div id="doc">      
            <div id="hd">
                <h2>Hinzufügen der Partner</h2>
                <span>Bitte geben Sie mindestens einen Partner an, für den diese Datenquelle konfiguriert wird.</span>
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

                        <form:form method="post" action="partner.html" modelAttribute="plugDescription">
                            <div>
                                <fieldset>
                                   <h2>Partner Hinzufügen</h2>
                                   <label>Partner:</label>
                                   <form:select path="" id="partner"> 
                                        <form:option value="-" label="bitte wählen" />
                                        <form:options items="${partnerList}" itemValue="shortName" itemLabel="displayName" />
                                    </form:select><br />
                                    <button type="button" id="add_partner">Hinzufügen</button>
                                    <div id="partners">
                                    </div>
                                 </fieldset>
                            </div>
                            
                            <div>
                                <button type="button" onclick="document.location='general.html';">Zurück</button>
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