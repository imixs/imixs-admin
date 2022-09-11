package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;

/**
 * The ExportImportController provides methods to export or import data sets
 * into the server file system.
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class ExportImportController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ExportImportController.class.getName());

    private String query;
    private String filePath;

    @Inject
    ConnectionController connectionController;

    @Inject
    LogController logController;

    @Inject
    SearchController searchController;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Export a data set
     *
     * @param id
     */
    public void exportData() {
        logController.reset();
        if (connectionController.isConnected()) {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {

                logController.info("starting export....");
                logController.info("query=" + query);
                logController.info("target=" + filePath);
                // String uri = "documents/backup/" + encode(query) + "?filepath=" + filepath;
                try {
                    String uri = "documents/backup/" + encode(query) + "?filepath=" + filePath;
                    // create put for backup ...
                    WebTarget target = workflowClient.getWebTarget(uri);
                    // here we create a dummmy object
                    target.request().put(Entity.xml(""));
                    logController.info("export successful!");
                } catch (RestAPIException e) {
                    logController.warning("export failed. " + e.getMessage());
                }
            }
        }
    }

    /**
     * Export a data set
     *
     * @param id
     */
    public void importData() {
        logController.reset();
        if (connectionController.isConnected()) {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {

                logController.info("starting export....");

                logController.info("target=" + filePath);
                // String uri = "documents/backup/" + encode(query) + "?filepath=" + filepath;
                try {
                    String uri = "documents/restore?filepath=" + filePath;
                    // create put for backup ...
                    WebTarget target = workflowClient.getWebTarget(uri);
                    // here we create a dummmy object
                    target.request().get();
                    logController.info("export successful!");
                } catch (RestAPIException e) {
                    logController.warning("export failed. " + e.getMessage());
                }
            }
            searchController.reset();
        }
    }

    /**
     * This method URL-encodes a data string so it can be used by the rest api
     *
     * @return
     */
    private String encode(String _data) {
        String encodedData = _data;
        try {
            encodedData = URLEncoder.encode(encodedData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warning("encoding of query string failed!");
        }
        return encodedData;
    }
}