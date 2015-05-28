package umu.sakai.umusync.dao.page;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import umu.sakai.umusync.api.dao.ISortedListString;
import umu.sakai.umusync.dao.Page;


@Entity
@Table(name="SAKAI_SYNCSITES_LEFTCOLUMN")
public class LeftColumn implements ISortedListString, Comparable<LeftColumn>{
	@SequenceGenerator(name="SyncIdGen", sequenceName="SEQ_SYNCSITES_LEFTCOLUMN")
	@Id @GeneratedValue(generator="SyncIdGen")
	@Column(name="NUMBER_ID")
    protected long id;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PAGE_NAME")
	protected Page page;
	
	@Column(name = "POSITION_ID")
	protected int pos;
	
	@Column(name = "TOOL_ID", nullable = false, length = 100)
	protected String tool;
	
	

	public long getId() {
		return id;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getString() {
		return tool;
	}

	public void setString(String tool) {
		this.tool = tool;
	}
	
	public void setMaster(Object page) {		
		this.page = (Page)page;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((page == null || page.getName() == null) ? 0 : page.getName().hashCode());
		result = prime * result + pos;
		result = prime * result + ((tool == null) ? 0 : tool.hashCode());
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
		LeftColumn other = (LeftColumn) obj;
		if (id != other.id)
			return false;
		if (page == null) {
			if (other.page != null)
				return false;
		} else if (page.getName() == null) {
			if (other.page.getName() != null)
				return false;
		} else if (!page.getName().equals(other.page.getName()))
			return false;
		if (pos != other.pos)
			return false;
		if (tool == null) {
			if (other.tool != null)
				return false;
		} else if (!tool.equals(other.tool))
			return false;
		return true;
	}

	public int compareTo(LeftColumn o) {
		return this.getPos()-o.getPos();
	}

	
}
