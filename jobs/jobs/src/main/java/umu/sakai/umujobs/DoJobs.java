package umu.sakai.umujobs;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import umu.sakai.umujobs.api.IJob;


public class DoJobs implements Job {

	private static final Log log = LogFactory.getLog(DoJobs.class);
	protected List<IJob> jobs;
	protected SessionManager sessionManager;
	protected Session sakaiSession;
	protected AuthzGroupService authzGroupService;
	protected String user = "admin";

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			loginToSakai();
			for (IJob job:jobs) {
				doJob(job);
			}
		} catch (Throwable t) {
			log.error("Doing jobs error: "+t);
			throw new JobExecutionException(t.toString());
		} finally {
			logoutFromSakai();
		}
	}
	
	public List<IJob> getJobs() { return jobs; }
	public void setJobs(List jobs) { this.jobs = jobs; }
	
	protected void loginToSakai() {
		sakaiSession = getSessionManager().getCurrentSession();
		sakaiSession.setUserId(user);
		sakaiSession.setUserEid(user);
		getAuthzGroupService().refreshUser(user);

	}

	protected void logoutFromSakai() {
		sakaiSession.invalidate();
	}
	
	protected void doJob(IJob job) throws Throwable {
		log.debug("Doing: "+job.jobName());
		job.doit();
	}
	
	public void setSessionManager(SessionManager sessionManager) { this.sessionManager = sessionManager; }
	public SessionManager getSessionManager() {	return sessionManager; }
	
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {	this.authzGroupService = authzGroupService; }
	public AuthzGroupService getAuthzGroupService() { return authzGroupService;	}
	
	public void setUser(String user) { this.user = user; }
	public String getUser() { return this.user; }
	
}
