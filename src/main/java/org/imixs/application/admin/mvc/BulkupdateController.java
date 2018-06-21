package org.imixs.application.admin.mvc;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * The bulkupdate controller is used to update a resultset of workitems
 * 
 * @author rsoika
 *
 */
@Controller
@Path("/bulkupdate")
@Named
public class BulkupdateController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(BulkupdateController.class.getName());

	@Inject
	ConnectionController connectionController;

	@Inject
	DataController dataController;

	public BulkupdateController() {
		super();
	}

	@GET
	public String home() {
		return "bulkupdate.xhtml";
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
	public String update(@FormParam("query") String query, @FormParam("sortBy") String sortBy,
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

		updateSearchResult();

		return "bulkupdate.xhtml";
	}

	/**
	 * This method iterates over the serach result and updates the documetns based
	 * on the given settings stored in the DataController. This method is called by
	 * the update method.
	 */
	private void updateSearchResult() {
		if (connectionController.getConfiguration() != null && !connectionController.getUrl().isEmpty()) {

			String uri = "documents/search/" + dataController.getQuery() + "?pageSize=" + dataController.getPageSize()
					+ "&pageIndex=" + dataController.getPageIndex() + "&sortBy=" + dataController.getSortBy()
					+ "&sortReverse=" + ("DESC".equals(dataController.getSortOrder()));

			logger.info("URI=" + uri);

			List<ItemCollection> documents = connectionController.getWorkflowCLient().getCustomResource(uri);

			// first convert the newValue in a list of objects based on the selected
			// fieldType...
			String fieldType = dataController.getFieldType();
			String newFieldValues=dataController.getFieldValues();
			String[] rawItems = newFieldValues.split("\\r?\\n");
			// now convert to selected type
			List<Object> typedItems = new ArrayList<Object>();
			for (String rawValue : rawItems) {
				if ("xs:int".equals(fieldType)) {
					typedItems.add(Integer.parseInt(rawValue));
				} else if ("xs:dateTime".equals(fieldType)) {
					SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-mm-dd");
					try {
						Date date = dt1.parse(rawValue);
						typedItems.add(date);
					} catch (ParseException e) {
						logger.warning("...unable to convert '" + rawValue + "' into date object!");
						typedItems.add(rawValue);
					}

				} else if ("xs:boolean".equals(fieldType)) {
					typedItems.add(Boolean.parseBoolean(rawValue));
				} else {
					typedItems.add(rawValue);
				}

			}

			// iterate over all documents....
			for (ItemCollection document : documents) {
				// update documetn item
				if (dataController.isAppendValues()) {
					// append
					document.appendItemValue(dataController.getFieldName(), typedItems);
				} else {
					// replace
					document.replaceItemValue(dataController.getFieldName(), typedItems);
				}

				// Save or Process workitem?
				if (dataController.getWorkflowEvent() <= 0) {
				}

			}

			dataController.setDocuments(documents);
		}
	}

}