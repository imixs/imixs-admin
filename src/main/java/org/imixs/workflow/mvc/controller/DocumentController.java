package org.imixs.workflow.mvc.controller;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.imixs.workflow.ItemCollection;

/**
 * The DocumentController provide a generic controller class to handle document
 * entities managed by the Imixs-Workflow DocumentService.
 * 
 * The DocumentController provides a set of properties to describe a specific
 * document entity.
 * 
 * @author rsoika
 *
 */
public abstract class DocumentController {

	private ItemCollection workitem = new ItemCollection();
	private String documentsView;
	private String documentView;
	private String documentType;

	private static Logger logger = Logger.getLogger(DocumentController.class.getName());

	@Inject
	protected Event<WorkitemEvent> events;

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getDocumentsView() {
		return documentsView;
	}

	public void setDocumentsView(String documentsView) {
		this.documentsView = documentsView;
	}

	public String getDocumentView() {
		return documentView;
	}

	public void setDocumentView(String documentView) {
		this.documentView = documentView;
	}

	/**
	 * load list of documents
	 * 
	 * @return
	 */
	@GET
	public String showDocuments() {
		return getDocumentsView();
	}

	@GET
	@Path("{uniqueid}")
	public String getDocumentByUnqiueID(@PathParam("uniqueid") String uid) {
		logger.info("......load document: " + uid);
		return "";
	}

	@POST
	public String createDocument() {
		return "";
	}

	@POST
	@Path("{uniqueid}")
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
	public String saveDocument(@PathParam("uniqueid") String uid, InputStream requestBodyStream) {
		return "";
	}

	public ItemCollection getWorkitem() {

		return workitem;
	}

	public void setWorkitem(ItemCollection workitem) {
		this.workitem = workitem;
		events.fire(new WorkitemEvent(workitem, WorkitemEvent.WORKITEM_CHANGED));
	}

}