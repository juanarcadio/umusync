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
@Table(name="SAKAI_SYNCSITES_DELTOOL")
public class DelTool implements IListString {

	@SequenceGenerator(name="SyncIdGen", sequenceName="SEQ_SYNCSITES_DELTOOL")
	@Id @GeneratedValue(generator="SyncIdGen")
	@Column(name="NUMBER_ID")
    protected long id;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TASK_ID", nullable=false)
	protected Task task;
	
	@Column(name = "TOOL_ID", nullable = false, length = 100)
	protected String tool;
	
	
	
	public long getId() {
		return id;
	}

	public String getString() {
		return tool;
	}

	public void setString(String tool) {
		this.tool = tool;
	}
	
	public void setMaster(Object task) {		
		this.task = (Task)task;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((task == null) ? 0 : (int) (task.getId() ^ (task.getId() >>> 32)));
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
		DelTool other = (DelTool) obj;
		if (id != other.id)
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (task.getId() != other.task.getId())
			return false;
		if (tool == null) {
			if (other.tool != null)
				return false;
		} else if (!tool.equals(other.tool))
			return false;
		return true;
	}	
	
	
}
