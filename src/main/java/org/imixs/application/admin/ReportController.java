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
                updateAttributeList();
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
     * Returns the attribute list for the current report. Item, Label, Convert,
     * Aggregate.
     *
     * Each element has an attriubte 'pos' indicating the positon starting by 1
     *
     * @return attribute list or null if no report is selected.
     */
    private void readAttributes() {

        // test if a itemList is provided or defined in the current report...
        List<List<String>> attributes = (List<List<String>>) report.getItemValue("attributes");
        attributeList = new ArrayList<ItemCollection>();
        for (List<String> attribute : attributes) {
            ItemCollection attr = new ItemCollection();
            attr.setItemValue("item", attribute.get(0));
            attr.setItemValue("label", attribute.get(1));
            attr.setItemValue("convert", attribute.get(2));
            attr.setItemValue("format", attribute.get(3));
            attr.setItemValue("aggregate", attribute.get(4));
            attr.setItemValue("pos", attributeList.size() + 1);
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
        // updateAttributeList();
        ItemCollection attr = new ItemCollection();
        attr.setItemValue("pos", attributeList.size() + 1);
        attributeList.add(attr);
        refreshAttributeList();
    }

    /**
     * This methdo updates the item 'attriburtes' of the current report
     */
    private void updateAttributeList() {
        List<List<String>> attributes = new ArrayList<List<String>>();
        if (attributeList != null) {
            for (ItemCollection attr : attributeList) {
                List<String> attribute = new ArrayList<>();
                attribute.add(attr.getItemValueString("item"));
                attribute.add(attr.getItemValueString("label"));
                attribute.add(attr.getItemValueString("convert"));
                attribute.add(attr.getItemValueString("format"));
                attribute.add(attr.getItemValueString("aggregate"));
                attributes.add(attribute);
            }
        }
        report.setItemValue("attributes", attributes);
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
     * move the attribute at pos up in the attribute list
     *
     * @param pos
     */
    public void moveAttributeDown(int pos) {
        ItemCollection attribute = attributeList.remove(pos - 1); // Remove the element
        attributeList.add(pos, attribute); // Add the element at the new position

        refreshAttributeList();
    }

    /**
     * Helper method to update the attribues and refresh the attriubteList
     */
    private void refreshAttributeList() {
        updateAttributeList();
        readAttributes();
    }

    /**
     * move the attribute at pos down in the attribute list
     *
     * @param pos
     */
    public void moveAttributeUp(int pos) {
        ItemCollection attribute = attributeList.remove(pos - 1); // Remove the element
        attributeList.add(pos - 2, attribute); // Add the element at the new position
        refreshAttributeList();
    }

    /**
     * delete the attribute at pos from the attribute list
     *
     * @param pos
     */
    public void deleteAttribute(int pos) {
        attributeList.remove(pos - 1); // Remove the element
        refreshAttributeList();
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