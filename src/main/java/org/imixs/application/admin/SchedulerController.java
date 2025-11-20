package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

import jakarta.annotation.PostConstruct;
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
public class SchedulerController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(SchedulerController.class.getName());

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    @Inject
    LogController logController;

    private ItemCollection schedulerConfig = null;
    private String timerSettings;

    @PostConstruct
    public void init() {
        schedulerConfig = loadSchedulerConfiguration();

    }

    /**
     * convert the chron settings in one string
     *
     * @return
     */
    public String getTimerSettings() {

        if (schedulerConfig != null) {
            timerSettings = "";
            List<String> cronSettings = schedulerConfig.getItemValueList("_scheduler_definition", String.class);
            for (String value : cronSettings) {
                timerSettings = timerSettings + value + "\n";
            }
        } else {
            timerSettings = "hour=*";
        }
        return timerSettings;
    }

    /**
     * convert the chron settings in to a value list
     *
     * @return
     */
    public void setTimerSettings(String timerSettings) {
        this.timerSettings = timerSettings;
        if (schedulerConfig != null) {
            String[] valueList = timerSettings.split("\n");
            schedulerConfig.setItemValue("_scheduler_definition", Arrays.asList(valueList));
        }
    }

    /**
     * This method starts a bulk update
     */
    public void startScheduler() {
        logger.info("...starting Worklfow Scheduler");
        int deletions = 0;
        logController.reset();
        try {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {
                // set items!
                workflowClient.setItems("");
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

    /**
     * This method loads the default scheduler configuration. If no configuration is
     * found the method creates an empty config document
     *
     * @return true if api call was successful
     */
    private ItemCollection loadSchedulerConfiguration() {

        WorkflowClient workflowClient = connectionController.getWorkflowClient();
        if (workflowClient != null) {
            logger.info("├── Load Workflow Scheduler configuration");
            try {
                String query = "(type:scheduler) AND (name:org.imixs.workflow.scheduler)";
                List<ItemCollection> searchResult = workflowClient.searchDocuments(query);
                if (searchResult == null || searchResult.size() == 0) {
                    logger.info("│   ├──  Create new Workflow Scheduler default configuration");
                    schedulerConfig = new ItemCollection();
                    schedulerConfig.setType("scheduler");
                    schedulerConfig.setItemValue("name", "org.imixs.workflow.scheduler")
                            .setItemValue("_scheduler_class", "org.imixs.workflow.engine.WorkflowScheduler")
                            .setItemValue("_scheduler_definition", "hour=*").setItemValue("_scheduler_enabled", false);
                } else {
                    schedulerConfig = searchResult.get(0);

                    if (searchResult.size() > 1) {
                        logger.warning("│   ├──  ⚠️ More then one Workflow Scheduler configuration found!");
                    }
                }

                return schedulerConfig;

            } catch (RestAPIException | UnsupportedEncodingException e) {

                logController.severe("├── Failed to load scheduler configuration: " + e.getMessage());
            }
        }
        return null;
    }

}