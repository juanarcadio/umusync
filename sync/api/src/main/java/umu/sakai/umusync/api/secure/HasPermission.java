package umu.sakai.umusync.api.secure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for advising what entities should be securized.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HasPermission {

	/**
	 * @return the tool permission needed
	 */
	String name() default "";
	/**
	 * 
	 * @return all permissions needed
	 */
	String[] all() default "";
	
	/**
	 * @return one permission needed
	 */
	String[] oneof() default "";
	
	/**
	 * @return the exception to be thrown in case of errors
	 */
	Class<?> exception() default PermissionException.class;
	
	/**
	 * 
	 * @return error message
	 */
	String message() default "not_permited";
	
	/**
	 * @return is the admin user
	 */
	boolean isAdmin() default false;
	
}