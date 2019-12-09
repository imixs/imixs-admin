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

import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.jwt.JWTException;
import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.JWTAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
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

	@Context
	private HttpServletRequest servletRequest;

	@Inject
	TokenService tokenService;

	/**
	 * The connect resource generates an access-token for the given api endpoint and
	 * requests the current index configuration.
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

		String token;
		try {
			token = tokenService.generateAccessToken(connectionData.getItemValueString("api"),
					connectionData.getItemValueString("userid"), connectionData.getItemValueString("secret"),
					connectionData.getItemValueString("authmethod"));
		} catch (JWTException e) {
			// invalid JWT
			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		}

		ItemCollection workitem;
		try {
			workitem = getConfiguration(token);
		} catch (RestAPIException e) {
			logger.severe("Rest API Error: " + e.getMessage());
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		}
		workitem.setItemValue("token", token);

		logger.info("Token=" + token);

		return Response.ok(XMLDocumentAdapter.getDocument(workitem)).build();

	}

	/**
	 * Delegater - read schema configuration from DocumentService
	 * 
	 * @param workitem
	 * @return
	 * @throws RestAPIException
	 */

	private ItemCollection getConfiguration(String token) throws RestAPIException {
		boolean debug = logger.isLoggable(Level.FINE);
		if (debug) {
			logger.fine("putXMLWorkitem @PUT /workitem  delegate to POST....");
		}

		logger.info("ok......");
		WorkflowClient client = createWorkflowClient(token);

		List<ItemCollection> result = client.getCustomResource("documents/configuration");
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	/**
	 * creates a new Instance of an Imixs DocumentClient.
	 * <p>
	 * The authentication method is build from the access token
	 * 
	 * @see DefaultAuthenicator
	 * @return
	 */
	private WorkflowClient createWorkflowClient(String _token) {
		String authMethod = null;
		String serviceAPI = null;
		String userid = null;
		String password = null;

		// 1st try bearer token...
		String token = _token;
		if (token == null) {
			token = servletRequest.getHeader("Authorization");
			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring("Bearer ".length());
			}
		}
		try {
			// extract the token....
			String payload = tokenService.getPayload(token);
			// extract payload.....
			JsonObject payloadObject = null;
			JsonReader reader = null;

			reader = Json.createReader(new StringReader(payload));
			payloadObject = reader.readObject();

			authMethod = payloadObject.getString("autmethod");
			serviceAPI = payloadObject.getString("api");
			userid = payloadObject.getString("sub");
			password = payloadObject.getString("secret");
			String iat = payloadObject.getString("iat");

			// validate iat
			long lIat = Long.parseLong(iat); 
			long lexpireTime = 3600; // 1h
			long lNow = new Date().getTime();
			if ((lIat*1000) + (lexpireTime*1000) < lNow) {
				logger.warning("JWT expired!");
				return null;
			}

		} catch (javax.json.stream.JsonParsingException | JWTException j1) {
			logger.severe("invalid token: " + j1.getMessage());
			return null;
		}

		WorkflowClient client = new WorkflowClient(serviceAPI);

		if ("JWT".equalsIgnoreCase(authMethod)) {
			JWTAuthenticator jwtAuht = new JWTAuthenticator(password);
			client.registerClientRequestFilter(jwtAuht);
		}

		if ("FORM".equalsIgnoreCase(authMethod)) {
			FormAuthenticator formAuth = new FormAuthenticator(serviceAPI, userid, password);
			client.registerClientRequestFilter(formAuth);
		}

		if ("BASIC".equalsIgnoreCase(authMethod)) {
			BasicAuthenticator basicAuth = new BasicAuthenticator(userid, password);
			client.registerClientRequestFilter(basicAuth);
		}

		return client;
	}
}
