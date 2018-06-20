package org.imixs.application.admin.mvc;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.imixs.application.admin.ConnectionController;
import org.imixs.application.admin.DataController;
import org.imixs.workflow.ItemCollection;

/**
 * The Connect controller is used to establish a connectio to Imixs-Worklfow
 * remote interface.
 * 
 * @author rsoika
 *
 */
@Controller
@Path("/query")
@Named
public class QueryController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(QueryController.class.getName());

	@Inject
	ConnectionController connectionController;

	@Inject
	DataController dataController;

	public QueryController() {
		super();
	}

	@GET
	public String home() {
		updateDocuments();
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
		dataController.setPageIndex(dataController.getPageIndex() + 1);
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
		if (dataController.getPageIndex() > 0) {
			dataController.setPageIndex(dataController.getPageIndex() - 1);
			updateDocuments();
		}
		return "search.xhtml";
	}

	@POST
	public String search(@FormParam("query") String query, @FormParam("sortBy") String sortBy,
			@FormParam("sortOrder") String sortOrder, @FormParam("pageIndex") int pageIndex,
			@FormParam("pageSize") int pageSize) {
		logger.info("query=" + query + " sortBy=" + sortBy + " sortOrder=" + sortOrder);
		dataController.setQuery(query);
		dataController.setSortBy(sortBy);
		dataController.setSortOrder(sortOrder);
		dataController.setPageIndex(pageIndex);
		dataController.setPageSize(pageSize);

		updateDocuments();

		return "search.xhtml";
	}

	private void updateDocuments() {
		if (connectionController.getConfiguration() != null && !connectionController.getUrl().isEmpty()) {

			String uri = "documents/search/" + dataController.getQuery() + "?pageSize=" + dataController.getPageSize()
					+ "&pageIndex=" + dataController.getPageIndex() + "&sortBy=" + dataController.getSortBy()
					+ "&sortReverse=" + ("DESC".equals(dataController.getSortOrder()));

			logger.info("URI=" + uri);

			List<ItemCollection> documents = connectionController.getWorkflowCLient().getCustomResource(uri);

			dataController.setDocuments(documents);
		}
	}

}