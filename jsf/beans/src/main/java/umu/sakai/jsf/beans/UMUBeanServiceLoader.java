package umu.sakai.jsf.beans;

import umu.sakai.jsf.validator.Validable;
import umu.sakai.kernel.api.UMUServiceLoader;

public class UMUBeanServiceLoader extends UMUServiceLoader implements Validable {

	public UMUSecureJspMap getHasPermission() { 
		return (UMUSecureJspMap)this.getService("umu.sakai.kernel.secure.UMUSecureJSP"); 
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
