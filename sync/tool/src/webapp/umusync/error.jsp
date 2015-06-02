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

  		<h:form id="menusakai">
			<sakai:tool_bar>
				<sakai:tool_bar_item
					action="task"
					value="#{msgs.tareas}"/>
				<sakai:tool_bar_item
					action="criteria"
					value="#{msgs.criteria}"/>
				<sakai:tool_bar_item	
					action="page"
					value="#{msgs.paginas}"/>		
   	  		</sakai:tool_bar>
	  	</h:form>
			  			
		<h1>ERROR</h1>
		<h:outputText value="#{msgs.error}: "/>
		<br/><br/>
		
		<rich:dataList var="titulo" value="#{SyncBean.errores}">
		    <h:outputText value="#{titulo}"/>
			<rich:dataList var="error" value="#{SyncBean.errorList}">
				<f:param value="#{titulo}" name="titulo"/>
				<h:outputText value="#{error}"/>
			</rich:dataList>
		</rich:dataList>
		 
	</sakai:view>
</f:view>
