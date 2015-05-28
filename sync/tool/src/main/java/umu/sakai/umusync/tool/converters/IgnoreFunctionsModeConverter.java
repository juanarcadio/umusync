package umu.sakai.umusync.tool.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

import umu.sakai.umusync.api.dao.ITask;

public class IgnoreFunctionsModeConverter implements Converter  {

	private static Log log = LogFactory.getLog(IgnoreFunctionsModeConverter.class);
	
	private ResourceLoader translator;
	
	public IgnoreFunctionsModeConverter(ResourceLoader translator) {
		this.translator = translator;
    }	
 
	// STRING -> OBJECT
    public Object getAsObject(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              String param){
        
    	if (translator.getString("notToDo").equals(param)) {
    		return ITask.NOT_CHANGE_FUNCTION_MODE;
    	}
    	else if ( translator.getString("fromproperties").equals(param)) {
    		return ITask.PROPERTIES_FUNCTION_MODE;
    	}
    	else if ( translator.getString("custom").equals(param)) {
    		return ITask.CUSTOMLIST_FUNCTION_MODE;
    	}
    	
    	log.error("IgnoreFunctionsModeConverter: Invalid string "+param);
    	throw new ConverterException("IgnoreFunctionsModeConverter: Invalid string "+param);
	
	}
 
    // OBJECT -> STRING
    public String getAsString(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              Object obj) {
    	
    	if (ITask.NOT_CHANGE_FUNCTION_MODE.equals(obj)) {
    		return translator.getString("notToDo");
    	}
    	else if ( ITask.PROPERTIES_FUNCTION_MODE.equals(obj)) {
    		return translator.getString("fromproperties");
    	}
    	else if ( ITask.CUSTOMLIST_FUNCTION_MODE.equals(obj)) {
    		return translator.getString("custom");
    	}
    	
    	log.error("IgnoreFunctionsModeConverter: Invalid object "+obj);
    	throw new ConverterException("IgnoreFunctionsModeConverter: Invalid object "+obj);
	}
}	 