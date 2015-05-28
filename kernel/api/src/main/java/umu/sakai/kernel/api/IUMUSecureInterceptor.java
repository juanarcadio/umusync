package umu.sakai.kernel.api;

public interface IUMUSecureInterceptor {

	public boolean hasPermission(String permissionName) throws Throwable;
	public String getCacheKey(Object obj);
	
}
