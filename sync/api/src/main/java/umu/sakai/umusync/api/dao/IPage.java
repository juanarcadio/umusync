package umu.sakai.umusync.api.dao;

import java.util.Collection;
import java.util.List;

public interface IPage {
	
	String getName();
	void setName(String name);
	
	String getColumns();
	void setColumns(String columns);
	
	List<IListString> getLeftColumn();
	void addToolInLeftColumn(Integer pos, String toolElegida);
	boolean delToolFromLeftColumn(IListString tool);
	
	List<IListString> getRightColumn();
	void addToolInRightColumn(Integer pos, String toolId);
	boolean delToolFromRightColumn(IListString tool);
	
	int getPos(IListString item);
	
	void numberingBeforeSave();
	void numberingAfterLoad();
	
	Collection<IListString> flushOrphanRemovalEntities();
}