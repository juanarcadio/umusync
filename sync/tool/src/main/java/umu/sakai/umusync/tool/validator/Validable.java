package umu.sakai.umusync.tool.validator;

public interface Validable {
	public Class getDaoClass(String beanProperty);
	public String getDaoId(String beanProperty);
}
