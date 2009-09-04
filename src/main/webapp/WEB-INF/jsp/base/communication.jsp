<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<html>
    <head>
        <title>Kommunikations-Einstellungen</title>
        <link rel="stylesheet" type="text/css" href="../css/yui/reset-fonts-grids/reset-fonts-grids.css">
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
        <!-- the id on the containing div determines the page width. -->
        <!-- #doc = 750px; #doc2 = 950px; #doc3 = 100%; #doc4 = 974px -->
        <div id="doc">      
            <div id="hd">
                <h2>Hinzufügen des iBus</h2>
                <span>Geben sie den iBus an.</span>
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
                            <input type="hidden" name="form" value="base" />
                            <input type="hidden" name="bus" value="${bus}" />
                            <div>
                                <fieldset class="proxyServiceUrl">
	                                <label for="name">Eigene Proxy Service Url</label><br />
		                            <span class="toggle"><b><c:out value="${communication.proxyServiceUrl}" /></b></span>
		                            <input class="hide" name="proxyServiceUrl" value="${communication.proxyServiceUrl}" />
                                </fieldset>
                                <fieldset>
			        				<h2>standard iBus</h2> 
			
			         				<label>iBus Proxy Service Url</label>
			         				<span class="toggle"><b><c:out value="${communication.busProxyServiceUrl}" /></b></span>
			         				<input class="hide" name="busProxyServiceUrl" value="${communication.busProxyServiceUrl}" />
			         				<br />
			         				<label>IP</label>
			         				<span class="toggle"><b><c:out value="${communication.ip}" /></b></span>
			         				<input class="hide" name="ip" value="${communication.ip}" />
			         				<br />
			         				<label>Port</label>
			         				<span class="toggle"><b><c:out value="${communication.port}" /></b></span>
			         				<input class="hide" name="port" value="${communication.port}" />
		         				</fieldset>
		         				
                            </div>
                            <div>
	                            <button type="button" id="edit">Bearbeiten</button>
	                            <input class="hide" type="submit" value="Speichern" id="save" />
                            </div>
	     				</form:form>
	     				
	     				<br />
	     				
	     				<c:if test="${!empty busses}">
		     				
		     				<form:form method="post" action="communication.html" modelAttribute="communication" name="client">
	                            <div>
	                                <fieldset>
	                                    <h2>weiterer iBus</h2> 
	            
	                                    <label>iBus Proxy Service Url</label>
	                                    <input type="text" name="busProxyServiceUrl" />
	                                    <br />
	                                    <label>IP</label>
	                                    <input type="text" name="ip"  />
	                                    <br />
	                                    <label>Port</label>
	                                    <input type="text" name="port" />
	                                </fieldset>
	                                
	                            </div>
	                            <div>
	                                <input type="submit" value="Hinzufügen" />
	                            </div>
	                        </form:form>
	                        
		     				<br />
		     				
		     				<div id="ibusses">
	                            <table>
	                                <tr>
	                                    <th><b>iBus Url</b></th>
	                                    <th><b>IP</b></th>
	                                    <th><b>Port</b></th>
	                                    <th></th>
	                                </tr>
	                                <c:set var="busIndex" value="0" />
	                                <c:forEach items="${busses}" var="bus">
	                                    <tr>
	                                        <td>${bus.busProxyServiceUrl}</td>
	                                        <td>${bus.ip}</td>
	                                        <td>${bus.port}</td>
	                                        <td><a href="/base/communication.html?action=edit&bus=${busIndex}">Bearbeiten</a> <a href="/base/communication.html?action=delete&bus=${busIndex}">Löschen</a></td>
	                                    </tr>
	                                    <c:set var="busIndex" value="${busIndex + 1}" />
	                                </c:forEach>
	                            </table>
		     				</div>
	     				
	     				</c:if>

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