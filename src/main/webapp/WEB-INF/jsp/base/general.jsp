<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
<head>
	<title>General Informations</title>
	<link rel="stylesheet" type="text/css" href="../css/yui/reset-fonts-grids/reset-fonts-grids.css"> 
</head>

<body>
<!-- the id on the containing div determines the page width. -->
<!-- #doc = 750px; #doc2 = 950px; #doc3 = 100%; #doc4 = 974px -->
<div id="doc">					
	<div id="hd">
		<p>Header</p>
	</div>
	<div id="bd">

		<!-- Use Standard Nesting Grids and Special Nesting Grids to subdivid regions of your layout. -->
		<!-- Special Nesting Grid B tells three children to split space evenly -->
		<div class="yui-gb">
	
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
				<p>A</p>
			</div>	
	
			<div class="yui-u">
				<p>
					<div id="myDialog"> 
	 					<div class="hd">Please enter your information</div> 
	    				<div class="bd"> 

	        				<form:form name="dlgForm" method="POST" action="general.html" modelAttribute="plugDescription"> 

            					<p>Allgemeine Angaben zum Betreiber</p> 

	            				<label for="partner">Partner:</label>
	            				<form:select path="partner"> 
                    				<form:option value="-" label="--Please Select"/> 
                    				<form:options items="${partners}" itemValue="shortName" itemLabel="displayName"/> 
                				</form:select> 
	            				
	            				//TODO show only providers from selected partner above
	            				<label for="partner">Provider:</label>
	            				<form:select path="provider"> 
                    				<form:option value="-" label="--Please Select"/> 
                    				<form:options items="${providers}" itemValue="shortName" itemLabel="displayName"/> 
                				</form:select> 
 
	            				<input type="submit" value="Weiter" />
	        				</form:form> 

	    				</div> 
					</div>
				
				</p>
			</div>
	
			<div class="yui-u">
				<p>C</p>
			</div>
	
		</div>
	
	</div>
	<div id="ft">
		<p>Footer</p>
	</div>
</div>
</body>
</html>
