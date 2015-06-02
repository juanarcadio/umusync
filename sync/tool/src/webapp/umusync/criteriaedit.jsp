<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
     
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="umu.sakai.umusync.tool.bundle.Messages"/>
</jsp:useBean>

<f:view>	
	<sakai:view title="#{msgs.maintitle}">
		<sakai:stylesheet path="/css/syncstyle.css"/>
		<sakai:script path="/js/checkMainFrameHeight.js"/>			 		 	
	  			
		<h:form id="editCriteria">
			<sakai:tool_bar>
				<sakai:tool_bar_item
					action="#{SyncBean.saveCriteria}"
					value="#{SyncBean.isNewCriteria ? msgs.b_criterianew : msgs.b_criteriaupd}"
				/>
				<sakai:tool_bar_item
				 	value="#{msgs.b_rtn}" 
					immediate="true"
					action="#{SyncBean.returnFromCriteria}"
				/>
   	  		</sakai:tool_bar>	
   	  		  									
			<h:panelGrid id="Name" columns="3"  styleClass="detached">

				<h:outputText value="#{msgs.criterianame} "/>				
				<h:inputText id="criteriaName" required="true"
					validator="#{SyncBean.criteriaNameValidator}"
					value="#{SyncBean.criteriaName}">
				</h:inputText>
				<h:message styleClass="error" for="criteriaName"/>
			
			</h:panelGrid>
			
			<rich:dataTable/> 
			<h:dataTable id="criteriaTable" 
							styleClass="dr-table rich-table"
							headerClass="dr-table-subheadercell rich-table-subheadercell dr-table-subheader rich-table-subheader"							
							columnClasses="dr-subtable-cell rich-subtable-cell"
							footerClass="dr-table-footercell rich-table-footercell dr-table-footer rich-table-footer"
							value="#{SyncBean.currentCriteriaList}"
							var="criterion" >
			
			<f:facet name="header">
				<h:panelGrid style="margin-left: auto; margin-right: auto; width:100%;"  
					styleClass="dr-table-headercell rich-table-headercell dr-table-header rich-table-header"> 
        			<h:outputText value="#{msgs.conditions}" />
        		</h:panelGrid>
			</f:facet> 


			<h:column>
			  	<f:facet name="header">
   					<h:outputText value=" "/>
				</f:facet>
				
				<a4j:commandLink
					immediate="true"
					reRender="criteriaTable"
					rendered="#{SyncBean.criteriaSize gt 1}"
					action="#{SyncBean.removeCriterion}"					
					actionListener="#{SyncBean.selectCriterion}"> 
					<h:graphicImage alt="#{msgs.del}" url="/images/eliminar.gif"/>
					<f:attribute name="criterion" value="#{criterion}"/>
				</a4j:commandLink>
				<h:graphicImage 
					rendered="#{SyncBean.criteriaSize eq 1}" 
					alt="X" 
					url="/images/eliminar-gris.gif"/>
			</h:column>

			<h:column>
			  	<f:facet name="header">
   					<h:outputText value="#{msgs.property}"/>
				</f:facet>
			
				<h:inputText id="Property" required="true" value="#{criterion.property}">
					<f:validator validatorId="SyncJpaValidator"/>
				</h:inputText>

				<h:message styleClass="error" for="Property"/>
			</h:column>
			<h:column>
			  	<f:facet name="header">
    				<h:outputText value="  "/>
				</f:facet>	
				<h:selectOneMenu onchange="submit()" immediate="true" value="#{criterion.comparador}">
					<f:selectItems value="#{SyncBean.comparatorSelect}"/>
					<a4j:support event="onchange" reRender="criteriaTable"/> 					
				</h:selectOneMenu>
			</h:column>
			<h:column>
			  	<f:facet name="header">
    				<h:outputText value="#{msgs.value}"/>
				</f:facet>					
				<h:inputText rendered="#{SyncBean.comparatorMap[criterion.comparador].arity eq 2}" 
					id="Value" required="true" value="#{criterion.valor}">
					<f:validator validatorId="SyncJpaValidator"/>
				</h:inputText>
				<h:message styleClass="error" for="Value"/>
			</h:column>
			
			<f:facet name="footer">
        		<a4j:commandLink
        			oncomplete="setMainFrameHeight(window.frameElement.id)"
        			reRender="criteriaTable, Name"
        			title="#{msgs.addc}"
					action="#{SyncBean.addNewCriterion}">
					<h:graphicImage alt="#{msgs.add}" url="/images/plus.png"/>
				</a4j:commandLink>
			</f:facet> 
			</h:dataTable>	

			<h:panelGrid columns="2"  styleClass="detached">
				<h:commandButton
					value="#{SyncBean.isNewCriteria ? msgs.b_criterianew : msgs.b_criteriaupd}" 
					action="#{SyncBean.saveCriteria}"
					onclick="if (#{not SyncBean.isNewCriteria} && !confirm('#{msgs.modcriteria} #{msgs.seguro}')) return false;"
				/>
				<h:commandButton immediate="true" value="#{msgs.b_rtn}" action="#{SyncBean.returnFromCriteria}"/>
			</h:panelGrid>
		</h:form>
		
	</sakai:view>
</f:view>
