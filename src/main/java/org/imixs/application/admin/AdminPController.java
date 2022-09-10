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
 * The UpdateController stores the data for bulk updates
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class AdminPController implements Serializable {

    public static final String JOB_RENAME_USER = "RENAME_USER";
    public static final String JOB_REBUILD_INDEX = "JOB_REBUILD_INDEX";
    public static final String JOB_UPGRADE = "UPGRADE";

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(AdminPController.class.getName());

    private String filter;
    private String jobType;
    private int interval = 60;
    private int blockSize = 500;
    private Date dateFrom;
    private Date dateTo;
    private String renameUserFrom;
    private String renameUserTo;
    private boolean renameFullReplace;
    List<ItemCollection> jobs = null;

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    @Inject
    LogController logController;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getRenameUserFrom() {
        return renameUserFrom;
    }

    public void setRenameUserFrom(String renameUserFrom) {
        this.renameUserFrom = renameUserFrom;
    }

    public String getRenameUserTo() {
        return renameUserTo;
    }

    public void setRenameUserTo(String renameUserTo) {
        this.renameUserTo = renameUserTo;
    }

    public boolean isRenameFullReplace() {
        return renameFullReplace;
    }

    public void setRenameFullReplace(boolean renameFullReplace) {
        this.renameFullReplace = renameFullReplace;
    }

    /**
     * This method starts a bulk update
     */
    public void executeJob() {
        logger.info("...starting Admin-P");

        reset();
        try {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {

                ItemCollection job = new ItemCollection();
                job.replaceItemValue("job", jobType);
                job.replaceItemValue("numinterval", interval); // seconds
                job.replaceItemValue("blocksize", blockSize);

                if (JOB_REBUILD_INDEX.equals(getJobType())) {
                }

                if (JOB_RENAME_USER.equals(getJobType())) {
                    job.replaceItemValue("typelist", filter);
                    job.replaceItemValue("datfrom", dateFrom);
                    job.replaceItemValue("datto", dateTo);

                    job.replaceItemValue("namfrom", renameUserFrom);
                    job.replaceItemValue("namto", renameUserTo);
                    job.replaceItemValue("keyreplace", renameFullReplace);

                }
                if (JOB_UPGRADE.equals(getJobType())) {
                    job.replaceItemValue("typelist", filter);
                    job.replaceItemValue("datfrom", dateFrom);
                    job.replaceItemValue("datto", dateTo);
                }

                // post Admin-P job
                workflowClient.createAdminPJob(job);
                logController.info("...admin-p job '" + getJobType() + "' started");
                logController.info("...block-size=" + blockSize);
                logController.info("...interval=" + interval + "sec");

            }
        } catch (RestAPIException e) {
            logController.warning("Rest API Error: " + e.getMessage());

        }
    }

    /**
     * Reset job list
     */
    public void reset() {
        logController.reset();
        jobs = null;
    }

    /**
     * Returns the current job list
     *
     * @return
     */
    public List<ItemCollection> getJobs() {
        if (jobs == null) {
            logger.info("compute job list...");
            jobs = new ArrayList<ItemCollection>();
            try {
                WorkflowClient workflowClient = connectionController.getWorkflowClient();
                if (workflowClient != null) {
                    XMLDataCollection jobList = workflowClient.getCustomResourceXML("/adminp/jobs");
                    jobs = XMLDataCollectionAdapter.putDataCollection(jobList);
                }
            } catch (RestAPIException e) {
                logController.warning("Rest API Error: " + e.getMessage());

            }
        }

        return jobs;
    }

}