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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.jwt.JWTException;
import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.JWTAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
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
	 * The connect resource generates an access-token for the given api endpoint and
	 * requests the current index configuration.
	 * 
	 * @param workitem
	 * @return
	 */
	@POST
	@Path("/search")
	public Response putSearchRequest(XMLDocument xmlBusinessEvent) {
		boolean debug = logger.isLoggable(Level.FINE);
		if (debug) {
			logger.fine("putXMLWorkitem @PUT /search  delegate to POST....");
		}

		ItemCollection connectionData = XMLDocumentAdapter.putDocument(xmlBusinessEvent);
		String query = connectionData.getItemValueString("query");
		String sortBy = connectionData.getItemValueString("sortby");
		String sortOrder = connectionData.getItemValueString("sortorder");
		int pageIndex = connectionData.getItemValueInteger("pageindex");
		int pageSize = connectionData.getItemValueInteger("pagesize");

		// set items!
		String items = "$uniqueid,$workflowstatus,txtname,$workflowsummary,$modified,$created";
		String token = servletRequest.getHeader("Authorization");
		if (token.toLowerCase().startsWith("bearer")) {
			token = token.substring(7);
		}

		WorkflowClient client = createWorkflowClient(token);
		if (client != null) {
			XMLDataCollection result;
			try {
				result = client.getCustomResourceXML(
						"documents/search/" + query + "?pageIndex=" + pageIndex + "&pageSize=" + pageSize + "&sortBy="
								+ sortBy + "&sortReverse=" + (sortOrder.equalsIgnoreCase("desc")) + "&items=" + items);

				return Response
						// Set the status and Put your entity here.
						.ok(result)
						// Add the Content-Type header to tell Jersey which format it should marshall
						// the entity into.
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML).build();
			} catch (RestAPIException e) {
				logger.severe("Rest API Error: " + e.getMessage());
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}
		}
		// no result
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	/**
	 * The update resource starts a bulk update 
	 * 
	 * @param workitem
	 * @return
	 */
	@POST
	@Path("/update")
	public Response putBulkUpdate(XMLDocument xmlBusinessEvent) {
		boolean debug = logger.isLoggable(Level.FINE);
		int updates = 0;
		if (debug) {
			logger.fine("putXMLWorkitem @PUT /update  delegate to POST....");
		}

		ItemCollection connectionData = XMLDocumentAdapter.putDocument(xmlBusinessEvent);
		String query = connectionData.getItemValueString("query");
		String sortBy = connectionData.getItemValueString("sortby");
		String sortOrder = connectionData.getItemValueString("sortorder");
		int pageIndex = connectionData.getItemValueInteger("pageindex");
		int pageSize = connectionData.getItemValueInteger("pagesize");

		String fieldname = connectionData.getItemValueString("fieldname");
		String fieldType = connectionData.getItemValueString("fieldtype");
		boolean append = connectionData.getItemValueBoolean("append");
		String newFieldValues = connectionData.getItemValueString("values");
		int event = connectionData.getItemValueInteger("event");

		// set items!
		String items = "$uniqueid,$taskid,$modelversion," + fieldname;
		String token = servletRequest.getHeader("Authorization");
		if (token.toLowerCase().startsWith("bearer")) {
			token = token.substring(7);
		}

		WorkflowClient client = createWorkflowClient(token);
		if (client != null) {
			List<ItemCollection> documents = null;
			try {
				documents = client.getCustomResource(
						"documents/search/" + query + "?pageIndex=" + pageIndex + "&pageSize=" + pageSize + "&sortBy="
								+ sortBy + "&sortReverse=" + (sortOrder.equalsIgnoreCase("desc")) + "&items=" + items);

				logger.info("..." + documents.size() + " documents selected for bulk update...");

				// first convert the newValue in a list of objects based on the selected
				// fieldType...
				String[] rawItems = newFieldValues.split("\\r?\\n");
				// now convert to selected type
				List<Object> typedItems = new ArrayList<Object>();
				for (String rawValue : rawItems) {
					if ("xs:int".equals(fieldType)) {
						typedItems.add(Integer.parseInt(rawValue));
					} else if ("xs:dateTime".equals(fieldType)) {
						SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-mm-dd");
						try {
							Date date = dt1.parse(rawValue);
							typedItems.add(date);
						} catch (ParseException e) {
							logger.warning("...unable to convert '" + rawValue + "' into date object!");
							typedItems.add(rawValue);
						}

					} else if ("xs:boolean".equals(fieldType)) {
						typedItems.add(Boolean.parseBoolean(rawValue));
					} else {
						typedItems.add(rawValue);
					}

				}

				// iterate over all documents....
				for (ItemCollection document : documents) {
					// update documetn item
					if (append) {
						// append
						document.appendItemValue(fieldname, typedItems);
					} else {
						// replace
						document.replaceItemValue(fieldname, typedItems);
					}

					// Save or Process workitem?
					if (event <= 0) {
						// save workitem
						client.saveDocument(document);
						updates++;
					} else {
						// process workitem
						document.setEventID(event);
						client.processWorkitem(document);
						updates++;
					}

				}

				logger.info("...update finished: " + updates + " udpates.");

				ItemCollection result = new ItemCollection();
				result.setItemValue("updates", updates);
				result.setItemValue("message", updates + " documents updated.");
				return Response
						// Set the status and Put your entity here.
						.ok(XMLDocumentAdapter.getDocument(result))
						// Add the Content-Type header to tell Jersey which format it should marshall
						// the entity into.
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML).build();

			} catch (RestAPIException e) {
				logger.severe("Rest API Error: " + e.getMessage());
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}
		}
		// no result
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	/**
	 * The connect resource generates an access-token for the given api endpoint and
	 * requests the current index configuration.
	 * 
	 * @param workitem
	 * @return
	 */
	@GET
	@Path("/documents/{uniqueid : ([0-9a-f]{8}-.*|[0-9a-f]{11}-.*)}")
	public Response getDocument(@PathParam("uniqueid") String uniqueid, @QueryParam("items") String items) {

		boolean debug = logger.isLoggable(Level.FINE);
		if (debug) {
			logger.fine("getDocument @GET /documents/id delegate to GET....");
		}

		String token = servletRequest.getHeader("Authorization");
		if (token.toLowerCase().startsWith("bearer")) {
			token = token.substring(7);
		}

		WorkflowClient client = createWorkflowClient(token);
		if (client != null) {

			try {
				ItemCollection document = client.getDocument(uniqueid);
				return Response.ok(XMLDocumentAdapter.getDocument(document)).build();
			} catch (RestAPIException e) {
				logger.severe("Rest API Error: " + e.getMessage());
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}
		}
		// no result
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	/**
	 * The connect resource generates an access-token for the given api endpoint and
	 * requests the current index configuration.
	 * 
	 * @param workitem
	 * @return
	 */
	@DELETE
	@Path("/documents/{uniqueid : ([0-9a-f]{8}-.*|[0-9a-f]{11}-.*)}")
	public Response deleteDocument(@PathParam("uniqueid") String uniqueid) {

		boolean debug = logger.isLoggable(Level.FINE);
		if (debug) {
			logger.fine("getDocument @DELETE /documents/id delegate to DELETE....");
		}

		String token = servletRequest.getHeader("Authorization");
		if (token.toLowerCase().startsWith("bearer")) {
			token = token.substring(7);
		}

		WorkflowClient client = createWorkflowClient(token);
		if (client != null) {

			try {
				client.deleteDocument(uniqueid);
				return Response.status(Response.Status.OK).build();
			} catch (RestAPIException e) {
				logger.severe("Rest API Error: " + e.getMessage());
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}
		}
		// no result
		return Response.status(Response.Status.NO_CONTENT).build();
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
			if ((lIat * 1000) + (lexpireTime * 1000) < lNow) {
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
