package org.imixs.application.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.WorkflowKernel;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The ReportController loads and udpate the report list
 * <p>
 *
 * @see ModelUploadServlet
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class ReportController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ReportController.class.getName());

    List<ItemCollection> reports = null;
    ItemCollection report = null;
    List<ItemCollection> attributeList = null;

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    /**
     * Creates a new empty report object
     */
    public void createReport() {
        report = new ItemCollection();
        report.setItemValue(WorkflowKernel.UNIQUEID, WorkflowKernel.generateUniqueID());
        report.setType("ReportEntity");
        logger.fine("new Report Object created");
    }

    public ItemCollection getReport() {
        return report;
    }

    public void submitReport() {
        if (report != null) {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            try {
                workflowClient.saveDocument(report);
                logger.info("Report '" + report.getUniqueID() + "' updated");
            } catch (RestAPIException e) {
                e.printStackTrace();
            }
        }
        reset();
    }

    /**
     * Load a Report Defintion by ID
     */
    public void loadReport(String id) {
        try {
            logger.info(" load report " + id);
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            report = workflowClient.getDocument(id);
            readAttributes();
        } catch (RestAPIException e) {
            logger.severe("Failed to load report definition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the attribute list for the current report.
     * Item, Label, Convert, Aggregate
     * 
     * @return attribute list or null if no report is selected.
     */
    private void readAttributes() {

        // test if a itemList is provided or defined in the current report...
        List<List<String>> attributes = (List<List<String>>) report.getItemValue("attributes");
        attributeList = new ArrayList<ItemCollection>();
        for (List<String> attribute : attributes) {
            ItemCollection attr = new ItemCollection();
            attr.setItemValue("name", attribute.get(0));
            attr.setItemValue("label", attribute.get(1));
            attr.setItemValue("convert", attribute.get(2));
            attr.setItemValue("aggregate", attribute.get(3));

            attributeList.add(attr);
        }

    }

    /**
     * Returns the attribute list of the current report
     * 
     * @return
     */
    public List<ItemCollection> getAttributeList() {
        return attributeList;
    }

    /**
     * Adds a new attriubet to the attributelist of the current report
     */
    public void addAttribute() {
        attributeList.add(new ItemCollection());
    }

    /**
     * Delete a Report Definition
     */
    public void deleteReport(String id) {
        try {
            logger.info(" delete report " + id);
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            workflowClient.deleteDocument(id);

        } catch (RestAPIException e) {
            logger.severe("Failed to delete report definition: " + e.getMessage());
            e.printStackTrace();
        }
        // reset current report list
        reset();
    }

    /**
     * Reset Report list
     */
    private void reset() {
        reports = null;
    }

    /**
     * Returns the current job list
     *
     * @return
     */
    public List<ItemCollection> getReports() {
        if (reports == null) {
            logger.fine("compute report list...");
            try {
                WorkflowClient workflowClient = connectionController.getWorkflowClient();
                reports = workflowClient.getCustomResource("/report/definitions");
            } catch (RestAPIException e) {
                logger.warning("Rest API Error: " + e.getMessage());

            }
            // sort result by name
            Collections.sort(reports, new ItemCollectionComparator("txtname", true));
        }
        return reports;
    }

}