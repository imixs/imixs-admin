package org.imixs.application.admin.mvc;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.imixs.application.admin.ConnectionController;
import org.imixs.application.admin.DataController;

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

	private static Logger logger = Logger.getLogger(DocumentController.class.getName());

	@Inject
	ConnectionController connectionController;

	@Inject
	DataController dataController;

	public DocumentController() {
		super();
	}

	@GET
	@Path("/{uniqueid}")
	public String loadDocument(@PathParam("uniqueid") String uniqueid) {

		logger.finest("......load document: " + uniqueid);

		dataController.setDocument(connectionController.getWorkflowCLient().getWorkitem(uniqueid));

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