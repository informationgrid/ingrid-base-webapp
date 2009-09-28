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
		<li <c:if test="${active == 'workingDir'}">class="active"</c:if>><a href="workingDir.html">Arbeitsverzeichnis wählen</a></li>
		<li <c:if test="${active == 'general'}">class="active"</c:if>><a href="general.html">Angaben zu Betreiber und Datenquelle</a></li>
		<li <c:if test="${active == 'partner'}">class="active"</c:if>><a href="partner.html">Hinzufügen der Partner</a></li>
		<li <c:if test="${active == 'fieldQuery'}">class="active"</c:if>><a href="fieldQuery.html">Verfügbarkeit der Ergebnisse</a></li>
		<c:catch>
		<c:import url="iplugSubNavi.jsp"></c:import>
		</c:catch>
		<li <c:if test="${active == 'save'}">class="active"</c:if>><a href="save.html">Speichern</a></li>
	</ul>
	<div class="konf">
		<p class="no">3</p>
		<h2>Indexierung</h2>
	</div>
	<ul>
		<li>Scheduling</li>
		<li <c:if test="${active == 'indexing'}">class="active"</c:if>><a href="indexing.html">Indexieren</a></li>
		<li>Suche Testen</li>
	</ul>
	<div class="konf">
		<p class="no">4</p>
		<h2>Abschließen</h2>
	</div>
	<ul>
		<li <c:if test="${active == 'heartbeat'}">class="active"</c:if>><a href="heartbeat.html">Zum IBus verbinden</a></li>
	</ul>
</div>