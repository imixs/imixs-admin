package org.imixs.application.admin;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.ModelClient;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

/**
 * The ModelController loads and udpate the model list
 * <p>
 *
 * @see ModelUploadServlet
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class ModelController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ModelController.class.getName());

    List<ItemCollection> models = null;

    @Inject
    ConnectionController connectionController;

    @Inject
    SearchController searchController;

    private List<Part> files;

    public List<Part> getFiles() {
        return files;
    }

    public void setFiles(List<Part> files) {
        this.files = files;
    }

    /**
     * This method extracts all uploaded .bpmn files and uploades them to the model
     * service.
     *
     * @throws IOException
     */
    // @Deprecated
    // public void uploadModel() throws IOException {
    // if (files != null) {
    // try {
    // logger.info(" uploading " + files.size() + " files");
    // for (Part file : files) {

    // logger.info("name: " + file.getSubmittedFileName());
    // logger.info("type: " + file.getContentType());
    // logger.info("size: " + file.getSize());
    // InputStream content = file.getInputStream();
    // byte[] targetArray = new byte[content.available()];
    // content.read(targetArray);
    // // post model - /bpmn/{filename
    // BPMNModel model = new BPMNModel();
    // model.setRawData(targetArray);
    // ModelClient modelClient = connectionController.getModelClient();
    // modelClient.postModel(model);
    // }
    // } catch (RestAPIException e) {
    // logger.severe("Failed to read BPMN files: " + e.getMessage());
    // e.printStackTrace();
    // }
    // // reset current model list
    // reset();
    // }
    // }

    public void uploadModel() throws ModelException {
        if (files != null) {
            logger.info("├── uploading " + files.size() + " files");
            for (Part file : files) {
                try {
                    logger.info("│   ├── Processing file: " + file.getSubmittedFileName());

                    ModelClient modelClient = connectionController.getModelClient();
                    modelClient.postModel(file.getInputStream());

                } catch (RestAPIException | IOException e) {
                    throw new ModelException(ModelException.INVALID_MODEL,
                            "Unable to read model file: " + file.getSubmittedFileName(), e);
                }
            }
            // Nach erfolgreicher Verarbeitung die Model-Liste aktualisieren
            reset();
        }
    }

    /**
     * Deletes a model
     */
    public void deleteModel(String modelVersion) {
        try {
            logger.info(" delete model " + modelVersion);

            ModelClient modelClient = connectionController.getModelClient();
            modelClient.deleteModel(modelVersion);

        } catch (RestAPIException e) {
            logger.severe("Failed to delete BPMN model: " + e.getMessage());
            e.printStackTrace();
        }
        // reset current model list
        reset();
    }

    /**
     * Reset job list
     */
    public void reset() {
        models = null;
    }

    /**
     * Returns the current job list
     *
     * @return
     */
    public List<ItemCollection> getModels() {
        if (models == null) {
            logger.info("compute model list...");
            models = new ArrayList<ItemCollection>();
            try {
                WorkflowClient workflowClient = connectionController.getWorkflowClient();
                if (workflowClient != null) {
                    String query = "SELECT document FROM Document AS document WHERE document.type='model'";
                    try {
                        query = URLEncoder.encode(query, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    XMLDataCollection result = workflowClient.getCustomResourceXML("documents/jpql/" + query);
                    models = XMLDataCollectionAdapter.putDataCollection(result);
                }
            } catch (RestAPIException e) {
                logger.warning("Rest API Error: " + e.getMessage());

            }

            // sort model by name
            Collections.sort(models, new ItemCollectionComparator("txtname", true));
        }
        return models;
    }

}