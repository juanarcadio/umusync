package umu.sakai.umusync.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import umu.sakai.umusync.api.dao.ICriteria;
import umu.sakai.umusync.api.dao.IListString;
import umu.sakai.umusync.api.dao.IPage;
import umu.sakai.umusync.api.dao.ITask;
import umu.sakai.umusync.dao.task.AddTool;
import umu.sakai.umusync.dao.task.DelPage;
import umu.sakai.umusync.dao.task.DelTool;
import umu.sakai.umusync.dao.task.IgnoreById;
import umu.sakai.umusync.dao.task.IgnoreFunction;


@Entity
@Table(name="SAKAI_SYNCSITES_TASK")
public class Task implements umu.sakai.umusync.api.dao.ITask {
	
	//private static Log log = LogFactory.getLog(Task.class);
	
	@SequenceGenerator(name="SyncIdGen", sequenceName="SEQ_SYNCSITES_TASK")
	@Id @GeneratedValue(generator="SyncIdGen")
	@Column(name="TASK_ID")
    protected long id;
	
	@Length(min = 1, max = 1)
	@Column(name="AVAILABLE")
	@NotNull
	protected String available = "N";
	
	@Column(name="SITE_TYPE")
	@Length(max=30)
	protected String tipo;
	
	@Column(name="REALM_SITE")
	@Length(max=50)
	protected String realmSite;
	
	@Column(name="REALM_SECTION")
	@Length(max=50)
	protected String realmSection;
	
	@Column(name="COMMENTS")
	@Length(max = 276)
	protected String comments;
	
	@Column(name="SYNC_INTO")
	@Length(min = 1, max = 1)
	@NotNull
	protected String syncInto = "N";
	
	@Column(name="SYNC_HOME")
	@Length(max = 100)
	protected String syncHome;
	
	@Column(name="IGNORE_FUNCTIONS")
	@Length(min = 1, max = 1)
	@NotNull
	protected String ignoreFunctionsMode = ITask.NOT_CHANGE_FUNCTION_MODE;
	
	
	@ManyToMany (fetch=FetchType.LAZY)
	@JoinTable (name="SAKAI_SYNCSITES_TASKCRITERIA",    
	          			joinColumns=@JoinColumn(name="TASK_ID", referencedColumnName="TASK_ID"),
	          			inverseJoinColumns=@JoinColumn(name="CRITERIA_ID", referencedColumnName="CRITERIA_ID"))
	protected List<Criteria> criteria = new ArrayList<Criteria>();
	
	

	@ManyToMany (fetch=FetchType.LAZY)
	@JoinTable (name="SAKAI_SYNCSITES_TASKPAGE" , 
	          			joinColumns=@JoinColumn(name="TASK_ID"),
	          			inverseJoinColumns=@JoinColumn(name="PAGE_NAME"))
	protected List<Page> pagesToAdd = new ArrayList<Page>();
	

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
	protected List<AddTool> toolsToAdd = new ArrayList<AddTool>(0);
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
	protected List<DelTool> toolsToDel = new ArrayList<DelTool>(0);
	
	/*@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
	protected List<AddPage> pagesToAdd = new ArrayList<AddPage>(0);*/
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
	protected List<DelPage> pagesToDel = new ArrayList<DelPage>(0);
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
	protected List<IgnoreById> ignoreById = new ArrayList<IgnoreById>(0);	
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
	protected List<IgnoreFunction> ignoreFunction = new ArrayList<IgnoreFunction>(0);	
	
	
	@Transient
	private List<IListString> orphanRemoval = new ArrayList<IListString>();
	
