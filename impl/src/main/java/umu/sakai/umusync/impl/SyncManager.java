package umu.sakai.umusync.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import umu.sakai.umusync.api.secure.HasPermission;
import umu.sakai.umusync.api.ISyncManager;
import umu.sakai.umusync.api.dao.ICriteria;
import umu.sakai.umusync.api.dao.IListString;
import umu.sakai.umusync.api.dao.IPage;
import umu.sakai.umusync.api.dao.ITask;
import umu.sakai.umusync.dao.Criteria;
import umu.sakai.umusync.dao.Page;
import umu.sakai.umusync.dao.Task;

public class SyncManager implements ISyncManager, Job {

	private static Log log = LogFactory.getLog(SyncManager.class);

	// User who execute sync tasks
	private final static String SYNC_USER = "admin";

	// Sakai Services
	protected SiteService siteService;
	protected AuthzGroupService authzGroupService;
	protected ToolManager toolManager;
	protected FunctionManager functionManager;
	protected ServerConfigurationService serverConfigurationService;
	protected SessionManager sessionManager;
	protected Session sakaiSession;

	private static final String IGNORE_FUNCTIONS_PROPERTY = "umusync.ignore.functions";
	private static final String DEFAULT_IGNORE_FUNCTIONS_VALUE = "annc.all.groups,annc.delete.any,annc.delete.own,annc.new,annc.read,annc.read.drafts,annc.revise.any,annc.revise.own,asn.all.groups,asn.delete,asn.grade,asn.new,asn.read,asn.receive.notifications,asn.revise,asn.share.drafts,asn.submit,calendar.all.groups,calendar.delete.any,calendar.delete.own,calendar.import,calendar.new,calendar.read,calendar.revise.any,calendar.revise.own,calendar.subscribe,chat.delete.any,chat.delete.channel,chat.delete.own,chat.new,chat.new.channel,chat.read,chat.revise.channel,content.all.groups,content.delete.any,content.delete.own,content.hidden,content.new,content.read,content.revise.any,content.revise.own,poll.add,poll.deleteAny,poll.deleteOwn,poll.editAny,poll.editOwn,poll.vote,rwiki.admin,rwiki.create,rwiki.read,rwiki.superadmin,rwiki.update";

