<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName"
		value="umu.sakai.umusync.tool.bundle.Messages" />
</jsp:useBean>

<f:view>
	<sakai:view title="#{msgs.maintitle}">
	<sakai:stylesheet path="/css/syncstyle.css"/>
	<sakai:script path="/js/umusync.js"/>
	  			
		<h:form id="executeTask">	  						
			<h:outputText value="#{msgs.tosync}:" styleClass="titulo"/>			
			
			<h:panelGrid columns="2" styleClass="detachedTable">
				<h:outputText value="#{msgs.comments}:"/>
				<h:outputText value="#{SyncBean.task.comments}" styleClass="tab"/>				
			
				<h:outputText value="#{msgs.sitetype}: " />
				<h:outputText rendered="#{not empty SyncBean.task.tipo}"
					converter="#{SyncBean.siteTypeConverter}"
					value="#{SyncBean.task.tipo}" styleClass="tab" />
				<h:outputText rendered="#{empty SyncBean.task.tipo}"
					value="#{msgs.todos}" styleClass="tab" />

				<h:outputText value="#{msgs.realmp} (#{msgs.site}): " />
				<h:outputText rendered="#{not empty SyncBean.task.realmSite}"
					value="#{SyncBean.task.realmSite}" styleClass="tab" />
				<h:outputText rendered="#{empty SyncBean.task.realmSite}"
					value="#{msgs.ninguno}" styleClass="tab" />

				<h:outputText value="#{msgs.realmp} (#{msgs.section}): " />
				<h:outputText rendered="#{not empty SyncBean.task.realmSection}"
					value="#{SyncBean.task.realmSection}" styleClass="tab" />
				<h:outputText rendered="#{empty SyncBean.task.realmSection}"
					value="#{msgs.ninguno}" styleClass="tab" />

				<h:outputText value="#{msgs.ignoresites}: " />
				<h:outputText rendered="#{not empty SyncBean.task.ignoreSitesById}"
					value="#{SyncBean.task.ignoreSitesById}" styleClass="tab" />
				<h:outputText rendered="#{empty SyncBean.task.ignoreSitesById}"
					value="#{msgs.ninguno}" styleClass="tab" />

				<h:outputText value="#{msgs.filter}: " />
				<h:outputText value="#{SyncBean.task.criteria}"
					converter="#{SyncBean.criteriaConverter}" styleClass="tab" />
					
				<h:outputText value="#{msgs.syncHome}: "/>
				<h:outputText value="#{SyncBean.task.syncHome}" 
					converter="#{SyncBean.homePageConverter}" styleClass="tab"/>
				
				<h:outputText value="#{msgs.syncInto}:" />
				<h:outputText value="#{SyncBean.task.syncInto}" styleClass="tab" />

				<h:outputText value="#{msgs.addt}: " />
				<rich:dataList rendered="#{not empty SyncBean.task.toolsToAdd}"
					value="#{SyncBean.task.toolsToAdd}" var="addt">
					<h:outputText value="#{addt.string}" />
				</rich:dataList>
				<h:outputText rendered="#{empty SyncBean.task.toolsToAdd}"
					value="#{msgs.ninguna}" styleClass="tab" />

				<h:outputText value="#{msgs.delt}: " />
				<rich:dataList rendered="#{not empty SyncBean.task.toolsToDel}"
					value="#{SyncBean.task.toolsToDel}" var="delt">
					<h:outputText value="#{delt.string}" />
				</rich:dataList>
				<h:outputText rendered="#{empty SyncBean.task.toolsToDel}"
					value="#{msgs.ninguna}" styleClass="tab" />

				<h:outputText value="#{msgs.addp}: " />
				<rich:dataList rendered="#{not empty SyncBean.task.pagesToAdd}"
					value="#{SyncBean.task.pagesToAdd}" var="addp">
					<h:outputText value="#{addp.name}" />
				</rich:dataList>
				<h:outputText rendered="#{empty SyncBean.task.pagesToAdd}"
				 	value="#{msgs.ninguna}" styleClass="tab" />

				<h:outputText value="#{msgs.delp}: " />
				<rich:dataList rendered="#{not empty SyncBean.task.pagesToDel}"
					value="#{SyncBean.task.pagesToDel}" var="delp">
					<h:outputText value="#{delp.string}" />
				</rich:dataList>
				<h:outputText rendered="#{empty SyncBean.task.pagesToDel}"
					value="#{msgs.ninguna}" styleClass="tab" />

				<h:outputText value="#{msgs.ignoreFunction}: "/>
				<h:outputText rendered="#{SyncBean.task.ignoreFunctionsMode ne 'C'}"
					value="#{SyncBean.task.ignoreFunctionsMode}" 
					converter="#{SyncBean.ignoreFunctionsModeConverter}" styleClass="tab"/>
				<rich:dataList rendered="#{SyncBean.task.ignoreFunctionsMode eq 'C' and not empty SyncBean.task.ignoredFunctions}"
					value="#{SyncBean.task.ignoredFunctions}" var="permiso">				
					<h:outputText value="#{permiso.string}"/>
				</rich:dataList>
				<h:outputText rendered="#{SyncBean.task.ignoreFunctionsMode eq 'C' and empty SyncBean.task.ignoredFunctions}"
					value="#{msgs.ninguno}" styleClass="tab" />
			</h:panelGrid>
						
			<h:commandButton 
				rendered="#{empty SyncBean.informe}"
				value="#{msgs.confirm}"
				action="#{SyncBean.executeTask}"
				onclick="showCargando()"/>
			<h:commandButton value="#{msgs.b_rtn}" action="rtn"/>
			<h:graphicImage
				id="cargando"
				rendered="#{empty SyncBean.informe}" 
				alt="#{msgs.loading}..."
				url="/images/cargando.gif"
				style="display:none"/>
	        <h:outputText 
	        	id="espere"
	        	rendered="#{empty SyncBean.informe}"
	        	value="#{msgs.executing}..."
	        	style="display:none"/>

			<h:outputText
				rendered="#{not empty SyncBean.informe}"
				value="#{msgs.result}:"
				styleClass="titulo"/>
			<h:outputText
				rendered="#{not empty SyncBean.informe}"
				value="#{SyncBean.informe}" />
		</h:form>
		
	</sakai:view>
</f:view>
