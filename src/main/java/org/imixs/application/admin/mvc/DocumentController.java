package org.imixs.application.admin.mvc;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.imixs.application.admin.ConnectionController;
import org.imixs.workflow.ItemCollection;

/**
 * The Connect controller is used to establish a connectio to Imixs-Worklfow
 * remote interface.
 * 
 * @author rsoika
 *
 */
@Controller
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

	

	/**
	 * Returns a formated html string of a item value
	 * 
	 * @param itemname
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getFormatedItemValue(String itemname) {
		String result = "";
		if (document != null) {
			List itemvalues = document.getItemValue(itemname);
			for (Object o : itemvalues) {
				result += "<strong>" + o.getClass().getSimpleName() + ":</strong> " + o;
				result += "<br />";
			}
		}
		return result;
	}

	@GET
	@Path("/{uniqueid}")
	public String loadDocument(@PathParam("uniqueid") String uniqueid) {

		logger.finest("......load document: " + uniqueid);
		
		document = connectionController.getWorkflowCLient().getWorkitem(uniqueid);

		return "document.xhtml";
	}
	
	
	@GET
	@Path("/action/delete/{uniqueid}")
	public String actionDeleteDocument(@PathParam("uniqueid") String uniqueid) {

		logger.finest("......delete document: " + uniqueid);
		connectionController.getWorkflowCLient().deleteWorkitem(uniqueid);

		return "redirect:query/";
	}

}