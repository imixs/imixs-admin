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
 * The QueryController is used to search for workitems
 * 
 * @author rsoika
 *
 */
@Controller
@Path("/adminp")
@Named
public class AdminPController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AdminPController.class.getName());

	@Inject
	ConnectionController connectionController;

	@Inject
	DataController dataController;

	public AdminPController() {
		super();
	}

	@GET
	public String home() {
		updateDocuments();
		return "adminp.xhtml";
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