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
		<sakai:script path="/js/umusync.js"/>
		<sakai:script path="/js/checkMainFrameHeight.js"/>		
  		 			
		<rich:dragIndicator id="indicator" />	  			
	  			
		<h:form>
		  	<sakai:tool_bar>
				<sakai:tool_bar_item				
					action="#{SyncBean.savePage}"
					value="#{msgs.b_save}"/>
				<sakai:tool_bar_item	
					action="page"
					immediate="true"
					value="#{msgs.b_rtn}"/>
  	  		</sakai:tool_bar>

			<h:panelGrid columns="2" styleClass="detached">
				<h:outputText value="#{msgs.layout}: " />	  								
				<h:selectOneRadio 
						style="display:inline"
						layout="lineDirection"
						onclick="submit()"
			 			value="#{SyncBean.page.columns}">
		  			<f:selectItem itemLabel="#{msgs.onec}" itemValue="1" />
		  			<f:selectItem itemLabel="#{msgs.twoc}" itemValue="2" />
				</h:selectOneRadio>
			
				<h:outputText value="#{msgs.pagename}: " />
				<h:outputFormat>					
					<h:inputText id="pagename"
						required="true"
						disabled="#{SyncBean.pageEditing}"
						value="#{SyncBean.page.name}"
						validator="#{SyncBean.pageNameValidator}">
					</h:inputText>
					<h:message styleClass="error" for="pagename"/>
				</h:outputFormat>
			</h:panelGrid>

			<rich:dataTable id="unicaList" value="#{SyncBean.page.leftColumn}" var="item"
				rendered="#{SyncBean.page.columns eq '1'}"
				width="25%"
				styleClass="detached"
				columnClasses="iconColumn, nameColumn">

				<h:column>
 					<a4j:commandLink
 							reRender="unicaList"
 							action="#{SyncBean.eliminar1}"
 							actionListener="#{SyncBean.selectTool}"> 
						<h:graphicImage alt="eliminar" url="/images/eliminar.gif"/>
						<f:attribute name="tool" value="#{item}"/>
					</a4j:commandLink>
				</h:column>	
						
				<h:column>							  					
  					<a4j:outputPanel>
  						<rich:dragSupport dragIndicator=":indicator"
                                dragType="left" dragValue="#{item}">
							<rich:dndParam name="label" value="#{item.string}"/>
						</rich:dragSupport>
  						<h:outputText 
							value="#{item.string}">
						</h:outputText>
						<rich:dropSupport acceptedTypes="left" dropValue="#{item}"
							oncomplete="setMainFrameHeight(window.frameElement.id)"
                    		dropListener="#{SyncBean.drop1}" reRender="unicaList">
               	 		</rich:dropSupport>
					</a4j:outputPanel>
				</h:column>							
			</rich:dataTable>
			
			<h:panelGrid columns="2" width="50%" rendered="#{SyncBean.page.columns eq '2'}" styleClass="tablas" columnClasses="tabla">
			
			
			<rich:dataTable id="izqList" value="#{SyncBean.page.leftColumn}" var="item"
				width="100%"
				columnClasses="iconColumn, nameColumn">

				<f:facet name="header">
					<a4j:outputPanel>
						<h:outputText value="#{msgs.column} 1" />
						<rich:dropSupport acceptedTypes="left,right" dropValue="#{item}"
        	           		dropListener="#{SyncBean.drop1Top}" reRender="izqList,derList">
            	   	 	</rich:dropSupport>
					</a4j:outputPanel>
				</f:facet>

				<h:column>
 					<a4j:commandLink
 							reRender="izqList"
 							action="#{SyncBean.eliminar1}"
 							actionListener="#{SyncBean.selectTool}"> 
						<h:graphicImage alt="#{msgs.del}" url="/images/eliminar.gif"/>
						<f:attribute name="tool" value="#{item}"/>
					</a4j:commandLink>
				</h:column>	
						
				<h:column>							
  					<a4j:outputPanel>
  						<rich:dragSupport dragIndicator=":indicator"
                                dragType="left" dragValue="#{item}">
							<rich:dndParam name="label" value="#{item.string}"/>
						</rich:dragSupport>
  						<h:outputText 
							value="#{item.string}">
						</h:outputText>
						<rich:dropSupport acceptedTypes="left,right" dropValue="#{item}"
							oncomplete="setMainFrameHeight(window.frameElement.id)"
                    		dropListener="#{SyncBean.drop1}" reRender="izqList,derList">
               	 		</rich:dropSupport>
					</a4j:outputPanel>
				</h:column>							
			</rich:dataTable>
			
			
			<rich:dataTable id="derList" value="#{SyncBean.page.rightColumn}" var="item"
				width="100%"
				columnClasses="iconColumn, nameColumn">

				<f:facet name="header">
					<a4j:outputPanel>
						<h:outputText value="#{msgs.column} 2" />
						<rich:dropSupport acceptedTypes="left,right" dropValue="#{item}"
							oncomplete="setMainFrameHeight(window.frameElement.id)"
        	           		dropListener="#{SyncBean.drop2Top}" reRender="izqList,derList">
            	   	 	</rich:dropSupport>
					</a4j:outputPanel>
				</f:facet>
							
				<h:column>
					<a4j:commandLink
							reRender="derList"
							action="#{SyncBean.eliminar2}"
							actionListener="#{SyncBean.selectTool}"> 
						<h:graphicImage alt="#{msgs.del}" url="/images/eliminar.gif"/>
						<f:attribute name="tool" value="#{item}"/>
					</a4j:commandLink>
				</h:column>	
						
				<h:column>							  					
  					<a4j:outputPanel>
  						<rich:dragSupport dragIndicator=":indicator"
                                dragType="right" dragValue="#{item}">
							<rich:dndParam name="label" value="#{item.string}"/>
						</rich:dragSupport>
  						<h:outputText 
							value="#{item.string}">
						</h:outputText>
						<rich:dropSupport acceptedTypes="left,right" dropValue="#{item}"
							oncomplete="setMainFrameHeight(window.frameElement.id)"
                    		dropListener="#{SyncBean.drop2}" reRender="izqList,derList">
               	 		</rich:dropSupport>
					</a4j:outputPanel>
				</h:column>							
			</rich:dataTable>
			
			</h:panelGrid>

			<h:panelGrid styleClass="detached">
				<h:column>		
					<a4j:commandLink action="#{SyncBean.addToolInPage}" 
									oncomplete="setMainFrameHeight(window.frameElement.id)"
									reRender="unicaList, izqList">
						<h:graphicImage alt="#{msgs.add}" url="/images/plus.png"/>
					</a4j:commandLink>			
					<h:selectOneMenu value="#{SyncBean.toolElegida}">
							<f:selectItems value="#{SyncBean.toolsSakaiOpciones}"/>
					</h:selectOneMenu>
				</h:column>
			</h:panelGrid>
					
			<h:panelGrid columns="2" styleClass="detached">
				<h:commandButton styleClass="botonAzul" value="#{msgs.b_save}" action="#{SyncBean.savePage}" onclick="submitValidableForm()"/>
				<h:commandButton immediate="true" value="#{msgs.b_rtn}" action="page"/>
			</h:panelGrid>

		</h:form>
		
	</sakai:view>
</f:view>
