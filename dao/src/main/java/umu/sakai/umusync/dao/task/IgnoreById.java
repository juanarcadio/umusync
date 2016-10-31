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
@Table(name="SAKAI_SYNCSITES_IGNOREBYID")
public class IgnoreById implements IListString{

	@SequenceGenerator(name="SyncIdGen", sequenceName="SEQ_SYNCSITES_IGNORE")
	@Id @GeneratedValue(generator="SyncIdGen")
	@Column(name="NUMBER_ID")
    protected long id;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_ID", nullable = false)
	protected Task task;
	
	@Column(name = "SITE_ID", nullable = false, length = 100)
	protected String site;
	
	
	
	public long getId() {
		return id;
	}

	public String getString() {
		return site;
	}

	public void setString(String site) {
		this.site = site;
	}
	
	public void setMaster(Object task) {		
		this.task = (Task)task;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((site == null) ? 0 : site.hashCode());
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
		IgnoreById other = (IgnoreById) obj;
		if (id != other.id)
			return false;
		if (site == null) {
			if (other.site != null)
				return false;
		} else if (!site.equals(other.site))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (task.getId() != other.task.getId())
			return false;
		return true;
	}	
	
	
}
