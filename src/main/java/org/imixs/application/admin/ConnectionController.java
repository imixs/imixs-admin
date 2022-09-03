package org.imixs.application.admin;

import java.io.Serializable;
import java.util.logging.Logger;

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
    private String type = "BASIC";
    private boolean connected;

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
        if (conversation.isTransient()) {
            conversation.setTimeout(
                    ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                            .getSession().getMaxInactiveInterval() * 1000);
            conversation.begin();
            connected = true;
            logger.finest("......start new conversation, id=" + conversation.getId());
        }
        logger.info("...connting: " + endpoint);

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

}