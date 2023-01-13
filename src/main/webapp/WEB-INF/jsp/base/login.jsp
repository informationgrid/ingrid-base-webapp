<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2023 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  http://ec.europa.eu/idabc/eupl5
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  --%>
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<head>
<title>InGrid iPlug Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../../css/base/portal_u.css" type="text/css" media="all" />
</head>
<body>
	<div id="header">
        <img src="../../images/base/logo.gif" alt="InGrid" />
		<h1>Konfiguration</h1>
		<security:authorize access="isAuthenticated()">
			<div id="language"><a href="../../base/auth/logout.html">Logout</a></div>
		</security:authorize>
	</div>
	
	<div id="help"><a href="#">[?]</a></div>

    <!-- since we are a directory deeper (virtually) we have to modify relatives links in the navi -->	
	<c:import url="subNavi.jsp" var="subNavigation"></c:import>
    ${fn:replace(subNavigation, "../", "../../")}
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">iPlug Konfiguration</h1>
		
		<div id="content">
			<br/>
				        <div>
				        	<p>&nbsp;</p>
				            <form method="post" action="j_spring_security_check" id="login">
					            <table id="konfigForm">
									<tr>
										<td class="leftCol">Name:</td>
										<td>
                                            <div class="input inline">
                                                <input style="width: 200px;" type="text" name="j_username"/>
                                            </div>
										</td>
									</tr>
									<tr>
										<td class="leftCol">Passwort:</td>
										<td>
                                            <div class="input inline">
										        <input style="width: 200px;" type="password" name="j_password" />
                                            </div>
										</td>
									</tr>
									<tr>
										<td class="leftCol">&nbsp;</td>
										<td>
										     <input type="submit" value="Login" />
										</td>
									</tr>
								</table>
				            </form>   
				        </div>
						<c:if test="${!securityEnabled}">
					       	<script>
					       		document.getElementById('login').submit();
					       	</script>
				        </c:if>
			
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
