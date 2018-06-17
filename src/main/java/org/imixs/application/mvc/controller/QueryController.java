package org.imixs.application.mvc.controller;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
@Path("/query")
public class QueryController implements Serializable {

	private static final long serialVersionUID = 1L;

	public static int DEFAULT_PAGE_SIZE = 30;

	private static Logger logger = Logger.getLogger(QueryController.class.getName());

	@Inject
	ConnectionController connectionController;

	String query;
	int pageIndex;
	int pageSize;
	String sortBy;
	String sortOrder;
	List<ItemCollection> documents;
	

	public QueryController() {
		super();
	}

	public String getQuery() {
		if (query == null || query.isEmpty()) {
			query = "(type:\"workitem\")";
		}
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getSortBy() {
		if (sortBy == null) {
			sortBy = "$modified";
		}
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<ItemCollection> getDocuments() {
		return documents;
	}

	public void setDocuments(List<ItemCollection> documents) {
		this.documents = documents;
	}

	
	@GET
	public String home() {
		return "search.xhtml";
	}

	/**
	 * Page next
	 * 
	 * @return
	 */
	@GET
	@Path("/next")
	public String next() {
		pageIndex++;
		updateDocuments();
		return "search.xhtml";
	}

	/**
	 * Page prev
	 * 
	 * @return
	 */
	@GET
	@Path("/prev")
	public String prev() {
		if (pageIndex > 0) {
			pageIndex--;
			updateDocuments();
		}
		return "search.xhtml";
	}

	
	@POST
	public String search(@FormParam("query") String query, @FormParam("sortBy") String sortBy,
			@FormParam("sortOrder") String sortOrder) {
		logger.info("query=" + query + " sortBy=" + sortBy + " sortOrder=" + sortOrder);
		setQuery(query);
		setSortBy(sortBy);
		setSortOrder(sortOrder);

		updateDocuments();

		return "search.xhtml";
	}

	private void updateDocuments() {
		WorkflowClient workflowCLient = new WorkflowClient(connectionController.getUrl());
		// Create a basic authenticator
		BasicAuthenticator basicAuth = new BasicAuthenticator(connectionController.getUserid(),
				connectionController.getPassword());
		// register the authenticator
		workflowCLient.registerClientRequestFilter(basicAuth);

		String uri = "documents/search/" + query + "?pageSize=" + getPageSize() + "&pageIndex=" + getPageIndex()
				+ "&sortBy=" + getSortBy() + "&sortReverse=" + ("DESC".equals(getSortOrder()));

		logger.info("URI=" + uri);

		documents = workflowCLient.getCustomResource(uri);

	}

}