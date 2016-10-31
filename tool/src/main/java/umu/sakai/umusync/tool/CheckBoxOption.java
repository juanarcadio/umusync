package umu.sakai.umusync.tool;

import umu.sakai.umusync.api.dao.IListString;
import umu.sakai.umusync.api.dao.IPage;


public class CheckBoxOption	implements Comparable<CheckBoxOption> {

	private String label;
	
	/* persistent objects linked */
	private Object addItem;
	private Object delItem;
		
	/* current values of checkBox */
	private boolean add;
	private boolean del;
	
	/* initial values of checkBox */ 
	private boolean initAdd;
	private boolean initDel;
	
	/* true = option is over a tool 
	 * false = option is over a page */
	private boolean isTool;
	
	
	/* Options for a tool */
	public CheckBoxOption(String label) {
		this.label = label;
		this.isTool = true;
		initAdd = false;
		initDel = false;
		add = false;
		del = false;		
	}
	
	/* Options for a page */
	public CheckBoxOption(IPage pageDefined) {
		this.label = pageDefined.getName();
		this.addItem = pageDefined;
		this.isTool = false;
		initAdd = false;
		initDel = false;
		add = false;
		del = false;
	}

	/* link with a persistent object for tools */
	protected void setAddObject(IListString item) {
		this.addItem = item;
		initAdd = true;
		add = true;
	}
	
	/* link with a persistent object */
	protected void setDelObject(IListString item) {
		this.delItem = item;
		initDel = true;
		del = true;
	}
	
	/* initalize for pages to add */
	protected void initAdd() {
		initAdd = true;
		add = true;
	}
	
	
	/* calls from webpage */
	public String getLabel() {
		return label;
	}
	
	public boolean getAdd() {
		return add;
	}

	public void setAdd(boolean add) {
		this.add = add;
	}

	public boolean getDel() {
		return del;
	}

	public void setDel(boolean del) {
		this.del = del;
	}
	
	/* is a tool to add and it was unchecked by user */
	protected IListString removedAddTool() {
		return (IListString)(isTool && !add && initAdd ? addItem : null);
	}

	/* is a tool  and it was check by user */
	protected String addedAddTool() {
		return isTool && add && !initAdd ? label : null;
	}

	/* is a page to add and it was unchecked by user */
	protected IPage removedAddPage() {
		return (IPage)(!isTool && !add && initAdd ? addItem : null);
	}

	/* is a page to add and it was check by user */
	protected IPage addedAddPage() {
		return !isTool && add && !initAdd ? (IPage)addItem : null;
	}
	
	/* is a tool to remove and it was unchecked by user */
	protected IListString removedDelTool() {
		return (IListString)(isTool && !del && initDel ? delItem : null);
	}

	/* is a tool to remove and it was check by user */
	protected String addedDelTool() {
		return isTool && del && !initDel ? label : null;
	}

	/* is a page to remove and it was unchecked by user */
	protected IListString removedDelPage() {
		return (IListString)(!isTool && !del && initDel ? delItem : null);
	}

	/* is a page to remove and it was check by user */
	protected String addedDelPage() {
		return !isTool && del && !initDel ? label : null;
	}

	/* tools alphabetic - pages alphabetic */
	public int compareTo(CheckBoxOption o) {
		if (this.equals(o)) return 0;
		CheckBoxOption c = (CheckBoxOption)o;
		if (this.isTool && !c.isTool) return -1;
		else if (!this.isTool && c.isTool) return 1;
		return this.getLabel().compareTo(c.getLabel());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (add ? 1231 : 1237);
		result = prime * result + ((addItem == null) ? 0 : addItem.hashCode());
		result = prime * result + (del ? 1231 : 1237);
		result = prime * result + ((delItem == null) ? 0 : delItem.hashCode());
		result = prime * result + (initAdd ? 1231 : 1237);
		result = prime * result + (initDel ? 1231 : 1237);
		result = prime * result + (isTool ? 1231 : 1237);
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		CheckBoxOption other = (CheckBoxOption) obj;
		if (add != other.add)
			return false;
		if (addItem == null) {
			if (other.addItem != null)
				return false;
		} else if (!addItem.equals(other.addItem))
			return false;
		if (del != other.del)
			return false;
		if (delItem == null) {
			if (other.delItem != null)
				return false;
		} else if (!delItem.equals(other.delItem))
			return false;
		if (initAdd != other.initAdd)
			return false;
		if (initDel != other.initDel)
			return false;
		if (isTool != other.isTool)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}	
}
