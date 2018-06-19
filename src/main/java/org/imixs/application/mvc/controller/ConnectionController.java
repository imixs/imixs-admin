package org.imixs.application.mvc.controller;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

/**
 * The Connect controller is used to establish a connection to Imixs-Worklfow
 * remote interface.
 * 
 * @author rsoika
 *
 */
@Controller
@Named
@SessionScoped
@Path("/connection")
public class ConnectionController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ConnectionController.class.getName());

	String url;
	String userid;
	String password;

	List<String> indexListNoAnalyse;
	List<String> indexListAnalyse;
	ItemCollection configuration;

	WorkflowClient workflowCLient = null;

	public ConnectionController() {
		super();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return the workflow client
	 * 
	 * @return
	 */
	public WorkflowClient getWorkflowCLient() {
		return workflowCLient;
	}

	public ItemCollection getConfiguration() {
		return configuration;
	}

	@GET
	public String actionHome() {
		return "connect.xhtml";
	}

	@GET
	@Path("/configuration")
	public String showConfiguration() {
		return "configuration.xhtml";
	}

	/**
	 * Establishes a new connection to a remote rest service interface and loads the
	 * lucene configuration. If the connection was successful, then the search page
	 * is shown. Otherwise the connect page is shown.
	 * 
	 * The method creaats a new instance of a workfowClient that can be reused be
	 * other beans.
	 * 
	 * @param url
	 * @param userid
	 * @param password
	 * @return
	 */
	@POST
	public String actionConnect(@FormParam("url") String url, @FormParam("userid") String userid,
			@FormParam("password") String password, @FormParam("authentication") String authentication) {
		logger.info("url=" + url);
		setUrl(url);
		setUserid(userid);
		setPassword(password);

		workflowCLient = new WorkflowClient(getUrl());
		// Test authentication method
		if ("Form".equalsIgnoreCase(authentication)) {
			// default basic authenticator
			FormAuthenticator formAuth = new FormAuthenticator(url, getUserid(), getPassword());
			// register the authenticator
			workflowCLient.registerClientRequestFilter(formAuth);

		} else {
			// default basic authenticator
			BasicAuthenticator basicAuth = new BasicAuthenticator(getUserid(), getPassword());
			// register the authenticator
			workflowCLient.registerClientRequestFilter(basicAuth);
		}

		// load the index table
		loadIndex();

		if (configuration != null) {
			logger.info("...connection: " + url + " = OK");
			// return "search.xhtml";
			return "redirect:query/";

		} else {
			logger.info("...connection: " + url + " = FAILED");
			return "connect.xhtml";
		}

	}

	/**
	 * This helper method loads the lucene configuration and set the property
	 * indexList with all index fields
	 */
	@SuppressWarnings("unchecked")
	private void loadIndex() {

		indexListNoAnalyse = null;
		indexListAnalyse = null;
		configuration = null;

		if (url != null && !url.isEmpty()) {

			List<ItemCollection> indexInfo = workflowCLient.getCustomResource("documents/configuration");

			if (indexInfo != null && indexInfo.size() > 0) {
				configuration = indexInfo.get(0);
				indexListNoAnalyse = configuration.getItemValue("lucence.indexFieldListNoAnalyze");
				indexListAnalyse = configuration.getItemValue("lucence.indexFieldListAnalyze");
			} else {

			}
		}
	}

	/**
	 * Returns true if the given field name is in the indexList no-Analyse
	 * 
	 * @param field
	 * @return
	 */
	public boolean isIndexNoAnalyze(String field) {
		if (indexListNoAnalyse != null) {
			return indexListNoAnalyse.contains(field);
		}
		return false;
	}

	/**
	 * Returns true if the given field name is in the indexList Analyse
	 * 
	 * @param field
	 * @return
	 */
	public boolean isIndexAnalyze(String field) {
		if (indexListAnalyse != null) {
			return indexListAnalyse.contains(field);
		}
		return false;
	}

}