package umu.sakai.umutests;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MockUMUSecureInterceptor extends umu.sakai.kernel.impl.UMUSecureInterceptor {

	private static Log log = LogFactory.getLog(MockUMUSecureInterceptor.class);
	
	// Permissions Registration
	public void init() {
		log.debug("Init MockUMUSecureInterceptor...");
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		// TODO Auto-generated method stub
		return methodInvocation.proceed();
	}
	
	@Override
	public boolean hasPermission(String permission) throws Throwable {
		return true;
	}
	
}
