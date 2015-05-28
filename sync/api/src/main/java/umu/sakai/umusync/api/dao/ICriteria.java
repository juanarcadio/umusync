package umu.sakai.umusync.api.dao;



public interface ICriteria {
	
	long getId();
	void setId(long id);
	
	String getName();
	void setName(String name);

	String getProperty();
	void setProperty(String property);

	String getComparador();
	void setComparador(String comparador);

	String getValor();
	void setValor(String valor);
	
}