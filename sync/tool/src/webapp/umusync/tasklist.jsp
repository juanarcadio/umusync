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
					disabled="true"
					action="task"
					value="#{msgs.tareas}"/>
				<sakai:tool_bar_item
					action="criteria"
					value="#{msgs.criteria}"/>
				<sakai:tool_bar_item	
					action="page"
					value="#{msgs.paginas}"/>
				<sakai:tool_bar_item
					action="#{SyncBean.newTask}"
					value="#{msgs.newtask}"/>				
   	  		</sakai:tool_bar>
	  	</h:form>
	  			
		<a4j:form id="list">	
		  	
			<%-- TASKS LIST --%>
			<rich:dataTable id="tasksList" value="#{SyncBean.tasksList}" var="task"
				bgcolor="#AE1B2E" border="10" cellpadding="5" cellspacing="3"
				first="0" width="100%" dir="LTR" frame="hsides" rules="all"
				rendered="#{not empty SyncBean.tasksList}"
				styleClass="detached"
				columnClasses="iconColumn, iconColumn, iconColumn, nameColumn, textColumn, textColumn, iconColumn">
				
				<f:facet name="header">
					<h:outputText value="#{msgs.taskslist}" />
				</f:facet>
				
				<rich:column styleClass="#{task.available?'Fila1':'Fila3'}">
					<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<a4j:commandLink 
  						actionListener="#{SyncBean.changeTask}"
  						reRender="tasksList"
  						title="#{task.available? msgs.on : msgs.off}">  					 
						<h:graphicImage alt="available" url="/images/#{task.available?'S':'N'}.gif"/>
						<f:attribute name="task" value="#{task}"/>  
					</a4j:commandLink>
				</rich:column>		
												
				<rich:column styleClass="#{task.available?'Fila1':'Fila3'}">
  					<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<h:commandLink 
  						title="#{msgs.edit}"
  						action="#{SyncBean.editTask}"
  						actionListener="#{SyncBean.selectTask}">
						
						<h:graphicImage alt="#{msgs.edit}" url="/images/editar.gif"/>
						<f:attribute name="task" value="#{task}"/>  
					</h:commandLink>
				</rich:column>
				
				<rich:column styleClass="#{task.available?'Fila1':'Fila3'}">
				  	<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<h:commandLink
  						title="#{msgs.del}"
						onclick="if (!confirm('#{msgs.deltask}. #{msgs.seguro}')) return false;"
  						action="#{SyncBean.removeTask}"
  						actionListener="#{SyncBean.selectTask}"> 
						<h:graphicImage alt="#{msgs.del}" url="/images/eliminar.gif"/>
						<f:attribute name="task" value="#{task}"/>
					</h:commandLink>
				</rich:column>		

				<rich:column styleClass="#{task.available?'Fila1':'Fila3'}">				
  					<f:facet name="header">
    					<h:outputText value="#{msgs.tipo}" />
  					</f:facet>
  					<h:outputText
  						value="#{task.tipo}" 
  						converter="#{SyncBean.siteTypeConverter}">
					</h:outputText>
					<h:outputText 
						rendered="#{empty task.tipo}"
						value="#{msgs.todos}">
					</h:outputText>
				</rich:column>
				
				<rich:column styleClass="#{task.available?'Fila1':'Fila3'}">
  					<f:facet name="header">
    					<h:outputText value="#{msgs.realms}" />
  					</f:facet>
				
					<h:outputText
						style="display:block" 
						value="#{task.realmSite}" />
					<h:outputText
						rendered="#{empty task.realmSite}"
						style="display:block" 
						value="#{msgs.ninguno}" />						
					<h:outputText
						style="display:block"
						value="#{task.realmSection}" />			
					
				</rich:column>
				
				<rich:column styleClass="#{task.available?'Fila1':'Fila3'}">
  					<f:facet name="header">
    					<h:outputText value="#{msgs.comments}"/>
  					</f:facet>
				
					<h:outputText value="#{task.comments}" />	
					
				</rich:column>
										
				<rich:column styleClass="#{task.available?'Fila1':'Fila3'}">
				  	<f:facet name="header">
    					<h:outputText value=" " />
  					</f:facet>
  					<h:commandLink
  						title="#{msgs.executetask}"
  						action="#{SyncBean.execTask}"
  						actionListener="#{SyncBean.selectTask}"> 
						<h:graphicImage alt="#{msgs.executetask}" url="/images/exe.gif"/>
						<f:attribute name="task" value="#{task}"/>
					</h:commandLink>
				</rich:column>		
			
			</rich:dataTable>
			
			<h:outputText value="#{msgs.notasks}"
				rendered="#{empty SyncBean.tasksList}"/>
			
		</a4j:form>
		
	</sakai:view>
</f:view>
