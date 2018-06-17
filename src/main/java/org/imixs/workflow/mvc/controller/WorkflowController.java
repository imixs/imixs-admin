package org.imixs.workflow.mvc.controller;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
public abstract class WorkflowController {

	private ItemCollection workitem = new ItemCollection();

	private static Logger logger = Logger.getLogger(WorkflowController.class.getName());

	
	@GET
	@Path("{uniqueid}")
	public String getWorkitemByUnqiueID(@PathParam("uniqueid") String uid) {
	return "";
	}

	/**
	 * Creates a new process instance based on the given model version
	 * 
	 * @return
	 */
	@POST
	@Path("{modelversion}/{task}")
	public String createWorkitem(@PathParam("modelversion") String modelversion, @PathParam("task") String task) {
		return "";
	}



	
}