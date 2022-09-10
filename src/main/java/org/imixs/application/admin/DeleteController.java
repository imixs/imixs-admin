package org.imixs.application.admin;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The UpdateController stores the data for bulk updates
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class DeleteController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(DeleteController.class.getName());

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    @Inject
    LogController logController;

    /**
     * This method starts a bulk update
     */
    public void bulkDelete() {
        logger.info("...starting Bulk Delete");
        int deletions = 0;
        logController.reset();
        try {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {
                // set items!
                workflowClient.setItems("$uniqueid");
                searchController.reset();
                List<ItemCollection> documents = searchController.getSearchResult();
                logController.info("..." + documents.size() + " documents selected for bulk delete...");

                // iterate over all documents....
                for (ItemCollection document : documents) {
                    // delete workitem
                    workflowClient.deleteDocument(document.getUniqueID());
                    deletions++;
                }
                logController.info("...bulk delete finished: " + deletions + " deletions.");
                searchController.reset();
            }
        } catch (RestAPIException e) {
            logController.warning("Rest API Error: " + e.getMessage());
        }
    }

}