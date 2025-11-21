package org.imixs.application.admin;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

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
        schedulerConfig = load();

    }

    public ItemCollection getSchedulerConfig() {
        return schedulerConfig;
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
    public void start() {
        logger.info("├── Starting Workflow Scheduler...");

        logController.reset();
        try {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {
                schedulerConfig.setItemValue("_scheduler_enabled", true);
                XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(schedulerConfig);
                XMLDataCollection result = workflowClient.postXMLDocument("/scheduler/", xmlWorkitem);
                schedulerConfig = XMLDataCollectionAdapter.putDataCollection(result).get(0);
            }
        } catch (RestAPIException e) {
            logController.warning("Rest API Error: " + e.getMessage());
        }
    }

    /**
     * This method starts a bulk update
     */
    public void stop() {
        logger.info("├── Starting Workflow Scheduler...");

        logController.reset();
        try {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {
                schedulerConfig.setItemValue("_scheduler_enabled", false);
                XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(schedulerConfig);
                XMLDataCollection result = workflowClient.postXMLDocument("/scheduler/", xmlWorkitem);
                schedulerConfig = XMLDataCollectionAdapter.putDataCollection(result).get(0);
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
    public ItemCollection load() {
        schedulerConfig = null;
        logger.info("├── Load Workflow Scheduler configuration");
        WorkflowClient workflowClient = connectionController.getWorkflowClient();
        if (workflowClient != null) {

            try {

                List<ItemCollection> result = workflowClient
                        .getCustomResource("scheduler/org.imixs.workflow.scheduler");
                // a valid result object contains the item lucence.fulltextFieldList'
                if (result == null || result.size() == 0) {
                    logController.info("│   ├──  Create new Workflow Scheduler default configuration");
                    createDefaultConfiguration();
                } else {
                    schedulerConfig = result.get(0);
                }
            } catch (RestAPIException e) {
                logController.info("├── Failed to load scheduler configuration - create default configuration");
                createDefaultConfiguration();

            }
        }

        return schedulerConfig;
    }

    /**
     * Helper method to create an empty default config
     */
    private void createDefaultConfiguration() {
        schedulerConfig = new ItemCollection();
        schedulerConfig.setType("scheduler");
        schedulerConfig.setItemValue("name", "org.imixs.workflow.scheduler")
                .setItemValue("_scheduler_class", "org.imixs.workflow.engine.WorkflowScheduler")
                .setItemValue("_scheduler_definition", "hour=*").setItemValue("_scheduler_enabled", false);

    }

    /**
     *
     * converts time (in milliseconds) to human-readable format "<dd:>hh:mm:ss"
     *
     * @return
     */
    public String millisToShortDHMS(int duration) {
        boolean debug = logger.isLoggable(Level.FINE);
        if (debug) {
            logger.log(Level.FINEST, "......confert ms {0}", duration);
        }
        String res = "";
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        if (days == 0) {
            res = String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
        } else {
            res = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
        }
        return res;

    }

}