package umu.sakai.umusync.tool;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.richfaces.component.UIDragSource;
import org.richfaces.event.DropEvent;
import org.sakaiproject.util.ResourceLoader;

import umu.sakai.umusync.api.ISyncManager;
import umu.sakai.umusync.api.dao.ICriteria;
import umu.sakai.umusync.api.dao.IListString;
import umu.sakai.umusync.api.dao.IPage;
import umu.sakai.umusync.api.dao.ITask;
import umu.sakai.umusync.tool.beans.UMUBeanServiceLoader;
import umu.sakai.umusync.tool.converters.CriteriaConverter;
import umu.sakai.umusync.tool.converters.HomePageConverter;
import umu.sakai.umusync.tool.converters.IgnoreFunctionsModeConverter;
import umu.sakai.umusync.tool.converters.SiteTypeConverter;


public class SyncBean extends UMUBeanServiceLoader {

	private static Log log = LogFactory.getLog(SyncBean.class);
	private Converter ignoreFunctionsModeConverter = null;
	private Converter homePageConverter = null;
	
	protected ISyncManager getSyncManager() {
		return (ISyncManager) this.getService("umu.sakai.umusync.api.ISyncManager");
	}

	protected ResourceLoader getTraductor() {
		return (ResourceLoader) this.getService("org.sakaiproject.util.ResourceLoader");
	}


	/* List of Tasks */
	private List<ITask> tasksList = null;

	public List<ITask> getTasksList() {
		if (tasksList == null) {
			refreshTasksList();
		}
		Collections.sort(tasksList);
		return tasksList;
	}

	/*  update the list */
	private void refreshTasksList() {
		tasksList = this.getSyncManager().getTasks();
	}
	
	/* Actions over a task */
	private ITask currentTask;

	/* When click on a task from list */
	public void selectTask(ActionEvent event) {		
		currentTask = (ITask) event.getComponent().getAttributes().get("task");
	}
		
	public void changeTask(ActionEvent event) {
		try {
			ITask formTask = (ITask) event.getComponent().getAttributes().get("task");
			formTask.changeAvailable();
			ITask fullTask = this.getSyncManager().getTask(formTask.getId());
			fullTask.changeAvailable();
			this.getSyncManager().addTask(fullTask);
		} catch (Throwable e) {
			log.error("Error persisting task: "+e);
		}
	}
	
	public ITask getTask() {
		return currentTask;
	}
	

	public void removeTask() {
		this.getSyncManager().delTask(currentTask.getId());
		tasksList.remove(currentTask);
		currentTask = null;
	}

	/* An empty task transient */
	public String newTask() {
		currentTask = this.getSyncManager().createTask();
		initializeCheckBoxOptions();
		initializeFunctionCheckBox();
		return "edit";
	}
	
	/* Edit the selected task */
	public String editTask() {
		// Load the task fully
		currentTask = this.getSyncManager().getTask(currentTask.getId());
		initializeCheckBoxOptions();
		initializeFunctionCheckBox();
		return "edit";
	}
	
	/* To go on execute task page */
	public String execTask() {
		currentTask = this.getSyncManager().getTask(currentTask.getId());
		informe = "";
		return "exec";
	}
	

	/* Site Types defined in our server */
	public Collection<SelectItem> getSiteTypes() {
		ArrayList<SelectItem> rtn = new ArrayList<SelectItem>();
		rtn.add(new SelectItem("", this.getTraductor().getString("todos")));
		for (String tipo : this.getSyncManager().getSiteTypes()) {
			rtn.add(new SelectItem(tipo));
		}
		rtn.add(new SelectItem(ITask.DEFAULT_USER_SITE, getTraductor().getString("defaultUserSite")));
		rtn.add(new SelectItem(ITask.ALL_USER_SITE, getTraductor().getString("allUserSite")));
		return rtn;
	}
	
	private SiteTypeConverter siteTypeConverter = null;
	public Converter getSiteTypeConverter() {
		if (siteTypeConverter == null) {
			siteTypeConverter = new SiteTypeConverter(getTraductor());
		}
		return siteTypeConverter;
	}