	public List<IListString> flushOrphanRemovalEntities() {
		List<IListString> rtn = orphanRemoval;
		orphanRemoval = new ArrayList<IListString>();
		return rtn;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean getAvailable() {
		return available!=null && available.equals("S");
	}

	public void changeAvailable() {
		this.available = getAvailable()?"N":"S";

	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	// tools to add
	public List getToolsToAdd() {
		return this.toolsToAdd;
	}
	
	public void addToolToAdd(String toolId) {
		if (toolId==null) return;		
		AddTool tool = new AddTool();
		tool.setString(toolId);
		tool.setMaster(this);
		toolsToAdd.add(tool);
	}
	
	public boolean delToolToAdd(IListString tool) {
		if (tool==null) return false;		
		boolean rtn = toolsToAdd.remove(tool);
		if (rtn) orphanRemoval.add(tool);
		return rtn;
	}
	
	// tools to del
	public List getToolsToDel() {
		return this.toolsToDel;
	}
	
	public void addToolToDel(String toolId) {
		if (toolId==null) return;		
		DelTool tool = new DelTool();
		tool.setString(toolId);
		tool.setMaster(this);
		toolsToDel.add(tool);
	}
	
	public boolean delToolToDel(IListString tool) {
		if (tool==null) return false;		
		boolean rtn = toolsToDel.remove(tool);
		if (rtn) orphanRemoval.add(tool);
		return rtn;
	}

	public List getPagesToAdd() {
		return this.pagesToAdd;
	}
	
	public void addPageToAdd(IPage page) {
		pagesToAdd.add((Page)page);
	}
	
	public boolean delPageToAdd(IPage page) {			
		return pagesToAdd.remove(page);
	}
	
	// pages to del
	public List getPagesToDel() {
		return this.pagesToDel;
	}
	
	public void addPageToDel(String pageName) {
		if (pageName==null) return;		
		DelPage page = new DelPage();
		page.setString(pageName);
		page.setMaster(this);
		pagesToDel.add(page);
	}
	
	public boolean delPageToDel(IListString page) {
		if (page==null) return false;		
		boolean rtn = pagesToDel.remove(page);
		if (rtn) orphanRemoval.add(page);
		return rtn;
	}
	
	public List getCriteria() {
		return this.criteria;
	}
	
	public void setCriteria(List<ICriteria> criteria) {
		this.criteria = (List)criteria;		
	}
	
	public String getRealmSite() {
		return realmSite;
	}

	public void setRealmSite(String realmSite) {
		this.realmSite = realmSite;
	}

	public String getRealmSection() {
		return realmSection;
	}

	public void setRealmSection(String realmSection) {
		this.realmSection = realmSection;
	}
	
	public boolean getSyncInto() {
		return "S".equals(syncInto);
	}
	
	public void setSyncInto(boolean syncInto) {
		this.syncInto = (syncInto?"S":"N");
	}
	
	public String getSyncHome() {		
		return (syncHome == null ? ITask.NOT_CHANGE_HOME_PAGE : syncHome);
	}
	
	public void setSyncHome(String syncHome) {
		this.syncHome = syncHome;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public List getIgnoreById() {
		return ignoreById;
	}
	
	public String getIgnoreSitesById() {
		if (ignoreById.isEmpty()) return "";
		StringBuffer rtn = new StringBuffer();
		for (IgnoreById ignore : ignoreById) {
			rtn.append(", "+ignore.getString());
		}
		return rtn.substring(2);
	}

	public void setIgnoreSitesById(String ignore) {
		// Convert string in a ArrayList
		ArrayList<String> settedIgnoreList = new ArrayList<String>();			
		for (String id : ignore.toString().split(",")) {
			id = id.trim();
			settedIgnoreList.add(id);
		}
		// Compare collections
		ArrayList<IgnoreById> deferredRemove = new ArrayList<IgnoreById>();
		for (IgnoreById ibi : ignoreById) {
			if (!settedIgnoreList.remove(ibi.getString())) {
				deferredRemove.add(ibi);
			}
		}
		// Remove in database
		for (IgnoreById delI : deferredRemove) {
			ignoreById.remove(delI);
			orphanRemoval.add(delI);
		}
		// Add to database
		for (String newSiteId : settedIgnoreList) {
			if (newSiteId.equals("")) continue;
			IgnoreById newI = new IgnoreById();
			newI.setString(newSiteId);
			newI.setMaster(this);
			ignoreById.add(newI);
		}
	}

	public String getIgnoreFunctionsMode() {
		return ignoreFunctionsMode;
	}

	public void setIgnoreFunctionsMode(String ignoreFunctionsMode) {
		this.ignoreFunctionsMode = ignoreFunctionsMode;
	}

	// functions to ignore (if checked)
	public List getIgnoredFunctions() {
		return this.ignoreFunction;
	}

	public void addFunctionToIgnore(String functionName) {
		if (functionName==null) return;		
		IgnoreFunction function = new IgnoreFunction();
		function.setString(functionName);
		function.setMaster(this);
		ignoreFunction.add(function);
	}

	public boolean delFunctionToIgnore(IListString function) {
		if (function==null) return false;		
		boolean rtn = ignoreFunction.remove(function);
		if (rtn) orphanRemoval.add(function);
		return rtn;
	}

	public int compareTo(ITask o) {
		return (int) (this.getId()-o.getId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((available == null) ? 0 : available.hashCode());
		result = prime * result
				+ ((comments == null) ? 0 : comments.hashCode());
		result = prime * result
				+ ((criteria == null) ? 0 : criteria.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((ignoreById == null) ? 0 : ignoreById.hashCode());
		result = prime * result
				+ ((ignoreFunction == null) ? 0 : ignoreFunction.hashCode());
		result = prime
				* result
				+ ((ignoreFunctionsMode == null) ? 0 : ignoreFunctionsMode
						.hashCode());
		result = prime * result
				+ ((pagesToAdd == null) ? 0 : pagesToAdd.hashCode());
		result = prime * result
				+ ((pagesToDel == null) ? 0 : pagesToDel.hashCode());
		result = prime * result
				+ ((realmSection == null) ? 0 : realmSection.hashCode());
		result = prime * result
				+ ((realmSite == null) ? 0 : realmSite.hashCode());
		result = prime * result
				+ ((syncHome == null) ? 0 : syncHome.hashCode());
		result = prime * result
				+ ((syncInto == null) ? 0 : syncInto.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		result = prime * result
				+ ((toolsToAdd == null) ? 0 : toolsToAdd.hashCode());
		result = prime * result
				+ ((toolsToDel == null) ? 0 : toolsToDel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (available == null) {
			if (other.available != null)
				return false;
		} else if (!available.equals(other.available))
			return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria))
			return false;
		if (id != other.id)
			return false;
		if (ignoreById == null) {
			if (other.ignoreById != null)
				return false;
		} else if (!ignoreById.equals(other.ignoreById))
			return false;
		if (ignoreFunction == null) {
			if (other.ignoreFunction != null)
				return false;
		} else if (!ignoreFunction.equals(other.ignoreFunction))
			return false;
		if (ignoreFunctionsMode == null) {
			if (other.ignoreFunctionsMode != null)
				return false;
		} else if (!ignoreFunctionsMode.equals(other.ignoreFunctionsMode))
			return false;
		if (pagesToAdd == null) {
			if (other.pagesToAdd != null)
				return false;
		} else if (!pagesToAdd.equals(other.pagesToAdd))
			return false;
		if (pagesToDel == null) {
			if (other.pagesToDel != null)
				return false;
		} else if (!pagesToDel.equals(other.pagesToDel))
			return false;
		if (realmSection == null) {
			if (other.realmSection != null)
				return false;
		} else if (!realmSection.equals(other.realmSection))
			return false;
		if (realmSite == null) {
			if (other.realmSite != null)
				return false;
		} else if (!realmSite.equals(other.realmSite))
			return false;
		if (syncHome == null) {
			if (other.syncHome != null)
				return false;
		} else if (!syncHome.equals(other.syncHome))
			return false;
		if (syncInto == null) {
			if (other.syncInto != null)
				return false;
		} else if (!syncInto.equals(other.syncInto))
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		if (toolsToAdd == null) {
			if (other.toolsToAdd != null)
				return false;
		} else if (!toolsToAdd.equals(other.toolsToAdd))
			return false;
		if (toolsToDel == null) {
			if (other.toolsToDel != null)
				return false;
		} else if (!toolsToDel.equals(other.toolsToDel))
			return false;
		return true;
	}

	
}
