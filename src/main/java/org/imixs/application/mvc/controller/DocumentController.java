package org.imixs.application.mvc.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
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
@Path("/document")
public class DocumentController implements Serializable {

	private static final long serialVersionUID = 1L;

	public static int DEFAULT_PAGE_SIZE = 30;

	private static Logger logger = Logger.getLogger(DocumentController.class.getName());

	@Inject
	ConnectionController connectionController;

	ItemCollection document;

	public DocumentController() {
		super();
	}

	public ItemCollection getDocument() {
		return document;
	}

	public void setDocument(ItemCollection document) {
		this.document = document;
	}

	public List<String> getItemNames() {

		Map<String, ?> itemList = document.getItemList();
		List<String> result = new ArrayList<String>();
		result.addAll(itemList.keySet());
		// sort result
		Collections.sort(result);

		return result;
	}

	
	/**
	 * Returns a formated html string of a item value
	 * @param itemname
	 * @return
	 */
	public String getFormatedItemValue(String itemname) {
		String result="";
		if (document!=null) {
			List itemvalues = document.getItemValue(itemname);
			
			for (Object o: itemvalues) {
				
				result+="<strong>"+o.getClass().getSimpleName() + ":</strong> " + o;
				
				result+="<br />";
				
			}
			
			
		}
		return result;
	}
	
	
	@GET
	@Path("/{uniqueid}")
	public String loadDocument(@PathParam("uniqueid") String uniqueid) {

		WorkflowClient workflowCLient = new WorkflowClient(connectionController.getUrl());
		// Create a basic authenticator
		BasicAuthenticator basicAuth = new BasicAuthenticator(connectionController.getUserid(),
				connectionController.getPassword());
		// register the authenticator
		workflowCLient.registerClientRequestFilter(basicAuth);

		document = workflowCLient.getWorkitem(uniqueid);

		return "document.xhtml";
	}

}