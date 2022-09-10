package org.imixs.application.admin;

import java.io.Serializable;
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
public class EventLogController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(EventLogController.class.getName());

    List<ItemCollection> entries = null;

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    /**
     * Reset job list
     */
    public void reset() {
        entries = null;
    }

    /**
     * Returns the current job list
     *
     * @return
     */
    public List<ItemCollection> getEntries() {
        if (entries == null) {
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
        }

        return entries;
    }

}