package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The EventLogController loads the current event log
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class ModelController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ModelController.class.getName());

    List<ItemCollection> models = null;

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    /**
     * Reset job list
     */
    public void reset() {
        models = null;
    }

    /**
     * Returns the current job list
     *
     * @return
     */
    public List<ItemCollection> getModels() {
        if (models == null) {
            logger.info("compute model list...");
            models = new ArrayList<ItemCollection>();
            try {
                WorkflowClient workflowClient = connectionController.getWorkflowClient();
                if (workflowClient != null) {
                    String query = "SELECT document FROM Document AS document WHERE document.type='model'";
                    try {
                        query = URLEncoder.encode(query, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    XMLDataCollection result = workflowClient.getCustomResourceXML("documents/jpql/" + query);
                    models = XMLDataCollectionAdapter.putDataCollection(result);
                }
            } catch (RestAPIException e) {
                logger.warning("Rest API Error: " + e.getMessage());

            }
        }
        return models;
    }

}