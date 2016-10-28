package umu.sakai.umusync.impl.secure;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.AopUtils;

import umu.sakai.umusync.api.secure.HasPermission;
import umu.sakai.umusync.api.secure.IUMUSecureInterceptor;
import umu.sakai.umusync.api.secure.PermissionException;

public class UMUSecureInterceptor implements MethodInterceptor,IUMUSecureInterceptor {

	private static Log log = LogFactory.getLog(UMUSecureInterceptor.class);
	
	protected Map methodPermissions;
	protected String toolPrefix;
	protected String [] toolPermissions;
	// Sakai Services
	protected AuthzGroupService authzGroupService;
	protected UserDirectoryService userDirectoryService;
	protected ToolManager toolManager;
	protected FunctionManager functionManager;
	protected SecurityService securityService;
	
	public void setMethodPermissions(Map methodPermissions) { this.methodPermissions = methodPermissions; }
	public void setToolPrefix(String toolPrefix) { this.toolPrefix = toolPrefix; }
	public void setToolPermissions(String[] toolPermissions) { this.toolPermissions = toolPermissions; }
	public String[] getToolPermissions() { return toolPermissions; }
	public String getToolPrefix() { return toolPrefix; }
	// Sakai Services
	public void setAuthzGroupService(AuthzGroupService authzGroupService) { this.authzGroupService = authzGroupService; }
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) { this.userDirectoryService = userDirectoryService; }
	public void setToolManager(ToolManager toolManager) { this.toolManager = toolManager; }
	public void setFunctionManager(FunctionManager functionManager) { this.functionManager = functionManager; }
	public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

	private boolean pasaPorSerAdmin(HasPermission annotation) {
		return (annotation!=null && annotation.isAdmin() && securityService.isSuperUser()) || 
			   (annotation==null && securityService.isSuperUser());
	}
	
	private boolean tieneTool(String toolId) {
		return toolManager.getTool(toolId)!=null;
	}
	
	private String getFunctionRequired(String toolId) {
		if (toolId==null) {
			return toolManager.getCurrentTool().getRegisteredConfig().getProperty("functions.require");
		} else {
			return toolManager.getTool(toolId).getRegisteredConfig().getProperty("functions.require");
		}
	}
	
	private boolean existeFunctionRequired(String toolId) {
		return getFunctionRequired(toolId)!=null; 
	}
	
	private boolean permitidoInvocarMetodo(HasPermission annotation, String toolId) {
		return (tieneTool(toolId) && !existeFunctionRequired(toolId)) ||
			   (!tieneTool(toolId) && pasaPorSerAdmin(annotation)) ||
			   (tieneTool(toolId) && existeFunctionRequired(toolId) && pasaPorSerAdmin(annotation));
	}
	
	private boolean tienePermisoRequerido() throws Throwable {
		return hasPermission(getFunctionRequired(null));
	}
	
	private boolean permitidoInvocarMetodo(HasPermission annotation) throws Throwable {
		return !existeFunctionRequired(null) || tienePermisoRequerido() || pasaPorSerAdmin(annotation);
	}
	
	// Method Interceptor
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		HasPermission annotation = this.getHasPermissionAnnotation(methodInvocation);
		// Estamos fuera (currentTool==null) y hay requiered
		if (toolManager.getCurrentTool()==null) {
			// toolid: sakai.umuxxxx (prefix: umuxxxx.) 
			String umuToolId = ("sakai."+getToolPrefix()).substring(0, getToolPrefix().length()+5);
			if (!permitidoInvocarMetodo(annotation,umuToolId)) {
				// ESTOY FUERA Y TIENE REQUIRE!
				String msg = "Intento de acceso desde fuera de sakai a: ["+umuToolId+"]: "+methodInvocation.getMethod().getName();
				log.error(msg);
				this.throwException(SecurityException.class, msg);
			}
		}
		// Estamos dentro y no tenemos permiso required
		else {
			if (!permitidoInvocarMetodo(annotation)) {
				// ESTOY DENTRO Y NO TENGO PERMISOS!
				String msg = "En sakai no tengo el permiso required: "+getFunctionRequired(null)+" en "+methodInvocation.getMethod().getName();
				log.error(msg);
				this.throwException(SecurityException.class, msg);
			}
		}
			
		if (annotation!=null) verifyPermissions(annotation);
		return methodInvocation.proceed();
	}

	public void verifyPermissions(HasPermission hp) throws Throwable {
		if (hp.isAdmin() && securityService.isSuperUser()) return;
		verifyPermission(hp,hp.name());
		verifyAllPermissions(hp,hp.all());
		verifyOneOfPermissions(hp,hp.oneof());
	}

	protected void verifyPermission(HasPermission hp,String permission) throws Throwable {
		if (permission==null || permission.equals("")) return;
		if (!hasPermission(this.getToolPrefix()+permission)) {
			this.throwException(hp.exception(),hp.message());
		}
	}

	protected void verifyAllPermissions(HasPermission hp,String [] allPermissions) throws Throwable {
		if (allPermissions==null) return;
		for (String permission:allPermissions) {
			if (permission==null || permission.equals("")) continue;
			if (!hasPermission(this.getToolPrefix()+permission)) {
				this.throwException(hp.exception(),hp.message());
			}
		}
	}

	protected void verifyOneOfPermissions(HasPermission hp,String [] oneOfPermissions) throws Throwable {
		if (oneOfPermissions==null) return;
		for (String permission:oneOfPermissions) {
			if (permission==null || permission.equals("") || hasPermission(this.getToolPrefix()+permission)) {
				return;
			}
		}
		this.throwException(hp.exception(),hp.message());
	}

	public boolean hasPermission(String permissionName) throws Throwable {
		// If permissionName is null then has permission 
		if (permissionName==null) return true;
		try {
			String userId = userDirectoryService.getCurrentUser().getId();
			String sitePath = "/site/"+toolManager.getCurrentPlacement().getContext();
			return authzGroupService.isAllowed(userId, permissionName, sitePath);
		} catch (Throwable t) {
			throw t;
		}
	}

	// Permissions Registration
	public void init() {
		if (this.getToolPermissions()!=null) {
	        Collection<String> registered = functionManager.getRegisteredFunctions(this.getToolPrefix());
	        for (String permission:this.getToolPermissions()) {
	            if (!registered.contains(this.getToolPrefix()+permission)) {
	                functionManager.registerFunction(this.getToolPrefix()+permission);
	            }
	        }
		}
	}
	
	/* Verifies whether the HasPermission annotation is present and get it if so. */
	private HasPermission getHasPermissionAnnotation(MethodInvocation methodInvocation) throws Throwable {
	    
	    HasPermission annotation = null;
	    Method method = methodInvocation.getMethod();
	    
	    // Try to read the annotation directly from the method
	    if (method.isAnnotationPresent(HasPermission.class)) {
	    	annotation = method.getAnnotation(HasPermission.class);
	    }
	    // If it's a RelfectiveMethodInvocation it means we're facing a Spring Proxy
	    else if (methodInvocation instanceof ReflectiveMethodInvocation) {
	      ReflectiveMethodInvocation reflectiveInvocation = (ReflectiveMethodInvocation) methodInvocation;
	      Object proxy = reflectiveInvocation.getThis();
	      Class<?> targetClass = AopUtils.getTargetClass(proxy);
	      Method proxyMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
	      if (proxyMethod.isAnnotationPresent(HasPermission.class)) {
	        annotation = proxyMethod.getAnnotation(HasPermission.class);
	      }
	    }
	    
	    return annotation;
	}
	
	protected void throwException(Class<?> exception, String errorId) throws Throwable {
		try {
			// Get a String-based constructor for the exception class
			Constructor<?> constructor = getStringConstructor(exception);
			Object exc;
			// Instantiate it and throw the exception
			if (constructor != null) {
				exc = constructor.newInstance(errorId);
			} else exc = exception.newInstance();
			// If we got here, an exception without any error message will be thrown
			throw (Throwable)exc; 
		}
		catch (InstantiationException ex) {
			PermissionException permExcp = new PermissionException(errorId);
			throw permExcp;
		}
		catch (InvocationTargetException ex) {
			PermissionException permExcp = new PermissionException(errorId);
			throw permExcp;
		}
	}
	
	/* Gets the String constructor for Class. */
	private Constructor<?> getStringConstructor(Class<?> clazz) {
		Constructor<?> constructor = null;
		try {
			constructor = clazz.getConstructor(String.class);
		} catch (NoSuchMethodException ex) {
			try {
				return PermissionException.class.getConstructor(String.class);
			} catch (NoSuchMethodException exx) { }
		}
		return constructor;
	}

	public String getCacheKey(Object obj) {
		String userId = userDirectoryService.getCurrentUser().getId();
		String sitePath = toolManager.getCurrentPlacement().getContext();
		return (String)obj+userId+sitePath;
	}
}
