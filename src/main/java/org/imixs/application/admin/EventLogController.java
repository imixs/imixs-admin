package org.imixs.application.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.EventLogClient;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.ClientRequestFilter;

/**
 * The EventLogController loads the current event log
 *
 * @author rsoika
 *
 */
@Named
@RequestScoped
public class EventLogController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(EventLogController.class.getName());

    List<ItemCollection> entries = null;

    @Inject
    ConnectionController connectionController;

    /**
     * Returns the current job list
     *
     * @return
     */
    public List<ItemCollection> getEntries() {
        if (connectionController.isConnected()) {

            logger.info("compute eventLog...");
            entries = new ArrayList<ItemCollection>();
            try {
                WorkflowClient workflowClient = connectionController.getWorkflowClient();
                if (workflowClient != null) {
                    XMLDataCollection entryList = workflowClient.getCustomResourceXML("/eventlog");
                    entries = XMLDataCollectionAdapter.putDataCollection(entryList);
                }
            } catch (RestAPIException e) {
                logger.warning("Rest API Error: " + e.getMessage());
            }
        } else {
            entries = new ArrayList<ItemCollection>();
        }

        return entries;
    }

    /**
     * Deletes a event log entry
     *
     * @param id
     */
    public void deleteEventLogEntry(String id) {
        if (connectionController.isConnected()) {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {
                EventLogClient client = new EventLogClient(workflowClient.getBaseURI());
                // register all filters from workfow client
                List<ClientRequestFilter> filterList = workflowClient.getRequestFilterList();
                for (ClientRequestFilter filter : filterList) {
                    client.registerClientRequestFilter(filter);
                }
                try {
                    client.deleteEventLogEntry(id);
                } catch (RestAPIException e) {
                    logger.severe("Rest API Error: " + e.getMessage());
                }
            }
        }
    }

}