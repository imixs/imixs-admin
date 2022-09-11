package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDocument;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The DocumentController shows a singel document selected form the search
 * result or other admin views.
 *
 * @author rsoika
 *
 */
@Named
@RequestScoped
public class DocumentController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(DocumentController.class.getName());

    private ItemCollection document;

    @Inject
    ConnectionController connectionController;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        String id = params.get("id");
        if (id != null && !id.isEmpty()) {
            load(id);
        }
    }

    public ItemCollection getDocument() {
        return document;
    }

    public void setDocument(ItemCollection document) {
        this.document = document;
    }

    /**
     * Returns the type of a single item
     *
     * @param name
     * @return
     */
    public String getItemType(String name) {
        List<?> valueList = document.getItemValue(name);

        if (valueList == null || valueList.size() == 0) {
            return "null";
        }

        Object o = valueList.get(0);

        return o.getClass().getSimpleName();

    }

    /**
     * Returns the values of a single item represented as a string
     *
     * @param name
     * @return
     */
    public List<String> getItemValues(String name) {
        List<String> result = new ArrayList<String>();
        List<?> valueList = document.getItemValue(name);

        if (valueList == null || valueList.size() == 0) {
            return result;
        }

        for (Object o : valueList) {
            result.add(o.toString());
        }
        return result;

    }

    /**
     * Computes the search result based on the current query data
     *
     * @return
     */
    public void load(String id) {

        logger.finest("...load document: " + id);
        WorkflowClient workflowClient = connectionController.getWorkflowClient();
        try {
            // load all items
            workflowClient.setItems(null);
            document = workflowClient.getDocument(id);
        } catch (RestAPIException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a document
     *
     * @return
     */
    public void delete(String id) {

        logger.info("...delete document: " + id);
        WorkflowClient workflowClient = connectionController.getWorkflowClient();
        try {
            // delete document
            workflowClient.deleteDocument(id);
        } catch (RestAPIException e) {
            e.printStackTrace();
        }
    }

}