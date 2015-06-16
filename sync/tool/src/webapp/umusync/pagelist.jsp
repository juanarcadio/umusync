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
					action="criteria"
					value="#{msgs.criteria}"/>
				<sakai:tool_bar_item
					disabled="true"	
					action="page"
					value="#{msgs.paginas}"/>
				<sakai:tool_bar_item
					action="#{SyncBean.newPage}"
					value="#{msgs.newpage}"/>
   	  		</sakai:tool_bar>
	  	</h:form>
	
		<h:form>	  						
			
			<rich:dataTable id="pagesList" value="#{SyncBean.pagesList}" var="item"
				bgcolor="#F1F1F1" border="10" cellpadding="5" cellspacing="3"
				first="0"  dir="LTR" frame="hsides" rules="all"
				rendered="#{not empty SyncBean.pagesList}"
				styleClass="detached"
				columnClasses="iconColumn, iconColumn, nameColumn, nameColumn, textColumn">

				<f:facet name="header">
					<h:outputText value="#{msgs.pagelist}" />
				</f:facet>
				
				<h:column>
  					<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<h:commandLink 
  						title="#{msgs.edit}"
  						action="#{SyncBean.editPage}"
  						actionListener="#{SyncBean.selectPage}">
						
						<h:graphicImage alt="#{msgs.edit}" url="/images/editar.gif"/>
						<f:attribute name="page" value="#{item}"/>  
					</h:commandLink>
				</h:column>
				
				<h:column>
				  	<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<h:commandLink
  						title="#{msgs.del}"
  						onclick="if (!confirm('#{msgs.delpage} #{msgs.seguro}')) return false;"
  						action="#{SyncBean.removePage}"
  						actionListener="#{SyncBean.selectPage}"> 
						<h:graphicImage alt="#{msgs.del}" url="/images/eliminar.gif"/>
						<f:attribute name="page" value="#{item}"/>
					</h:commandLink>
				</h:column>	

				<h:column>
  					<f:facet name="header">
    					<h:outputText value="#{msgs.pagename}" />
  					</f:facet>
  					<h:outputText 
						value="#{item.name}">
					</h:outputText>
				</h:column>
				
				<h:column>
  					<f:facet name="header">
    					<h:outputText value="#{msgs.columns}" />
  					</f:facet>
  					<h:outputText 
						value="#{item.columns}">
					</h:outputText>
				</h:column>
						
			</rich:dataTable>
			
			<h:outputText value="#{msgs.nopages}"
				rendered="#{empty SyncBean.pagesList}"/>			
		</h:form>
		
	</sakai:view>
</f:view>
