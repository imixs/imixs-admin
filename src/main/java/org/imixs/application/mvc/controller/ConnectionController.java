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
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

/**
 * The Connect controller is used to establish a connectio to Imixs-Worklfow
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

	ItemCollection configuration;

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

	@GET
	public String home() {
		return "connect.xhtml";
	}

	@POST
	public String connect(@FormParam("url") String url, @FormParam("userid") String userid,
			@FormParam("password") String password) {
		logger.info("url=" + url);
		setUrl(url);
		setUserid(userid);
		setPassword(password);

		// load the index table
		loadIndex();

		return "search.xhtml";
	}

	private void loadIndex() {

		if (url != null && !url.isEmpty()) {
			WorkflowClient workflowCLient = new WorkflowClient(getUrl());
			// Create a basic authenticator
			BasicAuthenticator basicAuth = new BasicAuthenticator(getUserid(), getPassword());
			// register the authenticator
			workflowCLient.registerClientRequestFilter(basicAuth);

			List<ItemCollection> indexInfo = workflowCLient.deleteCustomResource("documents/configuration");

			if (indexInfo != null && indexInfo.size() > 0) {
				configuration = indexInfo.get(0);
			}
		}
	}

	public boolean isIndex(String field) {

		if (configuration != null) {

			String s = configuration.getItemValueString("lucence.indexFieldListNoAnalyze");
			if (s.contains(field)) {
				return true;
			}
		}

		return false;
	}

}