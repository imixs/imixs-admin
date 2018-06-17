package org.imixs.application.mvc.controller;

import java.util.logging.Logger;

import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.Path;

import org.imixs.workflow.mvc.controller.WorkflowController;

/**
 * Controller to manage active imixs-workflow instances.
 * 
 * @author rsoika
 *
 */
@Controller
@Path("ticket")
@Named
public class TicketController extends WorkflowController {

	private static Logger logger = Logger.getLogger(TicketController.class.getName());

	/**
	 * Initialize TeamController
	 */
	public TicketController() {
		super();
		
	}

}