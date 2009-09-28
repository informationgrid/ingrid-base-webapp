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
        $(".hide").hide();
        $("#edit").toggle(function() {
            $(".toggle,.hide").toggle();
            $(this).html("Abbrechen");
        }, function() {
        	$(".toggle,.hide").toggle();
        	$(this).html("Bearbeiten");
        });
        
        <c:if test="${(empty busses) || (!empty bus)}">
        $(".toggle,.hide").toggle();
        $("#edit").hide();
        <c:if test="${!empty bus}">
        $(".proxyServiceUrl .toggle, .proxyServiceUrl .hide").toggle();
        </c:if>
        </c:if>
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
	
	<c:set var="active" value="communication" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Hinzufügen des iBus</h1>
		<div class="controls">
			<a href="#" onclick="document.location='welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('communication').submit();">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('communication').submit();">Weiter</a>
		</div>
		<div id="content">
			<h2>Geben sie den iBus an.</h2>
			<form:form method="post" action="communication.html" modelAttribute="communication" name="client">
				<input type="hidden" name="form" value="base" />
                <input type="hidden" name="bus" value="${bus}" />
				<table id="konfigForm">
					<tr>
						<td class="leftCol">Eigene Proxy Service Url:</td>
						<td>
		                    <input type="text" name="proxyServiceUrl" value="${communication.proxyServiceUrl}" />
						</td>
					</tr>
					<tr>
						<td colspan="2"><h3>Standard IBus</h3></td>
					</tr>
					<tr>
						<td class="leftCol">IBus Proxy Service Url:</td>
						<td>
		                    <input type="text" name="busProxyServiceUrl" value="${communication.busProxyServiceUrl}" />
						</td>
					</tr>
					<tr>
						<td class="leftCol">IP:</td>
						<td>
		                    <input type="text" name="ip" value="${communication.ip}" />
						</td>
					</tr>
					<tr>
						<td class="leftCol">Port:</td>
						<td>
		                    <input type="text" name="port" value="${communication.port}" />
						</td>
					</tr>		
				</table>
			</form:form>
			
			<c:if test="${!empty busses}">
			<form:form method="post" action="communication.html" modelAttribute="communication" name="client">
                  <table id="konfigForm">
					<tr>
						<td colspan="2"><h3>Weiterer IBus</h3></td>
					</tr>
					<tr>
						<td class="leftCol">IBus Proxy Service Url:</td>
						<td>
		                    <input type="text" name="busProxyServiceUrl" />
						</td>
					</tr>
					<tr>
						<td class="leftCol">IP:</td>
						<td>
		                    <input type="text" name="ip" />
						</td>
					</tr>
					<tr>
						<td class="leftCol">Port:</td>
						<td>
		                    <input type="text" name="port" />
						</td>
					</tr>
					<tr>
						<td class="leftCol">&nbsp;</td>
						<td> <input type="submit" value="Hinzufügen" /></td>
					</tr>
				</table>	
                  
              </form:form>
	                
			<br />
			
			<div id="ibusses">
              <table>
                  <tr>
                  	<td colspan="4"><h3>Vorhandene IBusse</h3></td>
                  </tr>
                  <tr>
                      <th>IBus Url</th>
                      <th>IP</th>
                      <th>Port</th>
                      <th>&nbsp;</th>
                  </tr>
                  <c:set var="busIndex" value="0" />
                  <c:forEach items="${busses}" var="bus">
                      <tr>
                          <td>${bus.busProxyServiceUrl}</td>
                          <td>${bus.ip}</td>
                          <td>${bus.port}</td>
                          <td><button onclick="document.location = '/base/communication.html?action=delete&bus=${busIndex}'"/>Löschen</button></td>
                      </tr>
                      <c:set var="busIndex" value="${busIndex + 1}" />
                  </c:forEach>
              </table>
			</div>
			</c:if>
			
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>


<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
    <head>
        <title>Kommunikations-Einstellungen</title>
        
    </head>
    <body>
        <!-- the id on the containing div determines the page width. -->
        <!-- #doc = 750px; #doc2 = 950px; #doc3 = 100%; #doc4 = 974px -->
        <div id="doc">      
            <div id="hd">
                <h2>Hinzufügen des iBus</h2>
                <span></span>
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

                        <div>
                            <button type="button" onclick="document.location='welcome.html';">Zurück</button>
                        </div>
                        
                        <br />

                        <form:form method="post" action="communication.html" modelAttribute="communication" name="client">
                            
                            <div>
                               
                                <fieldset>
			        				<h2>standard iBus</h2> 
			
			         				
		         				</fieldset>
		         				
                            </div>
                            <div>
	                            <button type="button" id="edit">Bearbeiten</button>
	                            <input class="hide" type="submit" value="Speichern" id="save" />
                            </div>
	     				</form:form>
	     				
	     				<br />
	     				
	     				

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