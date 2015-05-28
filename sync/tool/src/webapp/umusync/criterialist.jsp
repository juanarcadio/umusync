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
  		<h:form id="menusakai">
			<sakai:tool_bar>
				<sakai:tool_bar_item
					action="task"
					value="#{msgs.tareas}"/>
				<sakai:tool_bar_item
					disabled="true"
					action="criteria"
					value="#{msgs.criteria}"/>
				<sakai:tool_bar_item	
					action="page"
					value="#{msgs.paginas}"/>
				<sakai:tool_bar_item
					action="#{SyncBean.newCriteria}"
					value="#{msgs.newcriteria}"/>
   	  		</sakai:tool_bar>
	  	</h:form>
	  			
		<h:form id="list">	  						
			
			<%-- CRITERIA LIST --%>
			<rich:dataTable id="criteriasList" value="#{SyncBean.criteriaNames}" var="item"
				bgcolor="#F1F1F1" border="10" cellpadding="5" cellspacing="3"
				first="0" dir="LTR" frame="hsides" rules="all"
				rendered="#{not empty SyncBean.criteriaNames}"
				styleClass="detached"
				columnClasses="iconColumn, iconColumn, nameColumn, textColumn">

				<f:facet name="header">
					<h:outputText value="#{msgs.criterialist}" />
				</f:facet>
				
				<h:column>
  					<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<h:commandLink 
  						title="#{msgs.edit}"
  						action="#{SyncBean.editCriteria}"
  						actionListener="#{SyncBean.selectCriteria}">
						
						<h:graphicImage alt="#{msgs.edit}" url="/images/editar.gif"/>
						<f:attribute name="name" value="#{item}"/>  
					</h:commandLink>
				</h:column>
				
				<h:column>
				  	<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<h:commandLink
  						title="#{msgs.del}"
  						onclick="if (!confirm('#{msgs.delcriteria} #{msgs.seguro}')) return false;"
  						action="#{SyncBean.removeCriteria}"
  						actionListener="#{SyncBean.selectCriteria}"> 
						<h:graphicImage alt="#{msgs.del}" url="/images/eliminar.gif"/>
						<f:attribute name="name" value="#{item}"/>
					</h:commandLink>
				</h:column>	

				<h:column>
  					<f:facet name="header">
    					<h:outputText value="#{msgs.criterianame}" />
  					</f:facet>
  					<h:outputText 
						value="#{item}">
					</h:outputText>
				</h:column>
				
				<h:column>
  					<f:facet name="header">
    					<h:outputText value="#{msgs.conditions}" />
  					</f:facet>

					<h:dataTable var="criterion" value="#{SyncBean.criteriaMap[item]}">
						<h:column>
							<h:outputText value="#{criterion.property} "/>
							<h:outputText value="#{SyncBean.comparatorMap[criterion.comparador].signature}"/>
							<h:outputText rendered="#{SyncBean.comparatorMap[criterion.comparador].arity eq 2}"
											value=" #{criterion.valor}"/>
						</h:column>
					</h:dataTable>
				</h:column>
						
			</rich:dataTable>
			
			<h:outputText value="#{msgs.nocriteria}"
				rendered="#{empty SyncBean.criteriaNames}"/>
			
		</h:form>		
	</sakai:view>
</f:view>
