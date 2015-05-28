package umu.sakai.umusync.dao.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import umu.sakai.umusync.api.dao.IListString;
import umu.sakai.umusync.dao.Task;


@Entity
@Table(name="SAKAI_SYNCSITES_DELPAGE")
public class DelPage implements IListString{

	@SequenceGenerator(name="SyncIdGen", sequenceName="SEQ_SYNCSITES_DELPAGE")
	@Id @GeneratedValue(generator="SyncIdGen")
	@Column(name="NUMBER_ID")
    protected long id;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_ID", nullable=false)
	protected Task task;
	
	@Column(name = "PAGE_NAME", length=50, nullable=false)
	protected String page;

	
	public long getId() {
		return id;
	}

	public String getString() {
		return page;
	}

	public void setString(String page) {
		this.page = page;
	}
	
	public void setMaster(Object task) {		
		this.task = (Task)task;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((page == null) ? 0 : page.hashCode());
		result = prime * result + ((task == null) ? 0 : (int) (task.getId() ^ (task.getId() >>> 32)));
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
		DelPage other = (DelPage) obj;
		if (id != other.id)
			return false;
		if (page == null) {
			if (other.page != null)
				return false;
		} else if (!page.equals(other.page))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (task.getId() != other.task.getId())
			return false;
		return true;
	}	
	
	
}
