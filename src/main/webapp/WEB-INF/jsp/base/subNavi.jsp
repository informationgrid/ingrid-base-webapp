<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<div id="navi_vertical">
	<div id="konf_1"><p class="pic"><img src="../images/konf_1.gif" width="13" height="15" /></p><h2>Konfiguration geladen</h2></div>
	<div id="konf_2"><p class="pic"><img src="../images/konf_2.gif" width="13" height="15" /></p><h2>Alle Einstellungen bearbeiten</h2></div>
	<div class="freitext">${desc}</div>
	<ul>
		<li <c:if test="${active == 'workingDir'}">class="active"</c:if>><a href="workingDir.html">Arbeitsverzeichnis wählen</a></li>
		<li <c:if test="${active == 'general'}">class="active"</c:if>><a href="general.html">Angaben zu Betreiber und Datenquelle</a></li>
		<li <c:if test="${active == 'partner'}">class="active"</c:if>><a href="partner.html">Hinzufügen der Partner</a></li>
		<li <c:if test="${active == 'fieldQuery'}">class="active"</c:if>><a href="fieldQuery.html">Verfügbarkeit der Ergebnisse</a></li>
		<c:catch>
		<c:import url="iplugSubNavi.jsp"></c:import>
		</c:catch>
	</ul>
	<div id="konf_3"><p class="pic"><img src="../images/konf_3.gif" width="13" height="15" /></p><h2>Konfiguration abgeschlossen</h2></div>
</div>