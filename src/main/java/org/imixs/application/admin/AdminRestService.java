/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.application.admin;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

/**
 * The WorkflowService Handler supports methods to manage user accounts to
 * access the Imixs-Microservice platform. This service is based on the Marty
 * UserGroupService.
 * 
 * @see org.imixs.marty.ejb.security.UserGroupService
 * @author rsoika
 * 
 */
@Path("/")
@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
public class AdminRestService {

	private static Logger logger = Logger.getLogger(AdminRestService.class.getName());

	
	/**
	 * Delegater
	 * 
	 * @param workitem
	 * @return
	 */
	@POST
	@Path("/connect")
	public Response putConnectionData(XMLDocument xmlBusinessEvent) {
		boolean debug = logger.isLoggable(Level.FINE);
		if (debug) {
			logger.fine("putXMLWorkitem @PUT /workitem  delegate to POST....");
		}

		logger.info("connect ok......");

		ItemCollection connectionData = XMLDocumentAdapter.putDocument(xmlBusinessEvent);

		logger.info("api=" + connectionData.getItemValueString("api"));

		ItemCollection workitem = new ItemCollection();

		String token = JWTGenerator.generateAccessToken(connectionData.getItemValueString("api"),
				connectionData.getItemValueString("userid"), connectionData.getItemValueString("secret"),
				connectionData.getItemValueString("authmethod"));
		workitem.setItemValue("token", token);

		logger.info("Token=" + token);

		return Response.ok(XMLDocumentAdapter.getDocument(workitem)).build();

	}

	/**
	 * Delegater
	 * 
	 * @param workitem
	 * @return
	 */
	@GET
	@Path("/configuration")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public Response getConfiguration() {
		boolean debug = logger.isLoggable(Level.FINE);
		if (debug) {
			logger.fine("putXMLWorkitem @PUT /workitem  delegate to POST....");
		}

		logger.info("ok......");

		ItemCollection workitem = new ItemCollection();
		workitem.setItemValue("name", "test test");

		// return workitem....
		return Response.ok(XMLDataCollectionAdapter.getDataCollection(workitem)).build();
	}

}
