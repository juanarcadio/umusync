package umu.sakai.umusync.tool.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

import umu.sakai.umusync.api.dao.ITask;

public class HomePageConverter implements Converter  {

	private static Log log = LogFactory.getLog(HomePageConverter.class);
	
	private ResourceLoader translator;
	
	public HomePageConverter(ResourceLoader translator) {
		this.translator = translator;
    }	
 
	// STRING -> OBJECT
    public Object getAsObject(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              String param){
        
    	if (param==null) {
    		log.error("HomePageConverter: null string");
    		throw new ConverterException("HomePageConverter: null string");
    	}
    	
    	if (translator.getString("notToDo").equals(param)) {
    		return ITask.NOT_CHANGE_HOME_PAGE;
    	}
    	else if ( translator.getString("removeHP").equals(param)) {
    		return ITask.REMOVE_THE_HOME_PAGE;
    	}
    	else if ( "sakai.properties".equals(param)) {
    		return ITask.PROPERTIES_HOME_PAGE;
    	}
    	
    	return param;
	}
 
    // OBJECT -> STRING
    public String getAsString(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              Object obj) {

    	if (obj==null || ITask.NOT_CHANGE_HOME_PAGE.equals(obj)) {
    		return translator.getString("notToDo");
    	}
    	else if ( ITask.REMOVE_THE_HOME_PAGE.equals(obj)) {
    		return translator.getString("removeHP");
    	}
    	else if ( ITask.PROPERTIES_HOME_PAGE.equals(obj)) {
    		return "sakai.properties";
    	}
    	return (String)obj;
    	
	}
}	 