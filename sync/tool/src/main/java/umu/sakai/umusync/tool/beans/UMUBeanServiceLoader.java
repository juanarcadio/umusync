package umu.sakai.umusync.tool.beans;

import java.io.Serializable;

import umu.sakai.umusync.tool.validator.Validable;
import umu.sakai.umusync.api.secure.UMUServiceLoader;

public class UMUBeanServiceLoader extends UMUServiceLoader implements Validable, Serializable {

	private static final long serialVersionUID = 1386888594369535951L;

	public UMUSecureJspMap getHasPermission() { 
		return (UMUSecureJspMap)this.getService("umu.sakai.umusync.api.secure.UMUSecureJSP"); 
	}
	
	public Class getDaoClass(String beanProperty) {
		String key = getDaoId(beanProperty);
		if (key==null) key = beanProperty;
		Object obj = getService(key);
		if (obj!=null) return obj.getClass();
		return null;
	}
	
	public String getDaoId(String beanProperty) {
		return null;
	}
}
