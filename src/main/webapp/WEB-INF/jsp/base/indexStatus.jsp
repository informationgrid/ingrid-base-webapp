<%--
  **************************************************-
  ingrid-base-webapp
  ==================================================
  Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>InGrid iPlug Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/base/jquery.tabslet.min.js"></script>
</head>
<script>
   var clusterState = ${clusterState};
   $( document ).ready(function() {
       $('#clusterName').text( clusterState.cluster_name );
       $('#status').text( clusterState.status );
       $('#active_shards').text( clusterState.active_shards );
       $('#unassigned_shards').text( clusterState.unassigned_shards );
       $('#number_of_nodes').text( clusterState.number_of_nodes );
       $('#number_of_data_nodes').text( clusterState.number_of_data_nodes );
       
       <c:forEach items="${indices}" var="index">
           $('#tab-${index.indexName}-${index.indexType} .mapping').text( JSON.stringify( ${index.mapping}, null, 2 ) );
       </c:forEach>
       
       $('.tabs').tabslet();
   });
</script>
<body>
    <div id="header">
        <img src="../images/base/logo.gif" alt="InGrid" />
        <h1>Konfiguration</h1>
        <security:authorize access="isAuthenticated()">
            <div id="language"><a href="../base/auth/logout.html">Logout</a></div>
        </security:authorize>
    </div>
    
    <div id="help"><a href="#">[?]</a></div>
    
    <c:set var="active" value="indexStatus" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">Index Status</h1>
        <div id="content">
	        <br />
            <p>Hier sehen sie den Status des Elastic Search Index.</p>
            
            <h3>Cluster Status</h3>
            <div id="clusterState">
                <div>Cluster Name: <span id="clusterName"></span></div>
                <div>Status: <span id="status"></span></div>
                <div>Active Shards: <span id="active_shards"></span></div>
                <div>Unassigned Shards: <span id="unassigned_shards"></span></div>
                <div># Nodes: <span id="number_of_nodes"></span></div>
                <div># Data Nodes: <span id="number_of_data_nodes"></span></div>
            </div>
            
            <h3>Indizes:</h3>
            <div class="tabs">
                <ul>
                <c:forEach items="${indices}" var="index">
                    <li><a href="#tab-${index.indexName}-${index.indexType}">${index.indexName}:${index.indexType}</a></li>
                </c:forEach>
                </ul>
                
                <c:forEach items="${indices}" var="index">
                    <div id="tab-${index.indexName}-${index.indexType}">
                        <div>Anzahl Dokumente: ${index.docCount}</div>
                        
                        <h3>Mapping</h3>
                        <pre class="mapping"></pre>
                    </div>
                </c:forEach>
            </div>
            
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>
