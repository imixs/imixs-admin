package org.imixs.application.mvc.controller;

import java.util.logging.Logger;

import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Test the cassandra cluster connection
 * 
 * @author rsoika
 *
 */
@Controller
@Named
@Path("/home")
public class WelcomeController {
	private static Logger logger = Logger.getLogger(WelcomeController.class.getName());

	String id;
	
	
	
	
	
	public String getId() {
		logger.info("get id...");
		return ""+System.currentTimeMillis();
	}

	public void setId(String id) {
		this.id = id;
	}

	@GET
	public String home() {
		logger.info("home..");
		return "index.xhtml";
	}

}