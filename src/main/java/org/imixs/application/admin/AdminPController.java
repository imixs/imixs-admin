package org.imixs.application.admin;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public static final String JOB_UPDATE_WORKITEMS = "UPDATE_WORKITEMS";

    // defaults for the update job - small blocks keep the index flush reliable
    private static final int UPDATE_WORKITEMS_BLOCK_SIZE = 30;
    private static final int UPDATE_WORKITEMS_INTERVAL = 1;
    private static final int DEFAULT_BLOCK_SIZE = 500;
    private static final int DEFAULT_INTERVAL = 60;

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
    private String query;
    private String itemName;
    private String itemType = "xs:string";
    // raw text of the values field - one value per line
    private String itemValues;
    private boolean append;
    // Integer instead of int so an empty input field is accepted
    private Integer eventId;
    List<ItemCollection> jobs = null;

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    @Inject
    DocumentController documentController;

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

    /**
     * Sets the job type and adjusts block size and interval to the defaults of the
     * selected job.
     */
    public void setJobType(String jobType) {
        boolean changed = (jobType != null && !jobType.equals(this.jobType));
        this.jobType = jobType;
        if (changed) {
            if (JOB_UPDATE_WORKITEMS.equals(jobType)) {
                blockSize = UPDATE_WORKITEMS_BLOCK_SIZE;
                interval = UPDATE_WORKITEMS_INTERVAL;
            } else {
                blockSize = DEFAULT_BLOCK_SIZE;
                interval = DEFAULT_INTERVAL;
            }
        }
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemValues() {
        return itemValues;
    }

    public void setItemValues(String itemValues) {
        this.itemValues = itemValues;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
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
                // the job handlers read 'numBlockSize' (item names are case insensitive)
                job.replaceItemValue("numblocksize", blockSize);

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
                if (JOB_UPDATE_WORKITEMS.equals(getJobType())) {
                    // the job would fail immediately without query and item name
                    if (isBlank(query) || isBlank(itemName)) {
                        logController.warning("Search Query and Item Name are required!");
                        return;
                    }
                    List<Object> values;
                    try {
                        values = parseValues(itemValues, itemType);
                    } catch (IllegalArgumentException e) {
                        logController.warning("Invalid value for type " + itemType + ": " + e.getMessage());
                        return;
                    }
                    job.replaceItemValue("query", query.trim());
                    job.replaceItemValue("itemname", itemName.trim());
                    // multi-value item - the XML transport keeps the value types
                    job.replaceItemValue("itemvalue", values);
                    job.replaceItemValue("append", append);
                    job.replaceItemValue("eventid", eventId == null ? 0 : eventId);
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
     * Converts the text of the values field into a typed value list. Each line is a
     * separate value. A line ending with '\n' is joined with the next line to build
     * a single value with multiple lines. Empty lines are ignored.
     *
     * @param text raw input
     * @param type xml schema type (xs:string, xs:int, ...)
     * @return list of typed values, empty if no values are given
     * @throws IllegalArgumentException if a value can not be converted
     */
    private static List<Object> parseValues(String text, String type) {
        List<Object> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }
        StringBuilder current = new StringBuilder();
        for (String line : text.split("\\r?\\n")) {
            if (line.endsWith("\\n")) {
                // continuation - keep the line break inside the value
                current.append(line, 0, line.length() - 2).append("\n");
                continue;
            }
            current.append(line);
            String value = current.toString();
            current.setLength(0);
            if (!value.trim().isEmpty()) {
                result.add(convertValue(value, type));
            }
        }
        // a trailing continuation without a following line
        if (current.length() > 0) {
            result.add(convertValue(current.toString(), type));
        }
        return result;
    }

    /**
     * Converts a single value into the given xml schema type.
     */
    private static Object convertValue(String value, String type) {
        String v = value.trim();
        try {
            switch (type == null ? "xs:string" : type) {
            case "xs:int":
                return Integer.valueOf(v);
            case "xs:long":
                return Long.valueOf(v);
            case "xs:float":
                return Float.valueOf(v);
            case "xs:double":
                return Double.valueOf(v);
            case "xs:boolean":
                if (!"true".equalsIgnoreCase(v) && !"false".equalsIgnoreCase(v)) {
                    throw new IllegalArgumentException("'" + v + "'");
                }
                return Boolean.valueOf(v);
            case "xs:dateTime":
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                format.setLenient(false);
                return format.parse(v);
            default:
                // strings are not trimmed
                return value;
            }
        } catch (NumberFormatException | ParseException e) {
            throw new IllegalArgumentException("'" + v + "'", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Reset job list
     */
    public void reset() {
        logController.reset();
        jobs = null;
    }

    public void deleteJob(String id) {
        documentController.delete(id);
        reset();
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