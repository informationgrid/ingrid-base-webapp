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
		<menutaglib:MenuTag text="Arbeitsverzeichnis wählen" url="../base/workingDir.html" ></menutaglib:MenuTag>

		<!-- general -->
        <menutaglib:MenuTag text="Angaben zu Betreiber und Datenquelle" url="../base/general.html" ></menutaglib:MenuTag>
		
		<!-- partner -->
        <menutaglib:MenuTag text="Hinzufügen von weiteren Partnern" url="../base/partner.html" ></menutaglib:MenuTag>

		<!-- provider -->
        <menutaglib:MenuTag text="Hinzufügen von weiteren Anbietern" url="../base/provider.html" ></menutaglib:MenuTag>

		<!-- fieldQuery -->
        <menutaglib:MenuTag text="Verfügbarkeit der Ergebnisse" url="../base/fieldQuery.html" ></menutaglib:MenuTag>
        
		<!-- iplug sub navi  -->		
		<c:catch>
		<c:import url="../iplug-pages/iplugSubNavi.jsp"></c:import>
		</c:catch>
		
		<!-- save -->
        <menutaglib:MenuTag text="Speichern" url="../base/save.html" ></menutaglib:MenuTag>
		
	</ul>
	
	<% if (System.getProperty("indexing") != null) { %>
	<div class="konf">
		<p class="no">3</p>
		<h2>Indexierung</h2>
	</div>
	<ul>

		<!-- scheduling -->
        <menutaglib:MenuTag text="Scheduling" url="../base/scheduling.html" ></menutaglib:MenuTag>

		<!-- indexing -->
        <menutaglib:MenuTag text="Indexieren" url="../base/indexing.html" ></menutaglib:MenuTag>
	
	</ul>
	<% } %>
	
	<div class="konf">
	   <p class="no">&nbsp;</p>
		<h2>Admin Tools</h2>
	</div>
	<ul>
	
		<!-- communication -->
        <menutaglib:MenuTag text="Kommunikations Setup" url="../base/commSetup.html" ></menutaglib:MenuTag>

		<!-- heartbeat -->
        <menutaglib:MenuTag text="HeartBeat Setup" url="../base/heartbeat.html" ></menutaglib:MenuTag>
	
		<!-- search -->
        <menutaglib:MenuTag text="Suche Testen" url="../base/search.html" ></menutaglib:MenuTag>
		
		<!-- cache -->
        <menutaglib:MenuTag text="Caching Einstellungen" url="../base/cache.html" ></menutaglib:MenuTag>

	</ul>
</div>