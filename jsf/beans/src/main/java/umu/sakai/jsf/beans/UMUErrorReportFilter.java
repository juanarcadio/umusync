package umu.sakai.jsf.beans;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UMUErrorReportFilter implements Filter {

	public void destroy() {	}

	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (Throwable t) {
			org.sakaiproject.portal.util.ErrorReporter err = new org.sakaiproject.portal.util.ErrorReporter();
			err.report((HttpServletRequest)request, (HttpServletResponse)response, t);
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {}
}
