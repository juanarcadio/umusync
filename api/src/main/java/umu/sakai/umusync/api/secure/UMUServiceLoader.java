package umu.sakai.umusync.api.secure;

import java.util.Map;

public class UMUServiceLoader {

	protected Map<String,Object> services;
	
	public void setServices(Map<String,Object> m) { services = m; }
	
	public Object getService(String s) { return services.get(s); }
	
}
