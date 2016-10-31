package umu.sakai.umusync.api.secure;

public interface IUMUSecureInterceptor {

	public boolean hasPermission(String permissionName) throws Throwable;
	public String getCacheKey(Object obj);
	
}
