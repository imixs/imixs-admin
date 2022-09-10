package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
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
public class UpdateController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(UpdateController.class.getName());

    private String fieldName;// default query
    private String fieldType;
    private int workflowEvent;
    private String values;
    private boolean appendValues;

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    @Inject
    LogController logController;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public int getWorkflowEvent() {
        return workflowEvent;
    }

    public void setWorkflowEvent(int workflowEvent) {
        this.workflowEvent = workflowEvent;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public boolean isAppendValues() {
        return appendValues;
    }

    public void setAppendValues(boolean appendValues) {
        this.appendValues = appendValues;
    }

    /**
     * This method starts a bulk update
     */
    public void bulkUpdate() {
        logger.info("...starting Bulk Update");
        int updates = 0;

        logController.reset();
        try {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {
                // set items!
                workflowClient.setItems("$uniqueid,$taskid,$modelversion," + getFieldName());
                searchController.reset();
                List<ItemCollection> documents = searchController.getSearchResult();
                logController.info("..." + documents.size() + " documents selected for bulk update...");

                // first convert the newValue in a list of objects based on the selected
                // fieldType...
                String[] rawItems = values.split("\\r?\\n");
                // now convert to selected type
                List<Object> typedItems = new ArrayList<Object>();
                for (String rawValue : rawItems) {
                    if ("xs:int".equals(fieldType)) {
                        typedItems.add(Integer.parseInt(rawValue));
                    } else if ("xs:long".equals(fieldType)) {
                        typedItems.add(Long.parseLong(rawValue));
                    } else if ("xs:float".equals(fieldType)) {
                        typedItems.add(Float.parseFloat(rawValue));
                    } else if ("xs:double".equals(fieldType)) {
                        typedItems.add(Double.parseDouble(rawValue));
                    } else if ("xs:dateTime".equals(fieldType)) {
                        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-mm-dd");
                        try {
                            Date date = dt1.parse(rawValue);
                            typedItems.add(date);
                        } catch (ParseException e) {
                            logController.warning("...unable to convert '" + rawValue + "' into date object!");
                            typedItems.add(rawValue);
                        }
                    } else if ("xs:boolean".equals(fieldType)) {
                        typedItems.add(Boolean.parseBoolean(rawValue));
                    } else {
                        typedItems.add(rawValue);
                    }
                }

                // iterate over all documents....
                for (ItemCollection document : documents) {
                    // update documetn item
                    if (appendValues) {
                        // append
                        document.appendItemValue(fieldName, typedItems);
                    } else {
                        // replace
                        document.replaceItemValue(fieldName, typedItems);
                    }

                    // Save or Process workitem?
                    if (workflowEvent <= 0) {
                        // save workitem
                        workflowClient.saveDocument(document);
                        updates++;
                    } else {
                        // process workitem
                        document.setEventID(workflowEvent);
                        workflowClient.processWorkitem(document);
                        updates++;
                    }
                }
                logController.info("...update finished: " + updates + " udpates.");

            }
        } catch (RestAPIException e) {
            logController.warning("Rest API Error: " + e.getMessage());

        }
    }

}