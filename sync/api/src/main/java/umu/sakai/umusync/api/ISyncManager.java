package umu.sakai.umusync.api;

import java.util.Collection;
import java.util.List;

import umu.sakai.umusync.api.dao.ICriteria;
import umu.sakai.umusync.api.dao.IPage;
import umu.sakai.umusync.api.dao.ITask;

public interface ISyncManager {
	
	// comparators in properties
	static final String COMPARATOR_EQUALS = "=";
	static final String COMPARATOR_NOT_EQUALS = "!";
	static final String COMPARATOR_LESS_THAN = "<";
	static final String COMPARATOR_GRATHER_THAN = ">";
	static final String COMPARATOR_MATCHES = "M";
	static final String COMPARATOR_EXISTS = "E";
	static final String COMPARATOR_NOT_EXISTS = "N";

	// reserved properties
	static final String SITE_TITLE = "SITE_TITLE";
	
	
	/**
	 * Do the sync process of one task. 
	 * @param tarea sync task, it can contains filter, realms, tools, pages, etc,..
	 * @return String with nm/ns where:
	 * 			nm: number of sites saved after modify it.
	 * 			ns: number of sites that matches criteria to sync.
	 * @throws Throwable Throws an exception if any problem occurs.
	 */
	String syncSites(ITask tarea) throws Throwable;

	/**
	 * Get all task from database.
	 * @return Collection of registered tasks.
	 */
	List<ITask> getTasks();

	/**
	 * Get all criteria from database.
	 * @return Collection of registered criterion.
	 */
	List<ICriteria> getCriteria();

	/**
	 * Get all pages from database.
	 * @return Collection of registered pages.
	 */
	List<IPage> getPages();
	
	
	/**
	 * Lazy Load: Load fully the task with identifier taskId.
	 * @param taskId Identifier for tasks.
	 * @return The task linked in database.
	 */
	ITask getTask(long taskId);
	
	/**
	 * Lazy Load: Load fully the page with identifier name.
	 * @param name Identifier for pages.
	 * @return The page linked in database.
	 */
	IPage getPage(String name);
	
	/**
	 * Lazy Load: Load fully the task with identifier taskId.
	 * and includes the load fully of related pages. 
	 * @param taskId Identifier for tasks.
	 * @return The task linked in database.
	 */
	ITask getTaskAndRelatedPages(long taskId);
	
	/**
	 * Get all tools from Sakai services.
	 * @return Collection of registered tools.
	 */
	Collection<String> getTools();
	
	/**
	 * Get all types of site from Sakai services.
	 * @return Collection of registered site type.
	 */
	Collection<String> getSiteTypes();
	
	/**
	 * @return List with the name of all registered functions.
	 */
	List<String> getRegisteredFunctions();
	
	/**
	 * Get all template realms for sites registered in Sakai services.
	 * @return realms that starts with "!site.template"
	 */
	Collection<String> getSiteRealms();
	
	/**
	 * Get all template realms for groups registered in Sakai services.
	 * @return realms that starts with "!group.template"
	 */
	Collection<String> getSectionRealms();

	/** 
	 * Create an empty task.
	 * @return empty task
	 */
	ITask createTask();	
	
	/**
	 * Persist in database the task. If task exists do update, else do insert.
	 * @param nueva Task to persist.
	 */
	void addTask(ITask nueva) throws Throwable;
	
	/** 
	 * Remove the task whose identifier is the parameter received.
	 * If that task doesn't exist, do nothing.
	 * @param pk Task identifier.
	 */
	void delTask(long pk);
	
	/** 
	 * Create an empty criterion.
	 * @return empty criterion.
	 */
	ICriteria createCriteria();
	
	/** 
	 * Persist criterion in database. If criterion exists do update, else do insert.
	 * @param nueva Criterion to persist
	 * @return id Identifier of persistent object.
	 */
	long addCriteria(ICriteria nueva);
	
	/** 
	 * Remove the criterion whose identifier is the parameter received.
	 * If that criterion doesn't exist, do nothing.
	 * @param pk Criterion identifier.
	 */
	void delCriteria(long pk);
	
	/** 
	 * Create an empty page.
	 * @return empty page.
	 */
	IPage createPage();

	/** 
	 * Persist a new page in database.
	 * @param p New page to persist.
	 * @throws Throwable Throws an exception if any problem occurs. 
	 */
	void addNewPage(IPage p)  throws Throwable;;
	
	/** 
	 * Update tools of an existing page in database.
	 * @param p Modified page to persist.
	 * @throws Throwable Throws an exception if any problem occurs.
	 */
	void addUpdPage(IPage p)  throws Throwable;;
	
	/** 
	 * Remove the page whose name is the parameter received.
	 * If that page doesn't exist, do nothing.
	 * @param pk Name of the page.
	 */
	void delPage(String pk);
	
	
	/** 
	 * Check if the site with id siteId exists.
	 * @param siteId identifier of the site that we want check.
	 * @return true if site exists, false if site doesn't exist.
	 */
	boolean checkSiteId(String siteId);

	// QUARTZ JOB
	public String jobName();
	public void doit() throws Throwable;	
	
}
