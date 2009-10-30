<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<div id="navi_vertical">
	<div class="konf">
		<p class="no">1</p>
		<h2>Kommunikation</h2>
	</div>
	<ul>
		<li <c:if test="${active == 'communication'}">class="active"</c:if>><a href="<%=request.getContextPath()%>/base/communication.html">Kommunikation bearbeiten</a></li>
	</ul>
	<div class="konf">
		<p class="no">2</p>
		<h2>Allgemein & Datenmapping</h2>
	</div>
	<ul>

		<!-- workingDir -->
		<c:choose>
			<c:when test="${communicationClickable == 'false'}">
				<li>Arbeitsverzeichnis wählen</li>
			</c:when>
			<c:when test="${active != 'workingDir'}">
				<li><a href="<%=request.getContextPath()%>/base/workingDir.html">Arbeitsverzeichnis wählen</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/workingDir.html">Arbeitsverzeichnis wählen</a></li>
			</c:otherwise>
		</c:choose>

		<!-- general -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Angaben zu Betreiber und Datenquelle</li>
			</c:when>
			<c:when test="${active != 'general'}">
				<li><a href="<%=request.getContextPath()%>/base/general.html">Angaben zu Betreiber und Datenquelle</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/general.html">Angaben zu Betreiber und Datenquelle</a></li>
			</c:otherwise>
		</c:choose>
		
		<!-- partner -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Hinzufügen der Partner</li>
			</c:when>
			<c:when test="${active != 'partner'}">
				<li><a href="<%=request.getContextPath()%>/base/partner.html">Hinzufügen der Partner</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/partner.html">Hinzufügen der Partner</a></li>
			</c:otherwise>
		</c:choose>

		<!-- provider -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Hinzufügen der Provider</li>
			</c:when>
			<c:when test="${active != 'provider'}">
				<li><a href="<%=request.getContextPath()%>/base/provider.html">Hinzufügen der Provider</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/provider.html">Hinzufügen der Provider</a></li>
			</c:otherwise>
		</c:choose>

		<!-- fieldQuery -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Verfügbarkeit der Ergebnisse</li>
			</c:when>
			<c:when test="${active != 'fieldQuery'}">
				<li><a href="<%=request.getContextPath()%>/base/fieldQuery.html">Verfügbarkeit der Ergebnisse</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/fieldQuery.html">Verfügbarkeit der Ergebnisse</a></li>
			</c:otherwise>
		</c:choose>

		<!-- iplug sub navi  -->		
		<c:catch>
		<c:import url="../iplug/iplugSubNavi.jsp"></c:import>
		</c:catch>
		
		<!-- save -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Speichern</li>
			</c:when>
			<c:when test="${active != 'save'}">
				<li><a href="<%=request.getContextPath()%>/base/save.html">Speichern</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/save.html">Speichern</a></li>
			</c:otherwise>
		</c:choose>
		
	</ul>
	
	<% if (System.getProperty("indexing") != null) { %>
	<div class="konf">
		<p class="no">3</p>
		<h2>Indexierung</h2>
	</div>
	<ul>

		<!-- scheduling -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Scheduling</li>
			</c:when>
			<c:when test="${active != 'scheduling'}">
				<li><a href="<%=request.getContextPath()%>/base/scheduling.html">Scheduling</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/scheduling.html">Scheduling</a></li>
			</c:otherwise>
		</c:choose>

		<!-- indexing -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Indexieren</li>
			</c:when>
			<c:when test="${active != 'indexing'}">
				<li><a href="<%=request.getContextPath()%>/base/indexing.html">Indexieren</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/indexing.html">Indexieren</a></li>
			</c:otherwise>
		</c:choose>
	
	</ul>
	<% } %>
	
	<div class="konf">
	   <p class="no">&nbsp;</p>
		<h2>Admin Tools</h2>
	</div>
	<ul>
	
		<!-- communication -->
		<c:choose>
			<c:when test="${communicationClickable == 'false'}">
				<li>Kommunikations Setup</li>
			</c:when>
			<c:when test="${active != 'commSetup'}">
				<li><a href="<%=request.getContextPath()%>/base/commSetup.html">Kommunikations Setup</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/commSetup.html">Kommunikations Setup</a></li>
			</c:otherwise>
		</c:choose>

		<!-- heartbeat -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>HeartBeat Setup</li>
			</c:when>
			<c:when test="${active != 'heartbeat'}">
				<li><a href="<%=request.getContextPath()%>/base/heartbeat.html">HeartBeat Setup</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/heartbeat.html">HeartBeat Setup</a></li>
			</c:otherwise>
		</c:choose>
	
		<!-- search -->
		<c:choose>
			<c:when test="${plugdescriptionClickable == 'false'}">
				<li>Suche Testen</li>
			</c:when>
			<c:when test="${active != 'search'}">
				<li><a href="<%=request.getContextPath()%>/base/search.html">Suche Testen</a></li>
			</c:when>
			<c:otherwise>
				<li class="active"><a href="<%=request.getContextPath()%>/base/search.html">Suche Testen</a></li>
			</c:otherwise>
		</c:choose>
	</ul>
</div>