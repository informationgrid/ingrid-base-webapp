<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
<head>
	<title>Heart Beat</title>
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
				<p>
				</p>
			</div>	
	
			<div class="yui-u">
				<p>
				
				<c:if test="${heartBeat.beatable}">
					<form action="heartbeat.html" method="POST">
						<c:choose>
							<c:when test="${!heartBeat.enable}">
								<input type="hidden" name="start" value="true">
								<input type="submit" value="Start">
							</c:when>
							<c:otherwise>
								<input type="hidden" name="start" value="false">
								<input type="submit" value="Stop">
							</c:otherwise>
						</c:choose>
						
					</form>
				</c:if>
							
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
