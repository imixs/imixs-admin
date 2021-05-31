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
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;

import org.imixs.jwt.JWTException;
import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.CookieAuthenticator;
import org.imixs.melman.EventLogClient;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.JWTAuthenticator;
import org.imixs.melman.ModelClient;
import org.imixs.melman.WorkflowClient;

/**
 * The RestClientHandler provides methods to create instanced of a melman
 * WorklfowClient and ModelClient.
 * <p>
 * The RestClientHandler expects that a token is part of the servlet request.
 * The token is extracted to setup the connection to the rest api endpoint
 * 
 * @see org.imixs.application.admin.AdminRestService
 * @author rsoika
 */
@RequestScoped
public class RestClientHandler {

    private static Logger logger = Logger.getLogger(RestClientHandler.class.getName());

    @Inject
    private TokenService tokenService;

    public WorkflowClient createWorkflowClient(HttpServletRequest servletRequest) {
        // 1st try bearer token...
        String token = servletRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        return createWorkflowClient(token);
    }

    public EventLogClient createEventLogClient(HttpServletRequest servletRequest) {
        // 1st try bearer token...
        String token = servletRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        return createEventLogClient(token);
    }

    /**
     * creates a new Instance of an Imixs DocumentClient.
     * <p>
     * The authentication method is build from the access token
     * 
     * @see DefaultAuthenicator
     * @return
     */
    public WorkflowClient createWorkflowClient(String token) {
        String authMethod = null;
        String serviceAPI = null;
        String userid = null;
        String password = null;

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

        if ("COOKIE".equalsIgnoreCase(authMethod)) {
        	logger.info("..set cookie auth: name=" + userid + " value=" + password);
        	Cookie cookie = new Cookie(userid, password);
        	CookieAuthenticator cookieAuth = new CookieAuthenticator(cookie);
            client.registerClientRequestFilter(cookieAuth);
        }

        return client;
    }

    /**
     * creates a new Instance of an Imixs EventLogClient.
     * <p>
     * The authentication method is build from the access token
     * 
     * @see DefaultAuthenicator
     * @return
     */
    public EventLogClient createEventLogClient(String token) {
        String authMethod = null;
        String serviceAPI = null;
        String userid = null;
        String password = null;

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

        EventLogClient client = new EventLogClient(serviceAPI);

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

    /**
     * creates a new Instance of an Imixs ModelClient.
     * <p>
     * The authentication method is build from the access token
     * 
     * @see DefaultAuthenicator
     * @return
     */
    public ModelClient createModelClient(HttpServletRequest servletRequest) {
        String authMethod = null;
        String serviceAPI = null;
        String userid = null;
        String password = null;
        String token = null;

        // 1st try bearer token...
        token = servletRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
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

        ModelClient client = new ModelClient(serviceAPI);

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
