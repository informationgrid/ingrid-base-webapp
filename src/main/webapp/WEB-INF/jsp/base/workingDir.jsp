<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
	<head>
		<title>Arbeitsverzeichnis wählen</title>
		<link rel="stylesheet" type="text/css" href="../css/yui/reset-fonts-grids/reset-fonts-grids.css"> 
	</head>
	<body>
		<!-- the id on the containing div determines the page width. -->
		<!-- #doc = 750px; #doc2 = 950px; #doc3 = 100%; #doc4 = 974px -->
		<div id="doc">		
		    <div id="hd">
                <h2>Arbeitsverzeichnis wählen</h2>
                <span>Bitte geben Sie den Pfad zum Ordner an, in dem der Index abgelegt werden soll.</span>
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

        				<form:form method="post" action="workingDir.html" modelAttribute="plugDescription">
        				    <div>
        				        <fieldset>
        				            <h2>Pfad zum Ordner</h2>
                                    <label for="workingDir">Pfad:</label>
		            				<form:input path="workinDirectory" /><br />
        				        </fieldset>
                             </div>
        				    <div>
        				        <button type="button" onclick="document.location='welcome.html';">Zurück</button>
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