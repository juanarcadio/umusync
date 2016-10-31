package umu.sakai.umusync.tool.beans;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import umu.sakai.umusync.api.secure.IUMUSecureInterceptor;

public class UMUSecureJspMap implements Map {

	private static Log log = LogFactory.getLog(UMUSecureJspMap.class);
    protected IUMUSecureInterceptor permissionService;
	protected HashMap<Object,Object> permissionCache = new HashMap<Object,Object>(); 
	protected boolean useCache = true;
	protected long lastCachedTime = 0;
	protected int refreshPeriod = 30;
	
	public Collection values() {
		return null;
	}

	public Object put(Object key, Object value) {
		return null;
	}

	public Set keySet() {
		return null;
	}

	public boolean isEmpty() {
		return false;
	}

	public int size() {
		return 0;
	}

	public void putAll(Map t) {
	}

	public void clear() {
	}

	public boolean containsValue(Object value) {
		return false;
	}

	public Object remove(Object key) {
		return null;
	}

	public boolean containsKey(Object key) {
		return false;
	}

	public Set entrySet() {
    	return null;
    }
    
    public void setPermissionService(IUMUSecureInterceptor permissionService) { this.permissionService = permissionService; }
    
    public void setUseCache(boolean useCache) { this.useCache = useCache; }
    
    public void setRefreshPeriod(int refreshPeriod) { this.refreshPeriod = refreshPeriod; }
    
	protected Object getProperty(Object obj) {
		boolean permission = false;
		try {
			permission = permissionService.hasPermission((String) obj);
		} catch (Throwable t) { 
			log.error(t.getMessage());
		}
		return Boolean.valueOf(permission);
	}

	public Object get(Object obj) {
		return useCache?getCached(obj):getProperty(obj);
	}

	private boolean cacheExpired() {
		return ((System.currentTimeMillis()-lastCachedTime)/1000)>refreshPeriod;
	}
	
	protected Object getCached(Object obj) {
		if (cacheExpired()) { 
			permissionCache.clear();
			lastCachedTime = System.currentTimeMillis();
		}		
		String key = permissionService.getCacheKey(obj);
		Object cachedPermission = permissionCache.get(key);
		if (cachedPermission==null) {
			cachedPermission = getProperty(obj);
			permissionCache.put(key,cachedPermission);
		}
		return cachedPermission;
	}

}
