package umu.sakai.umusync.api.dao;

public interface IListString {
	
	long getId();
	
	String getString();
	void setString(String str);
	
	void setMaster(Object obj);
}