	@PersistenceContext(unitName = "umusync-jpa")
	protected EntityManager entityManager;

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public ToolManager getToolManager() {
		return toolManager;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public AuthzGroupService getAuthzGroupService() {
		return authzGroupService;
	}

	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	public FunctionManager getFunctionManager() {
		return functionManager;
	}

	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	/* Consultas DAO */
	public List<ITask> getTasks() {
		return entityManager.createNamedQuery("listTasks").getResultList();
	}

	public List<ICriteria> getCriteria() {
		return entityManager.createNamedQuery("listCriteria").getResultList();
	}

	public List<IPage> getPages() {
		return entityManager.createNamedQuery("listPages").getResultList();
	}

	/**
	 * Lazy load, we need a transaction to do it.
	 */
	public ITask getTask(long taskId) {
		ITask task = entityManager.getReference(Task.class, taskId);

		task.getCriteria().size();
		task.getToolsToAdd().size();
		task.getToolsToDel().size();
		task.getPagesToAdd().size();
		task.getPagesToDel().size();
		task.getIgnoreById().size();
		task.getIgnoreSitesById().length();
		task.getIgnoredFunctions().size();

		return task;
	}

	public IPage getPage(String name) {
		IPage page = entityManager.getReference(Page.class, name);

		page.getLeftColumn().size();
		page.getRightColumn().size();
		page.numberingAfterLoad();

		return page;
	}
	
	public ITask getTaskAndRelatedPages(long taskId) {
		ITask rtn = getTask(taskId);
		for (IPage page : rtn.getPagesToAdd()) {
			page = getPage(page.getName());
		}
		return rtn;
	}

	
	/* Tasks */
	public ITask createTask() {
		return new Task();
	}

	public void addTask(ITask nueva) throws Throwable {

		try {
			/* To remove all orphan entities from this task (they are detached) */
			for (IListString entity : nueva.flushOrphanRemovalEntities()) {
				entityManager.remove(entityManager.getReference(entity.getClass(), entity.getId()));
			}
			/* Update the task entity */
			if (nueva.getId() != 0) {
				entityManager.merge(nueva);
			}

			/* New task entity */
			else {
				entityManager.persist(nueva);
			}
			entityManager.flush();

		} catch (InvalidStateException ise) {
			Throwable lanzar = null;
			for (InvalidValue iv : ise.getInvalidValues()) {
				lanzar = new Throwable(iv.getPropertyName() + ": "
						+ iv.getMessage(), lanzar);
			}
			throw lanzar;
		}
	}

	public void delTask(long quita) {
		try {
			entityManager.remove(getTask(quita));
		} catch (EntityNotFoundException e) {
			if (log.isDebugEnabled()) log.debug("No existe: " + e);
		}
	}

	/* Criteria */
	public ICriteria createCriteria() {
		return new Criteria();
	}

	public long addCriteria(ICriteria nueva) {
		return entityManager.merge(nueva).getId();
	}

	public void delCriteria(long quita) {
		try {
			entityManager.remove(entityManager.getReference(Criteria.class,
					quita));
		} catch (EntityNotFoundException e) {
			if (log.isDebugEnabled()) log.debug("No existe: " + e);
		}
	}

	/* Pages */
	public IPage createPage() {
		return new Page();
	}

	public void addNewPage(IPage nueva) throws Throwable {
		try {
					
			nueva.numberingBeforeSave();
			entityManager.persist(nueva);
			entityManager.flush();
			
		} catch (InvalidStateException ise) {
			Throwable lanzar = null;
			for (InvalidValue iv : ise.getInvalidValues()) {
				lanzar = new Throwable(iv.getMessage(), lanzar);
			}
			throw lanzar;
		}
	}
	
	public void addUpdPage(IPage nueva) throws Throwable {
		try {
			/* To remove all orphan entities from this task (they are detached) */
			for (IListString entity : nueva.flushOrphanRemovalEntities()) {
				entityManager.remove(entityManager.getReference(entity.getClass(), entity.getId()));
			}
			
			nueva.numberingBeforeSave();
			entityManager.merge(nueva);
			entityManager.flush();
			
		} catch (InvalidStateException ise) {
			Throwable lanzar = null;
			for (InvalidValue iv : ise.getInvalidValues()) {
				lanzar = new Throwable(iv.getMessage(), lanzar);
			}
			throw lanzar;
		}
	}

	public void delPage(String quita) {
		try {
			entityManager.remove(getPage(quita));
		} catch (EntityNotFoundException e) {
			if (log.isDebugEnabled()) log.debug("No existe: " + e);
		}
	}

	/* Lista de tipos de site que existen */
	public Collection<String> getSiteTypes() {
		return this.getSiteService().getSiteTypes();
	}

	/* Lista de realm plantilla que empiezan por !site */
	public Collection<String> getSiteRealms() {
		Collection<AuthzGroup> deSites = this.getAuthzGroupService()
				.getAuthzGroups("!site.template", null);
		List<String> rtn = new ArrayList<String>();
		for (AuthzGroup a : deSites) {
			rtn.add(a.getId());
		}

		return rtn;
	}

	/* Lista de realm plantilla que empiezan por !group */
	public Collection<String> getSectionRealms() {
		Collection<AuthzGroup> deSections = this.getAuthzGroupService()
				.getAuthzGroups("!group.template", null);
		List<String> rtn = new ArrayList<String>();
		for (AuthzGroup a : deSections) {
			rtn.add(a.getId());
		}

		return rtn;
	}

	/* Lista de tools disponibles */
	public Collection<String> getTools() {
		Collection<Tool> herramientas = this.getToolManager().findTools(null,
				null);
		List<String> rtn = new ArrayList<String>();
		for (Tool t : herramientas) {
			rtn.add(t.getId());
		}
		Collections.sort(rtn);

		return rtn;
	}

	public List<String> getRegisteredFunctions() {
		return this.getFunctionManager().getRegisteredFunctions();
	}

	/* Comprueba la existencia del sitio siteId */
	public boolean checkSiteId(String siteId) {
		try {
			this.getSiteService().getSite(siteId);
			return true;
		} catch (IdUnusedException ex) {
			return false;
		}
	}

	// Quartz JOB
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			loginToSakai();
			doit();
		} catch (Throwable t) {
			log.error("Execution: "+t);
			throw new JobExecutionException(t.toString());
		} finally {
			logoutFromSakai();
		}
	}
	
	protected void loginToSakai() {
		sakaiSession = getSessionManager().getCurrentSession();
		sakaiSession.setUserId(SYNC_USER);
		sakaiSession.setUserEid(SYNC_USER);
		getAuthzGroupService().refreshUser(SYNC_USER);

	}

	protected void logoutFromSakai() {
		sakaiSession.invalidate();
	}
	
	// Job Scheduler: can execute if have an admin session
	@HasPermission(isAdmin = true)
	public void doit() throws Throwable {
		// Loop the tasks stored in database
		for (ITask tarea : getTasks()) {
			// Execute all the available tasks
			if (tarea.getAvailable()) {
				// Load task fully and execute syncSites
				syncSites(getTaskAndRelatedPages(tarea.getId()));
			}
		}
		if (log.isDebugEnabled()) log.debug("Finish!");
	}

	@HasPermission(isAdmin = true)
	public String jobName() {
		return "SyncSites Job";
	}

	
	/**
	 * Para ejecutar este metodo debes de tener sesion de admin. 
	 */

	@HasPermission(isAdmin = true)
	public String syncSites(ITask tarea) throws Throwable {

		if (log.isDebugEnabled()) log.debug("syncSites #TaskId: " + tarea.getId() + " - " + tarea.getComments());

		// Cargamos los realms plantilla
		AuthzGroup baseAuthzGroup, sectionAuthzGroup;

		try {
			baseAuthzGroup = getAuthzGroupService().getAuthzGroup(tarea.getRealmSite());
			if (log.isDebugEnabled()) log.debug("Base AuthzGroup [" + baseAuthzGroup.getId() + "]");
		} catch (GroupNotDefinedException gnde) {
			baseAuthzGroup = null;
			log.warn("Base AuthzGroup not defined." + gnde);
		}

		try {
			sectionAuthzGroup = getAuthzGroupService().getAuthzGroup(
					tarea.getRealmSection());
			if (log.isDebugEnabled()) log.debug("Section AuthzGroup [" + sectionAuthzGroup.getId() + "]");
		} catch (GroupNotDefinedException gnde) {
			sectionAuthzGroup = null;
			log.warn("Section AuthzGroup not defined." + gnde);
		}

		int ns = 0; // sitios que cumplen las condiciones
		int nm = 0; // sitios almacenados con cambios

		// Cargamos los sitios del tipo especificado (tipo=null -> todos los sitios):
		String type = tarea.getTipo();
		if (ITask.DEFAULT_USER_SITE.equals(type) || ITask.ALL_USER_SITE.equals(type)) {
			type = null;
		}
		
		// criteria: contains (SITE_TITLE)
		String criteria = null;
		
		// propertyCriteria: HashMap<String, String> / null :
		HashMap<String, String> propertyCriteria = new HashMap<String, String>();
		
		for (ICriteria c : tarea.getCriteria()) {
			if (COMPARATOR_EQUALS.equals(c.getComparador())) {
				propertyCriteria.put(c.getProperty(), c.getValor());
			} else if (SITE_TITLE.equals(c.getProperty()) && COMPARATOR_MATCHES.equals(c.getComparador())) {
				criteria = c.getValor();
			}
		}
		
		if (propertyCriteria.isEmpty()) {
			propertyCriteria = null;
		} 
		
		List<Site> sites = siteService.getSites(SiteService.SelectionType.ANY,
				type, criteria, propertyCriteria, SiteService.SortType.NONE, null);		

		for (Site lazy : sites) {
			// Cargamos el sitio completo (getSites hay veces que no lo carga completo)
			Site s = siteService.getSite(lazy.getId());
			// Comprobamos que cumple las condiciones
			if (isSynchronizable(s, tarea)) {
				ns++;
				log.info(ns + ") Synchronizing site [" + s.getId() + "] type: " + s.getType());

				if (synchronizeSite(s, baseAuthzGroup, sectionAuthzGroup,
						getIgnoreFunctions(tarea.getIgnoreFunctionsMode(),tarea.getIgnoredFunctions()), 
						tarea.getToolsToAdd(),	tarea.getToolsToDel(),	tarea.getPagesToAdd(),
						tarea.getPagesToDel(), tarea.getSyncInto(),	tarea.getSyncHome())) {
					nm++;
				}
			}
		}

		return nm + "/" + ns;
	}

	private List<String> getIgnoreFunctions(String mode,
			List<IListString> functions) {
		ArrayList<String> rtn = new ArrayList<String>();

		if (ITask.NOT_CHANGE_FUNCTION_MODE.equals(mode))
			return rtn;
		if (ITask.PROPERTIES_FUNCTION_MODE.equals(mode)) {
			String functionsInProperties = this.getServerConfigurationService().getString(IGNORE_FUNCTIONS_PROPERTY, DEFAULT_IGNORE_FUNCTIONS_VALUE);
			for (String function : functionsInProperties.split(",")) {
				function = function.trim();
				if (!"".equals(function)) {
					rtn.add(function);
				}
			}

		} else if (ITask.CUSTOMLIST_FUNCTION_MODE.equals(mode)) {
			for (IListString function : functions) {
				rtn.add(function.getString());
			}
		} else {
			log.error("IgnoreMode unknown: " + mode);
		}
		return rtn;
	}

	/**
	 ** El sitio se sincroniza si: 
	 *		- Es del tipo correspondiente. 
	 *		- Su propiedad synchronize no es off.
	 *		- Se cumplen todas las propiedades de "criteria"
	 */
	private boolean isSynchronizable(Site s, ITask t) {
		// Is default user Site?
		if (ITask.DEFAULT_USER_SITE.equals(t.getTipo()) && 
				!(siteService.isUserSite(s.getId()) &&
				s.getType()==null)) {
			if (log.isDebugEnabled()) log.debug(s.getId()+" NO ES DEFAULT USER SITE!");
			return false;
		}
		
		// Is an user Site?
		if (ITask.ALL_USER_SITE.equals(t.getTipo()) && 
				!siteService.isUserSite(s.getId())) {
			if (log.isDebugEnabled()) log.debug(s.getId()+" NO ES USER SITE!");
			return false;
		}
		
		List<IListString> ignoreById = t.getIgnoreById();
		Collection<ICriteria> criteria = t.getCriteria();

		// Property off to not synchronize
		if ("off".equals(s.getProperties().get("synchronize"))) {
			log.info("(Property synchronize:off) Ignoring site " + s.getId());
			return false;
		}

		// Check the list of siteId to ignore
		for (String id : getList(ignoreById)) {
			if (s.getId().equals(id)) {
				log.info("(ignoreById) Ignoring site " + id);
				return false;
			}
		}

		// criteria is empty -> can sync this site
		if (criteria == null) {
			return true;
		}

		if (log.isDebugEnabled()) log.debug("IN THE FILTRO " + s.getId());
		// Ignore sites which criteria don't match
		for (ICriteria c : criteria) {	
			String siteValue = (String) s.getProperties().get(c.getProperty());
			String comparator = c.getComparador();
			if (c.getValor()==null) c.setValor("");
			
			// Included in siteService previous call: propertyCriteria and criteria params  
                        if ((COMPARATOR_EQUALS.equals(comparator) || COMPARATOR_MATCHES.equals(comparator))
					&& SITE_TITLE.equals(c.getProperty())) {
				continue;
			}
			
			// Property non exists
			if (siteValue == null) {				
				// comparator: non exists -> check next
				// comparator: different -> check next
				if (COMPARATOR_NOT_EXISTS.equals(comparator) || COMPARATOR_NOT_EQUALS.equals(comparator)) {
					continue;
				}
				// comparator: exists -> not to sync
				else if (COMPARATOR_EXISTS.equals(comparator) || COMPARATOR_LESS_THAN.equals(comparator) || COMPARATOR_GRATHER_THAN.equals(comparator)) {
					return false;
				}
				else if (COMPARATOR_EQUALS.equals(comparator)) {
					if (c.getValor().equals("")) continue;
					return false;
				} else if (COMPARATOR_MATCHES.equals(comparator) && !"".matches(c.getValor())) {
					return false;
				}			
			// Property exists && comparator: exists
			} else if (COMPARATOR_EXISTS.equals(comparator)) {
				continue;
			}
			// operator MATCHES using regex
			else if (COMPARATOR_MATCHES.equals(comparator)) {
				if (!siteValue.matches(c.getValor())) {
						return false;
				}				
			} else {
				// Comparamos los valores de la property				
				int compare = siteValue.compareTo(c.getValor());
				// Son iguales
				if (compare == 0) {
					if (!COMPARATOR_EQUALS.equals(comparator)) {
						return false;
					}
				}
				// El site es mayor que el filtro
				else if (compare > 0) {
					if (comparator.equals(COMPARATOR_GRATHER_THAN))
						continue;
					if (comparator.equals(COMPARATOR_NOT_EQUALS))
						continue;
					return false;
				}
				// El site es menor que el filtro
				else {
					if (comparator.equals(COMPARATOR_LESS_THAN))
						continue;
					if (comparator.equals(COMPARATOR_NOT_EQUALS))
						continue;
					return false;
				}
			}
		}

		// Cumple las condiciones
		return true;
	}

	// Sincroniza un site
	private boolean synchronizeSite(Site s, AuthzGroup baseAuthzGroup,
			AuthzGroup sectionAuthzGroup, List<String> ignoreFunctions,
			List<IListString> addTool, List<IListString> delTool,
			List<IPage> addPage, List<IListString> delPage, boolean syncInto,
			String syncHome) {

		boolean save = false;

		// sincronizamos permisos
		save |= synchronizeRealms(s, baseAuthzGroup, sectionAuthzGroup,
				ignoreFunctions);

		// sincronizamos paginas
		save |= synchronizeSitePages(s, getList(addTool), getList(delTool),
				addPage, getList(delPage), syncHome, syncInto);

		// Guardamos los cambios
		try {
			if (save) {
				siteService.save(s);
				log.info("Cambios guardados en el site " + s.getId());
			} else {
				log.info("NO HAY CAMBIOS EN: " + s.getId());
			}
		} catch (IdUnusedException e) {
			log.warn("Sincronizando el no-sitio " + s.getId() + " E:" + e);
			return false;
		} catch (PermissionException e) {
			log.warn("Sincronizando en el sitio " + s.getId() + " E:" + e);
			return false;
		}
		return save;
	}

	private List<String> getList(List<IListString> param) {
		ArrayList<String> rtn = new ArrayList<String>();
		if (param != null) {
			for (IListString a : param) {
				rtn.add(a.getString());
			}
		}
		return rtn;
	}

	private boolean synchronizeSitePages(Site s, List<String> addTool,
			List<String> delTool, List<IPage> addPage, List<String> delPage,
			String syncHome, boolean syncInto) {

		if (addTool.isEmpty() && delTool.isEmpty() 
				&& addPage.isEmpty() && delPage.isEmpty()
				&& ITask.NOT_CHANGE_HOME_PAGE.equals(syncHome)) {
			if (log.isDebugEnabled()) log.debug("No hay tools que añadir/eliminar");
			return false;
		}

		boolean changedHomePage = false;

		// Lista de paginas que estan en el site y que debemos eliminar
		List<SitePage> pagesInSiteToRemove = new ArrayList<SitePage>();

		// Lista de paginas en las que
		Map<SitePage, List<ToolConfiguration>> toolsInPagesToRemove = new HashMap<SitePage, List<ToolConfiguration>>();

		// Recorremos todas las paginas del site
		SitePage home = null;
		for (SitePage p : s.getPages()) {
			
			try {
				if (p.getProperties().getBooleanProperty(SitePage.IS_HOME_PAGE)) {
					home = p;
					if (log.isDebugEnabled()) log.debug(p.getTitle() + "  IS HOME");
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) log.debug(p.getTitle() + " IS NOT HOME");
			}

			List<ToolConfiguration> tools = p.getTools();

			// Tool que hay en la pagina, o nombre de la pagina
			String idTool = (tools.size() == 1) ? tools.get(0).getToolId() : null; 
			String pageName = p.getTitle();

			// Esta en el sitio -> no hay que a"adirla de nuevo
			addTool.remove(idTool);
			IPage presentPage = null;
			for (IPage page : addPage) {
				if (page.getName()!=null && page.getName().equals(pageName)) {
					presentPage = page;
					break;
				}
			}
			addPage.remove(presentPage);

			// Si se tiene que eliminar, se guarda la pagina del sitio
			if (delTool.contains(idTool) || delPage.contains(pageName)) {
				pagesInSiteToRemove.add(p);
			}

			// Activado el flag de incluir contenido de las paginas
			if (syncInto && tools.size() > 1) {
				List<ToolConfiguration> ltc = new ArrayList<ToolConfiguration>();
				for (ToolConfiguration tc : tools) {
					if (log.isDebugEnabled()) log.debug(" -- tengo: " + tc.getToolId());
					// No hay que a"adir esa pagina
					addTool.remove(tc.getToolId());
					// Si esta se elimina del interior de la pagina
					if (delTool.contains(tc.getToolId())) {
						ltc.add(tc);
						// p.removeTool(tc);
						// map para fuera
						// si vacio -> borrar pagina
						if (log.isDebugEnabled()) log.debug("ELIMINARIA: " + tc.getToolId());
					}
				}
				if (!ltc.isEmpty())
					toolsInPagesToRemove.put(p, ltc);
			}
		}
		
		if (ITask.REMOVE_THE_HOME_PAGE.equals(syncHome) && (home!=null)) {
			s.removePage(home);
			log.info("HomePage was deleted");
			changedHomePage = true;
		} else if (ITask.PROPERTIES_HOME_PAGE.equals(syncHome)) {

			String propertyBase = "wsetup.home.toolids." + s.getType() + ".";
			Integer count = this.getServerConfigurationService().getInt(propertyBase + "count", 0);
			if (count == 0) {
				propertyBase = "wsetup.home.toolids.";
				count = this.getServerConfigurationService().getInt(propertyBase + "count", 0);
			}

			List<String> toolsInProperties = new ArrayList<String>();
			for (int i = 1; i <= count; i++) {
				String tool = this.getServerConfigurationService().getString(propertyBase + i, null);
				toolsInProperties.add(tool);
			}

			// Are home pages out of sync ?
			if (home==null || (home.getTools().size() != count)
					|| !areTheSameTools(home.getTools(), toolsInProperties)) {
				if (home!=null) s.removePage(home);
				SitePage newHome = s.addPage();
				newHome.setTitle("HOME");
				newHome.getProperties().addProperty(SitePage.IS_HOME_PAGE, "true");
				newHome.setLayout(SitePage.LAYOUT_DOUBLE_COL);

				int pos = 0;
				for (String tool : toolsInProperties) {
					ToolConfiguration tc = newHome.addTool(tool);
					tc.setLayoutHints(pos / 2 + "," + pos++ % 2);
				}
				log.info("HomePage was initialized");
				changedHomePage = true;
			}
		} else if (!ITask.NOT_CHANGE_HOME_PAGE.equals(syncHome)){
			try {
				IPage newHomePage = this.getPage(syncHome);
				ArrayList<String> toolsInPage = new ArrayList<String>();
				for (IListString t : newHomePage.getLeftColumn())
					toolsInPage.add(t.getString());
				for (IListString t : newHomePage.getRightColumn())
					toolsInPage.add(t.getString());
				if (home==null || (toolsInPage.size() > 0 && !areTheSameTools(home.getTools(), toolsInPage))) {
					addNewPage(newHomePage, s, true);
					if (home!=null) s.removePage(home);
					log.info("HomePage was set");
					changedHomePage = true;
				}
			} catch (Exception e) {
				log.error("Error loading page "+syncHome+": "+e);
			}
		}

		// Eliminamos las paginas marcadas
		for (SitePage p : pagesInSiteToRemove) {
			s.removePage(p);
		}

		// Eliminamos las tools dentro de paginas
		for (Entry<SitePage, List<ToolConfiguration>> entry : toolsInPagesToRemove.entrySet()) {
			SitePage p = entry.getKey();
			for (ToolConfiguration tc: entry.getValue()) {
				if (log.isDebugEnabled()) log.debug("eliminando " + tc.getToolId() + " de la pagina: "+ p.getId());
				p.removeTool(tc);
			}
			// Si se queda vac"a la eliminamos
			if (p.getTools().isEmpty()) {
				if (log.isDebugEnabled()) log.debug("vacia");
				s.removePage(p);
			}
		}

		// A"adimos las paginas que queden
		int notAddTools = 0;
		for (String p : addTool) {
			SitePage nueva = s.addPage();
			ToolConfiguration tc = nueva.addTool(p);
			tc.setLayoutHints("0,0");
			// es null si no existe, o no esta disponible para este tipo de site
			if (tc.getTool() == null) {
				log.error("No se puede añadir a la pagina la herramienta: " + p + " dentro siteId: " + s.getId() + " [" + s.getDescription() + "] de tipo " + s.getType());
				s.removePage(nueva);
				notAddTools++;
			}
		}

		// A"adimos las paginas que tienen varias tools
		for (IPage page : addPage) {
			addNewPage(page, s, false);
		}

		log.info(s.getId() + "}}- Se eliminan " + pagesInSiteToRemove.size()
				+ " tools/paginas, y se añaden "
				+ (addTool.size() - notAddTools) + " tools y " + addPage.size()
				+ " paginas.");

		return changedHomePage
				|| !(pagesInSiteToRemove.isEmpty()
						&& toolsInPagesToRemove.isEmpty()
						&& addTool.size() == notAddTools && addPage.isEmpty());
	}

	private void addNewPage(IPage page, Site s, boolean isHome) {
		SitePage newPage = s.addPage();
		if (isHome) {
			newPage.setTitle("HOME");
			newPage.getProperties().addProperty(SitePage.IS_HOME_PAGE, "true");
		} else {
			newPage.setTitle(page.getName());
		}

		newPage.setLayout(page.getColumns().equals("2") ? SitePage.LAYOUT_DOUBLE_COL : SitePage.LAYOUT_SINGLE_COL);

		int pos = 0;
		for (IListString tool : page.getLeftColumn()) {
			ToolConfiguration tc = newPage.addTool(tool.getString());
			tc.setLayoutHints((pos++) + "," + 0);
		}

		pos = 0;
		for (IListString tool : page.getRightColumn()) {
			ToolConfiguration tc = newPage.addTool(tool.getString());
			tc.setLayoutHints((pos++) + "," + 1);
		}

		if (log.isDebugEnabled()) log.debug("Creada pagina: " + page);
	}

	private boolean areTheSameTools(List<ToolConfiguration> toolsInHomePage,
			List<String> toolsInProperties) {
		for (ToolConfiguration tool : toolsInHomePage) {
			if (!toolsInProperties.contains(tool.getTool().getId()))
				return false;
		}
		return true;
	}

	private boolean synchronizeRealms(Site s, AuthzGroup baseAuthzGroup,
			AuthzGroup sectionAuthzGroup, List<String> ignoreFunctions) {

		boolean rtn;
		// Permisos del site
		if (baseAuthzGroup == null) {
			log.info("No tengo baseAuthzGroup para sincronizar permisos para el sitio: "+ s.getId());
			rtn = false;
		} else {
			rtn = synchronizeAuthzGroup(baseAuthzGroup, s, ignoreFunctions);
		}

		// Permisos de las sections
		Collection<Group> grupos = s.getGroups();
		if (grupos.size() > 0 && sectionAuthzGroup == null) {
			log.info("No tengo sectionAuthzGroup para sincronizar permisos para el sitio: "+ s.getId());
			return rtn;
		}
		for (Group g : grupos) {
			if (log.isDebugEnabled()) log.debug("Sincronizando grupo: " + g.getTitle()
					+ " con plantilla: " + sectionAuthzGroup.getId());
			rtn |= synchronizeAuthzGroup(sectionAuthzGroup, g, ignoreFunctions);
		}

		return rtn;
	}

	private boolean synchronizeAuthzGroup(AuthzGroup origen,
			AuthzGroup destino, List<String> ignoreFunctions) {
		boolean rtn = false;
		Set<Role> baseroles = origen.getRoles();
		for (Role r : baseroles) {
			// A"adir roles que no estan presentes, con todos sus permisos.
			try {
				destino.addRole(r.getId(), r);
				rtn = true;
				if (log.isDebugEnabled()) log.debug("Rol " + r.getId() + " añadido al sitio "
						+ destino.getId());
			} catch (RoleAlreadyDefinedException e) {
				// Si ya existe el rol, se sincronizan sus permisos
				Role rol = destino.getRole(r.getId());

				// if (!isTheSameCollection(r.getAllowedFunctions(),
				// rol.getAllowedFunctions(), ignoreFunctions)) {
				// if (log.isDebugEnabled()) log.debug("El rol " + r.getId() + " existe en el sitio "+
				// destino.getId()+
				// " y tiene permisos distintos, los actualizo...");

				List<String> deleteFunction = new ArrayList<String>();
				for (String function : rol.getAllowedFunctions()) {
					if (!r.getAllowedFunctions().contains(function)
							&& !ignoreFunctions.contains(function)) {
						deleteFunction.add(function);
					}
				}
				for (String function : deleteFunction) {
					rol.disallowFunction(function);
					if (log.isDebugEnabled()) log.debug("- deleting function " + function);
					rtn = true;
				}
				for (String function : r.getAllowedFunctions()) {
					if (!rol.getAllowedFunctions().contains(function)
							&& !ignoreFunctions.contains(function)) {
						rol.allowFunction(function);
						if (log.isDebugEnabled()) log.debug("+ adding function " + function);
						rtn = true;
					}
				}
			}
		}

		// Lista de roles que hay que eliminar en destino
		Collection<Role> toRemove = CollectionUtils.subtract(destino.getRoles(), baseroles);

		for (Role r : toRemove) {
			destino.removeRole(r.getId());
			if (log.isDebugEnabled()) log.debug("- removing role "+r.getId());
			rtn = true;
		}

		return rtn;
	}

}
