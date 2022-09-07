package org.imixs.application.admin;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The ConnectionController stores the connection data to an Imixs Workflow
 * Instance Endpoint
 * <p>
 * The endpoint defines the URL to the rest API. The key is the userID or cookie
 * used for authentication. The token is the user password or cookie value for
 * authentication. The type defines the login type.
 * <p>
 * The method <code>connect</code> can be used to establish a test connection
 * indicating if the Rest API of the corresponding workflow instance is working.
 * The method also starts a JSF conversation scope.
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class ConnectionController implements Serializable {

    private static final long serialVersionUID = 7027147503119012594L;

    private static Logger logger = Logger.getLogger(ConnectionController.class.getName());

    private String endpoint;
    private String key;
    private String token;
    private String type = "FORM";
    private String errorMessage = "";
    private boolean connected;
    private ItemCollection luceneConfiguration = null;
    private WorkflowClient workflowClient = null;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Inject
    private Conversation conversation;

    /**
     * Starts a new conversation
     */
    public void connect() {
        logger.info("...connting: " + endpoint);

        // get JSESSIONID
        workflowClient = getWorkflowClient();

        luceneConfiguration = loadLuceneConfiguration();
        // test if the configuration was loaded successful
        connected = (luceneConfiguration != null);

        if (conversation.isTransient()) {
            conversation.setTimeout(
                    ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                            .getSession().getMaxInactiveInterval() * 1000);
            conversation.begin();
            logger.finest("......start new conversation, id=" + conversation.getId());
        }

    }

    /**
     * Closes the current conversation and reset the current connection. A
     * conversation is automatically started by the method connect(). You can call
     * the disconnect() method in a actionListener on any JSF navigation action.
     */
    public void disconnect() {
        if (!conversation.isTransient()) {
            logger.finest("......stopping conversation, id=" + conversation.getId());
            conversation.end();
            connected = false;
            luceneConfiguration = null;
            endpoint = null;
            key = null;
            token = null;
            workflowClient = null;
        }

    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the Lucene index schema
     *
     * @return
     */
    public ItemCollection getLuceneConfiguration() {
        return luceneConfiguration;
    }

    /**
     * This method creates a WorkflowRest Client and caches the instance
     *
     * @return
     */
    public WorkflowClient getWorkflowClient() {
        if (workflowClient == null) {
            // Init the workflowClient with a basis URL
            workflowClient = new WorkflowClient(getEndpoint());
            if ("BASIC".equals(getType())) {
                // Create a authenticator
                BasicAuthenticator basicAuth = new BasicAuthenticator(getKey(), getToken());
                // register the authenticator
                workflowClient.registerClientRequestFilter(basicAuth);
            }
            if ("FORM".equals(getType())) {
                // Create a authenticator
                FormAuthenticator formAuth = new FormAuthenticator(getEndpoint(), getKey(), getToken());
                // register the authenticator
                workflowClient.registerClientRequestFilter(formAuth);

            }
        }

        return workflowClient;

    }

    /**
     * This method test if the api endpoint documents/configuration is reachable.
     * The endpoint represents the LuceneConfiguration. If the configuration could
     * not be loaded than something with the Rest API endpoint is wrong.
     *
     * @return true if api call was successful
     */
    private ItemCollection loadLuceneConfiguration() {
        logger.info("...compute test result...");
        List<ItemCollection> result;
        try {
            WorkflowClient workflowClient = getWorkflowClient();
            // we do not expect a result
            workflowClient.setPageSize(1);

            // documents/configuration
            result = workflowClient.getCustomResource("documents/configuration");
            // a valid result object contains the item lucence.fulltextFieldList'
            if (result != null && result.size() > 0) {
                errorMessage = "";
                return result.get(0);
            } else {
                luceneConfiguration = null;
                errorMessage = "Unable to connect to endpoint!";
                logger.severe(errorMessage);
                return null;
            }
        } catch (RestAPIException e) {
            errorMessage = "Unable to connect to endpoint!";
            logger.severe("Unable to connect to endpoint: " + e.getMessage());
            return null;
        }
    }

}