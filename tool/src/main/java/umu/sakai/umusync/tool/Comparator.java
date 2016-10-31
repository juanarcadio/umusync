package umu.sakai.umusync.tool;

import org.sakaiproject.util.ResourceLoader;

public class Comparator {
	private ResourceLoader traductor;
	
	private String bundleKey;
	private Integer arity;
	
	public Comparator(String bundleKey, Integer arity, ResourceLoader traductor) {
		this.traductor = traductor;
		this.arity = arity;
		this.bundleKey = bundleKey;
	}

	public String getSignature() {
		return traductor.getString(bundleKey);
	}

	public Integer getArity() {
		return arity;
	}
	
	
}
