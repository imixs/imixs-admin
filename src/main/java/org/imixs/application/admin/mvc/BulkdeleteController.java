package org.imixs.application.admin.mvc;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mvc.Models;
import javax.mvc.annotation.Controller;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.imixs.application.admin.ConnectionController;
import org.imixs.application.admin.DataController;
import org.imixs.workflow.ItemCollection;

/**
 * The bulkdelete controller is used to delete a resultset of workitems
 * 
 * @author rsoika
 *
 */
@Controller
@Path("/bulkdelete")
@Named
public class BulkdeleteController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(BulkdeleteController.class.getName());

	@Inject
	ConnectionController connectionController;

	@Inject
	private Models model;

	
	@Inject
	DataController dataController;

	public BulkdeleteController() {
		super();
	}

	@GET
	public String home() {
		model.put("deletestatus","");
		return "bulkdelete.xhtml";
	}

	/**
	 * Runs the bulk update
	 * 
	 * @param query
	 * @param sortBy
	 * @param sortOrder
	 * @param pageIndex
	 * @param pageSize
	 * @param appendValues
	 * @return
	 */
	@POST
	public String delete(@FormParam("query") String query, @FormParam("sortBy") String sortBy,
			@FormParam("sortOrder") String sortOrder, @FormParam("pageIndex") int pageIndex,
			@FormParam("pageSize") int pageSize,

			@FormParam("fieldName") String fieldName, @FormParam("fieldType") String fieldType,
			@FormParam("fieldValues") String fieldValues, @FormParam("workflowEvent") int workflowEvent,

			@FormParam("appendValues") boolean appendValues) {

		logger.info("query=" + query + " sortBy=" + sortBy + " sortOrder=" + sortOrder);
		dataController.setQuery(query);
		dataController.setSortBy(sortBy);
		dataController.setSortOrder(sortOrder);
		dataController.setPageIndex(pageIndex);
		dataController.setPageSize(pageSize);

		dataController.setAppendValues(appendValues);
		dataController.setFieldName(fieldName);
		dataController.setFieldType(fieldType);
		dataController.setFieldValues(fieldValues);
		dataController.setWorkflowEvent(workflowEvent);

		deleteSearchResult();

		return "bulkdelete.xhtml";
	}

	/**
	 * This method iterates over the serach result and updates the documetns based
	 * on the given settings stored in the DataController. This method is called by
	 * the update method.
	 */
	private void deleteSearchResult() {
		if (connectionController.getConfiguration() != null && !connectionController.getUrl().isEmpty()) {
			model.put("deletestatus","");

			String uri = "documents/search/" + dataController.getQuery() + "?pageSize=" + dataController.getPageSize()
					+ "&pageIndex=" + dataController.getPageIndex() + "&sortBy=" + dataController.getSortBy()
					+ "&sortReverse=" + ("DESC".equals(dataController.getSortOrder()));

			logger.info("URI=" + uri);

			List<ItemCollection> documents = connectionController.getWorkflowCLient().getCustomResource(uri);

			// iterate over all documents....
			for (ItemCollection document : documents) {
				connectionController.getWorkflowCLient().deleteWorkitem(document.getUniqueID());
			}
			
			model.put("deletestatus", documents.size() + " documents deleted.");
		}
	}

}