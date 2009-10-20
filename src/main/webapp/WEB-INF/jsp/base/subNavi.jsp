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
		<li <c:if test="${active == 'workingDir'}">class="active"</c:if>><a href="../base/workingDir.html">Arbeitsverzeichnis wählen</a></li>
		<li <c:if test="${active == 'general'}">class="active"</c:if>><a href="../base/general.html">Angaben zu Betreiber und Datenquelle</a></li>
		<li <c:if test="${active == 'partner'}">class="active"</c:if>><a href="../base/partner.html">Hinzufügen der Partner</a></li>
		<li <c:if test="${active == 'provider'}">class="active"</c:if>><a href="../base/provider.html">Hinzufügen der Anbieter</a></li>
		<li <c:if test="${active == 'fieldQuery'}">class="active"</c:if>><a href="../base/fieldQuery.html">Verfügbarkeit der Ergebnisse</a></li>
		<c:catch>
		<c:import url="../iplug/iplugSubNavi.jsp"></c:import>
		</c:catch>
		<li <c:if test="${active == 'save'}">class="active"</c:if>><a href="../base/save.html">Speichern</a></li>
	</ul>
	
	<% if (System.getProperty("indexing") != null) { %>
	<div class="konf">
		<p class="no">3</p>
		<h2>Indexierung</h2>
	</div>
	<ul>
		<li <c:if test="${active == 'scheduling'}">class="active"</c:if>><a href="../base/scheduling.html">Scheduling</a></li>
		<li <c:if test="${active == 'indexing'}">class="active"</c:if>><a href="../base/indexing.html">Indexieren</a></li>
	</ul>
	<% } %>
	
	<div class="konf">
	   <p class="no">&nbsp;</p>
		<h2>Admin Tools</h2>
	</div>
	<ul>
        <li <c:if test="${active == 'commSetup'}">class="active"</c:if>><a href="../base/commSetup.html">Kommunikations Setup</a></li>
        <li <c:if test="${active == 'heartbeat'}">class="active"</c:if>><a href="../base/heartbeat.html">HeartBeat Setup</a></li>
        <li <c:if test="${active == 'search'}">class="active"</c:if>><a href="../base/search.html">Suche Testen</a></li>
	</ul>
</div>