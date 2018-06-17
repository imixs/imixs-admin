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

package org.imixs.application.rest;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * The WorkflowService Handler supports methods to manage user accounts to
 * access the Imixs-Microservice platform. This service is based on the Marty
 * UserGroupService.
 * 
 * @see org.imixs.marty.ejb.security.UserGroupService
 * @author rsoika
 * 
 */
@Path("/ping")
@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_XML })
@Stateless
public class PingRestService {

	@javax.ws.rs.core.Context
	private static HttpServletRequest servletRequest;

	private static Logger logger = Logger.getLogger(PingRestService.class.getName());

	/**
	 * Ping test
	 * 
	 * @return time
	 * @throws Exception
	 */
	@GET
	@Path("/")
	public String postWorkitemJSON(InputStream requestBodyStream, @QueryParam("encoding") String encoding) {

		logger.finest("......Ping....");

		java.time.LocalDate localDate = java.time.LocalDate.now();
		return "Ping: " + localDate;

	}

}
