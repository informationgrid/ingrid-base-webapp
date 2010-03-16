<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<div id="navi_vertical">
	<div class="konf">
		<p class="no">1</p>
		<h2>Kommunikation</h2>
	</div>
	<ul>
		<li <c:if test="${active == 'communication'}">class="active"</c:if>><a href="../base/communication.html">Kommunikation bearbeiten</a></li>
	</ul>
	<div class="konf">
		<p class="no">2</p>
		<h2>Allgemein & Datenmapping</h2>
	</div>
	<ul>

		<!-- workingDir -->
		<li <c:if test="${active == 'workingDir'}">class="active"</c:if>>
		<c:choose>
            <c:when test="${communicationExists}"><a href="../base/workingDir.html">Arbeitsverzeichnis wählen</a></c:when>
            <c:otherwise>Arbeitsverzeichnis wählen</c:otherwise>
		</c:choose>
		</li>

		<!-- general -->
		<li <c:if test="${active == 'general'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/general.html">Angaben zu Betreiber und Datenquelle</a></c:when>
            <c:otherwise>Angaben zu Betreiber und Datenquelle</c:otherwise>
        </c:choose>
        </li>
		
		<!-- partner -->
		<li <c:if test="${active == 'partner'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/partner.html">Hinzufügen von weiteren Partnern</a></c:when>
            <c:otherwise>Hinzufügen von weiteren Partnern</c:otherwise>
        </c:choose>
        </li>

		<!-- provider -->
		<li <c:if test="${active == 'provider'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/provider.html">Hinzufügen von weiteren Anbietern</a></c:when>
            <c:otherwise>Hinzufügen von weiteren Anbietern</c:otherwise>
        </c:choose>
        </li>

		<!-- fieldQuery -->
		<li <c:if test="${active == 'fieldQuery'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/fieldQuery.html">Verfügbarkeit der Ergebnisse</a></c:when>
            <c:otherwise>Verfügbarkeit der Ergebnisse</c:otherwise>
        </c:choose>
        </li>
        
		<!--
		<c:if test="${renderExtras == 'true'}">
			<c:choose>
			    <c:when test="${!plugdescriptionExists}">
			        <li
			        <c:if test="${active == 'extras'}">
			            class="active"
			        </c:if>
			        >Weitere Einstellungen</li>
			    </c:when>
			    <c:when test="${active != 'extras'}">
			        <li><a href="../base/extras.html">Weitere Einstellungen</a></li>
			    </c:when>
			    <c:otherwise>
			        <li class="active">Weitere Einstellungen</li>
			    </c:otherwise>
			</c:choose>
		</c:if>-->

		<!-- iplug sub navi  -->		
		<c:catch>
		<c:import url="../iplug-pages/iplugSubNavi.jsp"></c:import>
		</c:catch>
		
		<!-- save -->
		<li <c:if test="${active == 'save'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/save.html">Speichern</a></c:when>
            <c:otherwise>Speichern</c:otherwise>
        </c:choose>
        </li>
		
	</ul>
	
	<% if (System.getProperty("indexing") != null) { %>
	<div class="konf">
		<p class="no">3</p>
		<h2>Indexierung</h2>
	</div>
	<ul>

		<!-- scheduling -->
		<li <c:if test="${active == 'scheduling'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/scheduling.html">Scheduling</a></c:when>
            <c:otherwise>Scheduling</c:otherwise>
        </c:choose>
        </li>

		<!-- indexing -->
		<li <c:if test="${active == 'indexing'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/indexing.html">Indexieren</a></c:when>
            <c:otherwise>Indexieren</c:otherwise>
        </c:choose>
        </li>
	
	</ul>
	<% } %>
	
	<div class="konf">
	   <p class="no">&nbsp;</p>
		<h2>Admin Tools</h2>
	</div>
	<ul>
	
		<!-- communication -->
		<li <c:if test="${active == 'commSetup'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${communicationExists}"><a href="../base/commSetup.html">Kommunikations Setup</a></c:when>
            <c:otherwise>Kommunikations Setup</c:otherwise>
        </c:choose>
        </li>

		<!-- heartbeat -->
		<li <c:if test="${active == 'heartbeat'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/heartbeat.html">HeartBeat Setup</a></c:when>
            <c:otherwise>HeartBeat Setup</c:otherwise>
        </c:choose>
        </li>
	
		<!-- search -->
		<li <c:if test="${active == 'search'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/search.html">Suche Testen</a></c:when>
            <c:otherwise>Suche Testen</c:otherwise>
        </c:choose>
        </li>
		
		<!-- cache -->
		<li <c:if test="${active == 'cache'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="../base/cache.html">Caching Einstellungen</a></c:when>
            <c:otherwise>Caching Einstellungen</c:otherwise>
        </c:choose>
        </li>

	</ul>
</div>