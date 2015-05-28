package umu.sakai.umusync.tool.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import umu.sakai.umusync.api.dao.ICriteria;

public class CriteriaConverter implements Converter  {

//	private static Log log = LogFactory.getLog(CriteriaConverter.class);
	
	private Map<String, List<ICriteria>> map;
	private String empty;
	
	public CriteriaConverter(Map<String, List<ICriteria>> map, String empty) {
		this.map = map;
		this.empty = empty;
    }	
 
	// STRING -> OBJECT
    public Object getAsObject(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              String param){
    	
    	if (empty.equals(param) || "".equals(param)) return new ArrayList<ICriteria>();
		if (!map.containsKey(param)) throw new ConverterException("CriteriaConverter: ValueObject from "+param+"not found.");
		return map.get(param);		
	}
 
    // OBJECT -> STRING
    public String getAsString(FacesContext facesContext, 
                              UIComponent uiComponent, 
                              Object obj) {
    	try {
    		if (obj==null) return empty;
    		List<ICriteria> lista = (List<ICriteria>)obj;
    		if (lista.isEmpty()) return empty;		
    		return lista.get(0).getName();    		
    	}
        catch (Exception e) {
        	throw new ConverterException("CriteriaConverter: Invalid valueObject "+e);
        }
	}
}	 