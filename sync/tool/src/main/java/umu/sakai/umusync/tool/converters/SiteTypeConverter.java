package umu.sakai.umusync.tool.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

import umu.sakai.umusync.api.dao.ITask;

public class SiteTypeConverter implements Converter  {

	private static Log log = LogFactory.getLog(HomePageConverter.class);
	
	private ResourceLoader translator;
	
	public SiteTypeConverter(ResourceLoader translator) {
		this.translator = translator;
    }

	// STRING -> OBJECT
    public Object getAsObject(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              String param){
        
    	if (translator.getString("defaultUserSite").equals(param)) {
    		return ITask.DEFAULT_USER_SITE;
    	}
    	else if ( translator.getString("allUserSite").equals(param)) {
    		return ITask.ALL_USER_SITE;
    	}
    	
    	return param;
	}
 
    // OBJECT -> STRING
    public String getAsString(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              Object obj) {

    	if (ITask.DEFAULT_USER_SITE.equals(obj)) {
    		return translator.getString("defaultUserSite");
    	}
    	else if ( ITask.ALL_USER_SITE.equals(obj)) {
    		return translator.getString("allUserSite");
    	}
    	
    	return (String)obj;
    	
	}
}	 