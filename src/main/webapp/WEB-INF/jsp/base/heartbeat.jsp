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
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
</head>
<body>
    <div id="header">
        <img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
        <h1>Konfiguration</h1>
        <div id="language"><a href="#">Englisch</a></div>
    </div>
    
    <div id="help"><a href="#">[?]</a></div>
    
    <c:set var="active" value="heartbeat" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">HeartBeat Setup</h1>
        <div id="content">
            
            <span>Status:
                <c:choose>
                    <c:when test="${enabled}"><span class="success">sendet</span></c:when>
                    <c:otherwise><span class="error">sendet nicht</span></c:otherwise>
                </c:choose>
            </span><br />
            <br />
            <c:choose>
                <c:when test="${enabled}">
                    <form action="/base/heartbeat.html" method="post">
                        <input type="hidden" name="action" value="stop" />
                        <input type="submit" name="submit" value="Verbindung trennen" />
                    </form><br />
                    <form action="/base/heartbeat.html" method="post">
                        <input type="hidden" name="action" value="restart" />
                        <input type="submit" name="submit" value="Verbindung neu starten" />
                    </form>
                 </c:when>
                 <c:otherwise>
                    <form action="/base/heartbeat.html" method="post">
                        <input type="hidden" name="action" value="start" />
                        <input type="submit" name="submit" value="Verbindung starten" />
                    </form>
                 </c:otherwise>
             </c:choose>
        
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>