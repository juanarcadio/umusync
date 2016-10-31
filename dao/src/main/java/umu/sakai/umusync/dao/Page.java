package umu.sakai.umusync.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import umu.sakai.umusync.api.dao.IListString;
import umu.sakai.umusync.dao.page.LeftColumn;
import umu.sakai.umusync.dao.page.RightColumn;
import umu.sakai.umusync.dao.task.DelPage;


@Entity
@Table(name="SAKAI_SYNCSITES_PAGE")
public class Page implements umu.sakai.umusync.api.dao.IPage {

	@Column(name="PAGE_NAME")
	@Length(max=50)
	@NotNull
	@Id
	protected String name;
	
	@Column(name="COLUMNS")
	@Length(max=1)
	protected String columns = "1";
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "page")
	protected List<LeftColumn> leftColumn = new ArrayList<LeftColumn>(0);	

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "page")
	protected List<RightColumn> rightColumn = new ArrayList<RightColumn>(0);	

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "page")
	protected List<DelPage> delPage = new ArrayList<DelPage>(0);	
	
	
	
	@Transient
	private Collection<IListString> orphanRemoval = new ArrayList<IListString>();
	
	public Collection<IListString> flushOrphanRemovalEntities() {
		Collection<IListString> rtn = orphanRemoval;
		orphanRemoval = new ArrayList<IListString>();
		return rtn;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		if (this.columns.equals("2") && columns.equals("1")) {
			for (RightColumn r : rightColumn) {
				if (r.getId()!=0) orphanRemoval.add(r);
				this.addToolInLeftColumn(null, r.getString());
			}
			rightColumn.clear();
		}
		this.columns = columns;
	}
	
	
	public List getRightColumn() {
		return this.rightColumn;
	}
	
	public void addToolInRightColumn(Integer pos, String toolId) {
		if (toolId==null) return;		
		RightColumn tool = new RightColumn();
		tool.setString(toolId);
		tool.setMaster(this);
		if (pos==null) rightColumn.add(tool);
		else rightColumn.add(pos, tool);
	}
	
	public boolean delToolFromRightColumn(IListString tool) {
		if (tool==null) return false;		
		boolean rtn = rightColumn.remove(tool);
		if (tool.getId()!=0) orphanRemoval.add(tool);
		return rtn;
	}
	
	public List getLeftColumn() {
		return this.leftColumn;
	}
	
	public void addToolInLeftColumn(Integer pos, String toolId) {
		if (toolId==null) return;		
		LeftColumn tool = new LeftColumn();
		tool.setString(toolId);
		tool.setMaster(this);
		if (pos==null) leftColumn.add(tool);
		else leftColumn.add(pos, tool);
	}
	
	public boolean delToolFromLeftColumn(IListString tool) {
		if (tool==null) return false;		
		boolean rtn = leftColumn.remove(tool);
		if (tool.getId()!=0) orphanRemoval.add(tool);
		return rtn;
	}
	
	public int getPos(IListString item) {
		if (item instanceof LeftColumn) {
			return leftColumn.indexOf(item);
		}
		else if (item instanceof RightColumn) {
			return rightColumn.indexOf(item);
		}
		return -1;
	}
	
	public void numberingBeforeSave() {
		int pos = 0;
		for (LeftColumn lc : leftColumn) {
			lc.setPos(pos++);
		}
		pos = 0;
		for (RightColumn rc : rightColumn) {
			rc.setPos(pos++);
		}
	}
	
	public void numberingAfterLoad() {
		Collections.sort(leftColumn);
		Collections.sort(rightColumn);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columns == null) ? 0 : columns.hashCode());
		result = prime * result + ((delPage == null) ? 0 : delPage.hashCode());
		result = prime * result + ((leftColumn == null) ? 0 : leftColumn.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((rightColumn == null) ? 0 : rightColumn.hashCode());
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
		Page other = (Page) obj;
		if (columns == null) {
			if (other.columns != null)
				return false;
		} else if (!columns.equals(other.columns))
			return false;
		if (delPage == null) {
			if (other.delPage != null)
				return false;
		} else if (!delPage.equals(other.delPage))
			return false;
		if (leftColumn == null) {
			if (other.leftColumn != null)
				return false;
		} else if (!leftColumn.equals(other.leftColumn))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (rightColumn == null) {
			if (other.rightColumn != null)
				return false;
		} else if (!rightColumn.equals(other.rightColumn))
			return false;
		return true;
	}
	
	
}
