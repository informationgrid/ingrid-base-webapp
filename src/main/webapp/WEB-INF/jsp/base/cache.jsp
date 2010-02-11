<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="de.ingrid.admin.security.IngridPrincipal"%><html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>Portal U Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
</head>
<body>
    <div id="header">
        <img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
        <h1>Konfiguration</h1>
        <%
        java.security.Principal  principal = request.getUserPrincipal();
        if(principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
        %>
            <div id="language"><a href="../base/auth/logout.html">Logout</a></div>
        <%
        }
        %>
        
    </div>
    
    <div id="help"><a href="#">[?]</a></div>
    
    <c:set var="active" value="cache" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">Caching Einstellungen</h1>
        <div id="content">
            <br />
	        <form:form action="../base/cache.html" method="post" modelAttribute="cache">
                <table id="konfigForm">
                    <tr>
                        <td class="leftCol">Cache:</td>
                        <td><form:radiobutton path="active" value="true" label="aktiviert" /><form:radiobutton path="active" value="false" label="deaktiviert" /></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Cache-Time (in min):</td>
                        <td><form:input path="lifeTime" /></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Cache-Elements:</td>
                        <td><form:input path="elementsCount"/></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Cache-Speicher:</td>
                        <td><form:radiobutton path="diskStore" value="false" label="RAM" /><form:radiobutton path="diskStore" value="true" label="Festplatte" /></td>
                    </tr>
                    <tr>
                        <td class="leftCol">&nbsp;</td>
                        <td><input type="submit" value="Cache bearbeiten" /></td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>