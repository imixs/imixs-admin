package org.imixs.application.admin;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

/**
 * The Connect controller is used to establish a connection to Imixs-Worklfow
 * remote interface.
 * 
 * The session scoped bean holds an instance of an Melman WorkfowClient. 
 * 
 * @author rsoika
 *
 */
@Named
@SessionScoped
public class ConnectionController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ConnectionController.class.getName());
	public static int DEFAULT_PAGE_SIZE = 30;

	String url;
	String userid;
	String password;
	String authMethod;

	List<String> indexListNoAnalyse;
	List<String> indexListAnalyse;
	ItemCollection configuration;

	WorkflowClient workflowCLient = null;

	@Inject
	DataController dataController;

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

	public String getAuthMethod() {
		return authMethod;
	}

	public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}

	public List<String> getIndexListNoAnalyse() {
		return indexListNoAnalyse;
	}

	public List<String> getIndexListAnalyse() {
		return indexListAnalyse;
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
	 * @return true if connection was established.
	 */
	public boolean connect(String url, String userid, String password, String authMethod) {
		logger.info("url=" + url);
		setUrl(url);
		setUserid(userid);
		setPassword(password);

		workflowCLient = new WorkflowClient(getUrl());
		// Test authentication method
		if ("Form".equalsIgnoreCase(authMethod)) {
			// default basic authenticator
			FormAuthenticator formAuth = new FormAuthenticator(url, getUserid(),
					getPassword());
			// register the authenticator
			workflowCLient.registerClientRequestFilter(formAuth);

		} else {
			// default basic authenticator
			BasicAuthenticator basicAuth = new BasicAuthenticator(getUserid(),
					getPassword());
			// register the authenticator
			workflowCLient.registerClientRequestFilter(basicAuth);
		}

		// load the index table
		loadIndex();

		return (configuration != null);

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

		if (url!= null && !url.isEmpty()) {

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