	/* Template realms for sites */
	public Collection<SelectItem> getSiteRealms() {
		ArrayList<SelectItem> rtn = new ArrayList<SelectItem>();
		rtn.add(new SelectItem("", getTraductor().getString("ninguno")));
		for (String tipo : this.getSyncManager().getSiteRealms()) {
			rtn.add(new SelectItem(tipo));
		}
		return rtn;
	}
	
	/* Template realms for sections */
	public Collection<SelectItem> getSectionRealms() {
		ArrayList<SelectItem> rtn = new ArrayList<SelectItem>();
		rtn.add(new SelectItem("", getTraductor().getString("ninguno")));
		for (String tipo : this.getSyncManager().getSectionRealms()) {
			rtn.add(new SelectItem(tipo));
		}
		return rtn;
	}
	
	/* Check the 'ignore site by id' property */
	public void ignoreValidator(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		FacesMessage deferredMessage = null;
		for (String id : value.toString().split(",")) {
			id = id.trim();

			if (!this.getSyncManager().checkSiteId(id)) {
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_ERROR, 
						MessageFormat.format(this.getTraductor().getString("sitewrong"),id),
						MessageFormat.format(this.getTraductor().getString("sitewrong"),id));
				if (deferredMessage != null)
					context.addMessage(null, deferredMessage);
				deferredMessage = message;
			}
		}
		if (deferredMessage != null) {
			throw new ValidatorException(deferredMessage);
		}
	}
	
	/* List of tools */
	List<String> toolsSakai;
	
	/* Options indexed by label */
	Map<String, CheckBoxOption> checkboxes;
	List<CheckBoxOption> optionsToolsAndPages;
	
	private Collection<String> getToolsSakai() {
		if (toolsSakai == null) {
			toolsSakai = (ArrayList<String>) getSyncManager().getTools();			
		}
		return toolsSakai;
	}
	
	/* Initialize the checkbox options */
	private void initializeCheckBoxOptions() {

		checkboxes = new HashMap<String, CheckBoxOption>();
		// get tools
		for (String toolSakai : getToolsSakai()) {			
			checkboxes.put(toolSakai, new CheckBoxOption(toolSakai));
		}
		// get pages
		for (IPage pageDefined : getSyncManager().getPages()) {
			checkboxes.put(pageDefined.getName(), new CheckBoxOption(pageDefined));
		}
		
		// initialize
		for (IListString tool: currentTask.getToolsToAdd()) {
			CheckBoxOption cbo = checkboxes.get(tool.getString());
			if (cbo!=null) cbo.setAddObject(tool);
		}
		for (IListString tool: currentTask.getToolsToDel()) {
			CheckBoxOption cbo = checkboxes.get(tool.getString());
			if (cbo!=null) cbo.setDelObject(tool);
		}
		for (IPage page: currentTask.getPagesToAdd()) {
			checkboxes.get(page.getName()).initAdd();
		}
		for (IListString page: currentTask.getPagesToDel()) {
			checkboxes.get(page.getString()).setDelObject(page);
		}
		
		optionsToolsAndPages = new ArrayList<CheckBoxOption>();
		for (CheckBoxOption cbo:checkboxes.values()) {
			optionsToolsAndPages.add(cbo);
		}
		Collections.sort(optionsToolsAndPages);
	}
	
	/* Sorted List of checkBoxOptions with toolNames */
	public List<CheckBoxOption> getCheckBoxOptions() {
		//updateCheckBoxOptions();
		return optionsToolsAndPages;
	}
	
	/* Sync homepage from: properties, page or not to do */
	public Collection<SelectItem> getSelectHome() {	
		ArrayList<SelectItem> rtn = new ArrayList<SelectItem>();
		rtn.add(new SelectItem(ITask.NOT_CHANGE_HOME_PAGE, getTraductor().getString("notToDo")));
		rtn.add(new SelectItem(ITask.REMOVE_THE_HOME_PAGE, getTraductor().getString("removeHP")));
		for (IPage page : getPagesList()) {
			rtn.add(new SelectItem(page.getName(), page.getName().toUpperCase()));
		}
		rtn.add(new SelectItem(ITask.PROPERTIES_HOME_PAGE, "sakai.properties"));
		return rtn;
	}
	
	public Converter getHomePageConverter() {
		if (homePageConverter == null) {
			homePageConverter = new HomePageConverter(getTraductor());
		}
		return homePageConverter;
	}
	
	/* Save all changes in database */
	public String saveTask() {
		for (CheckBoxOption cbo : optionsToolsAndPages) {			
			currentTask.delToolToAdd(cbo.removedAddTool());
			currentTask.addToolToAdd(cbo.addedAddTool());
			currentTask.delToolToDel(cbo.removedDelTool());
			currentTask.addToolToDel(cbo.addedDelTool());	
			currentTask.delPageToAdd(cbo.removedAddPage());
			currentTask.addPageToAdd(cbo.addedAddPage());	
			currentTask.delPageToDel(cbo.removedDelPage());
			currentTask.addPageToDel(cbo.addedDelPage());				
		}
		for (CheckBoxOption cbo : functionOptions) {
			currentTask.addFunctionToIgnore(cbo.addedAddTool());
			currentTask.delFunctionToIgnore(cbo.removedDelTool());
		}
		try {

			/* Save task in db, lists cascade... */
			this.getSyncManager().addTask(currentTask);
			
			/* It was persistent -> is updated */
			if (currentTask.getId()!=0) {
				refreshTasksList();
			}
			/* It is a new list */
			else {
				tasksList.add(currentTask);
			}
			currentTask = null;
		} catch (Throwable e) {			
			log.error("Saving task #"+ (currentTask.getId() != 0 ? currentTask.getId() : "transient") + " Exception: " + e);
		}
		return "task";
	}
	
	
	/* Criteria are group by criteria name */
	HashMap<String, List<ICriteria>> criteriaMap = null;
	/* A list with the keys */
	List<String> criteriaNames = null;
	
	public HashMap<String, List<ICriteria>> getCriteriaMap() {
		if (criteriaMap == null) {
			updateCriteria();
		}	
		return criteriaMap;
	}
	
	public Collection<String> getCriteriaNames() {
		if (criteriaMap == null) {
			updateCriteria();
		}	
		return criteriaNames;
	}

	private void updateCriteria() {
		if (criteriaMap==null) {
			criteriaMap = new HashMap<String, List<ICriteria>>();
			criteriaNames = new ArrayList<String>();
			for (ICriteria criterion : this.getSyncManager().getCriteria()) {
				 List<ICriteria> myList = criteriaMap.containsKey(criterion.getName())?criteriaMap.get(criterion.getName()):new ArrayList<ICriteria>();
				 if (myList.isEmpty()) criteriaNames.add(criterion.getName()); 
				 myList.add(criterion);
				 criteriaMap.put(criterion.getName(), myList);
			}
		}
	}
	
	
	private String currentCriteria;
	private String criteriaName;
	private List<ICriteria> currentCriteriaList;
	
	/* When click on a task from list */
	public void selectCriteria(ActionEvent event) {
		currentCriteria = (String) event.getComponent().getAttributes().get("name");
	}
	
	public String getCriteria() {
		return currentCriteria;
	}
	
	public void setCriteriaName(String name) {
		this.criteriaName = name;
	}
	
	public String getCriteriaName() {
		return criteriaName;
	}
	
	public String newCriteria() {
		criteriaName = null;
		currentCriteriaList = new ArrayList<ICriteria>();
		currentCriteriaList.add(this.getSyncManager().createCriteria());
		return "edit";
	}	
	
	public String editCriteria() {
		criteriaName = currentCriteria;
		currentCriteriaList = criteriaMap.get(criteriaName);
		removeCriteriaDeferred = new ArrayList<ICriteria>();
		return "edit";
	}

	public void removeCriteria() {
		for (ICriteria del : criteriaMap.get(currentCriteria)) {
			if (del.getId() != 0) {
				this.getSyncManager().delCriteria(del.getId());
			}
		}
		criteriaMap.remove(currentCriteria);
		criteriaNames.remove(currentCriteria);
		currentCriteria = null;
	}
	
	public List<ICriteria> getCurrentCriteriaList() {
		return currentCriteriaList;
	}
	
	public boolean getIsNewCriteria() {	
		return (currentCriteria==null);
	}	
	
	public String returnFromCriteria() {
		criteriaMap=null;
		updateCriteria();
		return "criteria";
	}
	
	public void criteriaNameValidator(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		
		String name =  value.toString();
		
		if (!name.equals(currentCriteria) && criteriaMap.containsKey(name)) { 
			throw new ValidatorException(new FacesMessage(
					FacesMessage.SEVERITY_ERROR, 
					this.getTraductor().getString("criteriaexists")+": "+value, 
					this.getTraductor().getString("criteriaexists")+": "+value));
		}
	}

	
	

	
	
	/* SelectItem: CriteriaName - CriteriaListObject */
	public Collection<SelectItem> getSelectCriteria() {	
		ArrayList<SelectItem> rtn = new ArrayList<SelectItem>();
		rtn.add(new SelectItem(new ArrayList<ICriteria>(), getTraductor().getString("ninguno")));
		for (String name : getCriteriaNames()) {
			rtn.add(new SelectItem(criteriaMap.get(name), name));
		}
		return rtn;
	}
	
	/* Converter class to getObject in DAO */
	public Converter getCriteriaConverter() {
		return new CriteriaConverter(criteriaMap, getTraductor().getString("ninguno"));
	}
	
	
	private Map<String, Comparator> comparatorMap = null;
	private Collection<SelectItem> comparatorSelect = null;
	
	// "=", "!", "M", "E", "N", "<", ">" 
	private void initComparator() {
		comparatorMap = new HashMap<String, Comparator>();
		comparatorMap.put(ISyncManager.COMPARATOR_EQUALS, new Comparator("comparatorEq", 2, this.getTraductor()));
		comparatorMap.put(ISyncManager.COMPARATOR_NOT_EQUALS, new Comparator("comparatorNe", 2, this.getTraductor()));
		comparatorMap.put(ISyncManager.COMPARATOR_LESS_THAN, new Comparator("comparatorLt", 2, this.getTraductor()));
		comparatorMap.put(ISyncManager.COMPARATOR_GRATHER_THAN, new Comparator("comparatorGt", 2, this.getTraductor()));		
		comparatorMap.put(ISyncManager.COMPARATOR_MATCHES, new Comparator("comparatorMatches", 2, this.getTraductor()));
		comparatorMap.put(ISyncManager.COMPARATOR_EXISTS, new Comparator("comparatorExists", 1, this.getTraductor()));
		comparatorMap.put(ISyncManager.COMPARATOR_NOT_EXISTS, new Comparator("comparatorNonExsits", 1, this.getTraductor()));
		
		comparatorSelect = new ArrayList<SelectItem>();
		for (String comparator : comparatorMap.keySet()) {
			comparatorSelect.add(new SelectItem(comparator, comparatorMap.get(comparator).getSignature()));
		}
			
	}
	
	public Map<String, Comparator> getComparatorMap() {
		if (comparatorMap==null) {
			initComparator();
		}
		return comparatorMap;
	}
	
	public Collection<SelectItem> getComparatorSelect() {	
		if (comparatorMap==null) {
			initComparator();
		}
		return comparatorSelect;
	}
	
	public int getCriteriaSize() {
		return getCurrentCriteriaList().size();
	}
	
	public void addNewCriterion() {
		currentCriteriaList.add(this.getSyncManager().createCriteria());		
	}
	
	
	private ICriteria currentCriterion;
	
	public void selectCriterion(ActionEvent event) {
		currentCriterion = (ICriteria) event.getComponent().getAttributes().get("criterion");
	}
	
	private ArrayList<ICriteria> removeCriteriaDeferred;
	
	public void removeCriterion() {
		if (currentCriterion.getId()!=0) {
			removeCriteriaDeferred.add(currentCriterion);			
		}
		currentCriteriaList.remove(currentCriterion);
	}
	
	public String saveCriteria() {
		if (currentCriteria!=null){
			for (ICriteria deferred : removeCriteriaDeferred) {
				this.getSyncManager().delCriteria(deferred.getId());
			}			
		}
		for (ICriteria ic : currentCriteriaList) {
			ic.setName(criteriaName);
			if (comparatorMap.get(ic.getComparador()).getArity()==1) ic.setValor(null);
			ic.setId(this.getSyncManager().addCriteria(ic));
		}
		return returnFromCriteria();
	}
	
	
	/* List of Pages */
	private List<IPage> pagesList = null;

	public List<IPage> getPagesList() {
		if (pagesList == null) {
			refreshPagesList();
		}	
		return pagesList;
	}

	/*  update the list */
	private void refreshPagesList() {
		pagesList = this.getSyncManager().getPages();
	}
	
	
	
	
	public Collection<SelectItem> getSelectFunctionsMode() {	
		ArrayList<SelectItem> rtn = new ArrayList<SelectItem>();
		rtn.add(new SelectItem(ITask.NOT_CHANGE_FUNCTION_MODE, getTraductor().getString("notToDo")));
		rtn.add(new SelectItem(ITask.PROPERTIES_FUNCTION_MODE, getTraductor().getString("fromproperties")));
		rtn.add(new SelectItem(ITask.CUSTOMLIST_FUNCTION_MODE, getTraductor().getString("custom")));
		return rtn;
	}
	
	public Converter getIgnoreFunctionsModeConverter() {
		if (ignoreFunctionsModeConverter == null) {
			ignoreFunctionsModeConverter = new IgnoreFunctionsModeConverter(getTraductor());
		}
		return ignoreFunctionsModeConverter;
	}
	
	
	
	/* Options indexed by label */
	Map<String, CheckBoxOption> functionCheckboxes;
	List<CheckBoxOption> functionOptions;
	List<String> registeredFunctions;
	
	private List<String> getRegisteredFunctions() {
		if (registeredFunctions == null) {
			registeredFunctions = getSyncManager().getRegisteredFunctions();			
		}
		return registeredFunctions;
	}
	
	/* Initialize the checkboxes for functions */
	private void initializeFunctionCheckBox() {

		functionCheckboxes = new HashMap<String, CheckBoxOption>();
		// get all functions
		for (String function : getRegisteredFunctions()) {			
			functionCheckboxes.put(function, new CheckBoxOption(function));
		}		
		
		// initialize
		for (IListString function: currentTask.getIgnoredFunctions()) {
			functionCheckboxes.get(function.getString()).setAddObject(function);
		}
				
		functionOptions = new ArrayList<CheckBoxOption>();
		for (CheckBoxOption cbo:functionCheckboxes.values()) {
			functionOptions.add(cbo);
		}
		// Alphabetic sort
		Collections.sort(functionOptions);
	}
	
	/* Sorted List of checkBoxOptions with toolNames */
	public List<CheckBoxOption> getFunctionCheckBox() {
		return functionOptions;
	}
	
	
	IPage currentPage = null;
	
	/* When click on a task from list */
	public void selectPage(ActionEvent event) {
		currentPage = (IPage) event.getComponent().getAttributes().get("page");
	}
	
	public IPage getPage() {
		return currentPage;
	}

	public void removePage() {
		this.getSyncManager().delPage(currentPage.getName());
		pagesList.remove(currentPage);
		currentPage = null;
	}

	/* myPageName: USED IN VALIDATOR */
	private String myPageName = null;
	
	/* An empty task transient */
	public String newPage() {
		currentPage = this.getSyncManager().createPage();
		myPageName = null;
		return "edit";
	}
	
	/* Edit the selected task */
	public String editPage() {
		myPageName = currentPage.getName();
		currentPage = this.getSyncManager().getPage(myPageName);
		return "edit";
	}
	
	public boolean isPageEditing() {
		return myPageName!=null;
	}
	
	public void pageNameValidator(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		
		if (ITask.NOT_CHANGE_HOME_PAGE.equals(value.toString())
				|| ITask.PROPERTIES_HOME_PAGE.equals(value.toString())
				|| ITask.REMOVE_THE_HOME_PAGE.equals(value.toString())) {
			throw new ValidatorException(new FacesMessage(
					FacesMessage.SEVERITY_ERROR,
					this.getTraductor().getString("invalidValue")+": "+value,
					this.getTraductor().getString("invalidValue")+": "+value));
		}
		for (IPage page : pagesList) {
			if (!page.getName().equals(myPageName) && page.getName().equals(value.toString())) {
			throw new ValidatorException(new FacesMessage(					
					FacesMessage.SEVERITY_ERROR,
					this.getTraductor().getString("pageexists")+": "+value,
					this.getTraductor().getString("pageexists")+": "+value));
			}
		}
		if (getToolsSakai().contains(value.toString())) {
			throw new ValidatorException(new FacesMessage(
					FacesMessage.SEVERITY_ERROR,
					this.getTraductor().getString("toolexists")+": "+value,
					this.getTraductor().getString("toolexists")+": "+value));
		}
	}
	
	Collection<SelectItem> toolsSakaiOpciones = null;
	public Collection<SelectItem> getToolsSakaiOpciones() {
		if (toolsSakaiOpciones == null) {
			toolsSakaiOpciones = new ArrayList<SelectItem>();
			for (String t : getToolsSakai()) {
				toolsSakaiOpciones.add(new SelectItem(t));
			}
		}
		return toolsSakaiOpciones;
	}
	
	private String selectedTool;	

	public void setToolElegida(String tool) {
		selectedTool = tool;
	}

	public String getToolElegida() {
		return selectedTool;
	}

	public void addToolInPage() {
		this.getPage().addToolInLeftColumn(null, selectedTool);
	}
	
	// Actions over one tool into the editing page
	private IListString toolInPage;

	public void selectTool(ActionEvent event) {
		toolInPage = (IListString) event.getComponent().getAttributes().get("tool");
	}

	public void eliminar1() {
		this.getPage().delToolFromLeftColumn(toolInPage);
	}

	public void eliminar2() {
		this.getPage().delToolFromRightColumn(toolInPage);
	}

	
	// Drag and drop support
	public void drop1Top(DropEvent ev) {
		this.getPage().addToolInLeftColumn(0, delDragItem(ev));
	}
	
	public void drop1(DropEvent ev) {		
		// One object dropped over itself
		if (ev.getDragValue()==ev.getDropValue()) return;
		
		// Get position where was dropped
		int pos = this.getPage().getPos((IListString)ev.getDropValue());
		
		this.getPage().addToolInLeftColumn(pos, delDragItem(ev));
	}

	public void drop2Top(DropEvent ev) {
		this.getPage().addToolInRightColumn(0, delDragItem(ev));
	}
	
	public void drop2(DropEvent ev) {
		// One object dropped over itself
		if (ev.getDragValue()==ev.getDropValue()) return;
		
		// Get position where was dropped
		int pos = this.getPage().getPos((IListString)ev.getDropValue());
		
		this.getPage().addToolInRightColumn(pos, delDragItem(ev));
	}

	private String delDragItem(DropEvent ev) {
		IListString item = (IListString) ev.getDragValue();
		String dragType = ((UIDragSource) ev.getDragSource()).getType();
		
		if ("left".equals(dragType)) {
			this.getPage().delToolFromLeftColumn(item);
		} else {
			this.getPage().delToolFromRightColumn(item);
		}
		return item.getString();
	}

	public String savePage() {
		try {
			if (myPageName==null) {
				this.getSyncManager().addNewPage(currentPage);
			} else {
				this.getSyncManager().addUpdPage(currentPage);
			}
			refreshPagesList();
		} catch (Throwable e) {
			log.error("Saving page "+ currentPage.getName() +" Exception: " + e);
		}
		return "page";
	}
	
	/* Execute one task */
	public void executeTask() {
		currentTask = this.getSyncManager().getTaskAndRelatedPages(currentTask.getId());
		try {
			long inicio = System.currentTimeMillis();
			informe = "OK: " + getTraductor().getString("syncronized")+ " "
					+ getSyncManager().syncSites(currentTask) + " "					
					+ getTraductor().getString("sitios")+ " :: "
					+ (System.currentTimeMillis() - inicio) / 1000
					+ " " + getTraductor().getString("segundos");
			log.debug("INFORME: "+informe);
		} catch (Throwable e) {
			e.printStackTrace();
			informe = getTraductor().getString("execerror")+": " + e;
			log.debug("INFORME: "+informe);
		}
	}
	
	private String informe = "";
	public String getInforme() { return informe; }
	
}
