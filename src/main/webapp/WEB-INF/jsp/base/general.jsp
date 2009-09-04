<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
    <head>
        <title>Angaben zum Informations-Anbieter</title>
        <link rel="stylesheet" type="text/css" href="../css/yui/reset-fonts-grids/reset-fonts-grids.css">
        <script type="text/javascript">
	        var map = ${jsonMap};
	        
	        function updateProviders() {
		        var select = document.getElementById('provider');
		        reset(select);
		        var partner = document.getElementById('partner').value;
		        for(var i = 0; i < map[partner].length; i++) {
		        	select.options[select.length] = new Option(map[partner][i].displayName, map[partner][0].shortName);
		        }
	        }

	        function reset(select) {
		        for(var i = (select.length - 1); i > 0; i--) {
			        select.options[i] = null;
		        }
	        }
        </script> 
    </head>
    <body>
        <!-- the id on the containing div determines the page width. -->
        <!-- #doc = 750px; #doc2 = 950px; #doc3 = 100%; #doc4 = 974px -->
        <div id="doc">      
            <div id="hd">
                <h2>Angaben zu Betreiber und Datenquelle</h2>
                <span>Mit * gekennzeichnete Felder sind optional.</span>
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

        				<form:form method="post" action="general.html" modelAttribute="plugDescription"> 

                            <div>
	                            <fieldset>
	           					   <h2>Allgemeine Angaben zum Betreiber</h2>
	           					   <label>Name des Partners:</label>
                                   <form:select path="partner" onchange="updateProviders();"> 
		                                <form:option value="-" label="bitte wählen" />
		                                <form:options items="${partners}" itemValue="shortName" itemLabel="displayName" /> 
		                            </form:select><br /> 
	           					   <label>Name des Anbieters:</label>
	           					   <form:select path="provider"> 
		                                <form:option value="-" label="bitte wählen" /> 
		                            </form:select>
	                            </fieldset>
	                            <fieldset>
                                   <h2>Ansprechpartner</h2>
                                   <label>Titel(*):</label>
                                   <form:input path="personTitle" /><br />
                                   <label>Nachname:</label>
                                   <form:input path="personSureName" /><br />
                                   <label>Vorname:</label>
                                   <form:input path="personName" /><br />
                                   <label>Tel.:</label>
                                   <form:input path="personPhone" /><br />
                                   <label>E-mail:</label>
                                   <form:input path="personMail" />
                                </fieldset>
                                <fieldset>
                                   <h2>Datenquelle</h2>
                                   <label>Name der Datenquelle:</label>
                                   <form:input path="dataSourceName" /><br />
                                   <label>Kurzbeschreibung(*):</label>
                                   <form:textarea path="dataSourceDescription" /><br />
                                   <label>Art der Datenquelle</label>
                                   <form:checkboxes items="${datatypes}" path="dataTypes" itemLabel="displayName" itemValue="name" />
                                </fieldset>
                                <fieldset>
                                   <h2>iPlug</h2>
                                   <label>Adresse des iPlugs (/&lt;Gruppen Name&gt;:&lt;IPlug Name&gt;)</label>
                                   <form:input path="proxyServiceURL" /><br />
                                   <label>Adresse des korrespondierenden iPlugs (/&lt;Gruppen Name&gt;:&lt;IPlug Name&gt;)</label>
                                   <form:input path="correspondentProxyServiceURL" />
                                </fieldset>
                                <fieldset>
                                   <h2>Administrationsinterface</h2>
                                   <label>URL:</label>
                                   <form:input path="iplugAdminGuiUrl" /><br />
                                   <label>Port:</label>
                                   <form:input path="iplugAdminGuiPort" /><br />
                                   <label>Administrationskennwort:</label>
                                   <form:password path="iplugAdminPassword" /> 
                                </fieldset>
                            </div>

                            <div>
                                <button type="button" onclick="document.location='workingDir.html';">Zurück</button>
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