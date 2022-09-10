package org.imixs.application.admin;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import org.imixs.melman.WorkflowClient;

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
        int updates = 0;

        logController.reset();
        // try {
        WorkflowClient workflowClient = connectionController.getWorkflowClient();
        if (workflowClient != null) {
            // set items!

            logController.info("...admin-p started");

        }
//        } catch (RestAPIException e) {
//            logController.warning("Rest API Error: " + e.getMessage());
//
//        }
    }

}