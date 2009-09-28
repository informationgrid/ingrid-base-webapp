<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<div id="navi_vertical">
	<div class="konf">
		<p class="no">1</p>
		<h2>Kommunikation</h2>
	</div>
	<ul>
		<li <c:if test="${active == 'communication'}">class="active"</c:if>><a href="communication.html">Kommunikation bearbeiten</a></li>
	</ul>
	<div class="konf">
		<p class="no">2</p>
		<h2>Allgemein & Datenmapping</h2>
	</div>
	<ul>
		<li <c:if test="${active == 'workingDir'}">class="active"</c:if>><a href="workingDir.html">Arbeitsverzeichnis w�hlen</a></li>
		<li <c:if test="${active == 'general'}">class="active"</c:if>><a href="general.html">Angaben zu Betreiber und Datenquelle</a></li>
		<li <c:if test="${active == 'partner'}">class="active"</c:if>><a href="partner.html">Hinzuf�gen der Partner</a></li>
		<li <c:if test="${active == 'fieldQuery'}">class="active"</c:if>><a href="fieldQuery.html">Verf�gbarkeit der Ergebnisse</a></li>
		<c:catch>
		<c:import url="iplugSubNavi.jsp"></c:import>
		</c:catch>
	</ul>
	<div class="konf">
		<p class="no">3</p>
		<h2>Indexierung</h2>
	</div>
	<ul>
		<li>Scheduling</li>
		<li><a href="indexing.html">Indexieren</a></li>
		<li>Suche Testen</li>
	</ul>
	<div class="konf">
		<p class="no">4</p>
		<h2>Verbindung IBus</h2>
	</div>
	<ul>
		<li><a href="heartbeat.html">Start / Stop</a></li>
	</ul>
</div>