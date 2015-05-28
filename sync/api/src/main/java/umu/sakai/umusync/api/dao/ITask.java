package umu.sakai.umusync.api.dao;

import java.util.List;

public interface ITask extends Comparable<ITask> {
	
	long getId();
	void setId(long id);

	boolean getAvailable();
	void changeAvailable();

	String getTipo();
	void setTipo(String tipo);
	
	static final String DEFAULT_USER_SITE = "umusync.type.defaultUserSite";
	static final String ALL_USER_SITE = "umusync.type.allUserSite";

	List<IListString> getToolsToAdd();
	void addToolToAdd(String toolId);
	boolean delToolToAdd(IListString toolId);
	
	List<IListString> getToolsToDel();
	void addToolToDel(String toolId);
	boolean delToolToDel(IListString toolId);
	
	List<IListString> getPagesToDel();
	void addPageToDel(String pageName);
	boolean delPageToDel(IListString pageName);
	
	String getIgnoreSitesById();
	void setIgnoreSitesById(String ignore);
	List<IListString> getIgnoreById();
	
	List<ICriteria> getCriteria();
	void setCriteria(List<ICriteria> criteria);
	
	
	List<IPage> getPagesToAdd();
	void addPageToAdd(IPage page);
	boolean delPageToAdd(IPage page);

	
	String getRealmSite();
	void setRealmSite(String realmSite);

	String getRealmSection();
	void setRealmSection(String realmSection);
	
	boolean getSyncInto();
	void setSyncInto(boolean syncInto);
	
	String getSyncHome();
	void setSyncHome(String syncHome);
	
	/* SyncHome: This options or a page name. */
	static final String NOT_CHANGE_HOME_PAGE = ""; // saved in ORACLE as NULL!!
	static final String REMOVE_THE_HOME_PAGE = "umusync.homepage.remove";	
	static final String PROPERTIES_HOME_PAGE = "umusync.homepage.properties";
	
	
	String getComments();
	void setComments(String comments);
	
	String getIgnoreFunctionsMode();
	void setIgnoreFunctionsMode(String ignoreFunctions);
	/* valid values for IgnoreFunctionsMode */
	static final String NOT_CHANGE_FUNCTION_MODE = "N";
	static final String CUSTOMLIST_FUNCTION_MODE = "C";
	static final String PROPERTIES_FUNCTION_MODE = "P";
	
	List<IListString> getIgnoredFunctions();	
	void addFunctionToIgnore(String functionName);	
	boolean delFunctionToIgnore(IListString function);

	
	/* simulates orphan removal from JPA 2.0 */
	/* get items deleted in oneToMany relations from this task*/
	List<IListString> flushOrphanRemovalEntities();	
}