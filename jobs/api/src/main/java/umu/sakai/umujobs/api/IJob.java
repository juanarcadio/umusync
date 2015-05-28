package umu.sakai.umujobs.api;

public interface IJob {
	public void doit() throws Throwable;
	public String jobName();
}