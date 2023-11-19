package org.imixs.application.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

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

    /**
     * This method loads a document by its UniqueID and initialzes a download
     * stream. Used in the search view and in reports.xhtml to download a Document
     * objects as an XML file. The filename is the name the download will be shown
     * in the browser.
     */
    public void downloadDocument(String id, String filename) {
        try {
            // load report
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            ItemCollection downloadDocument = workflowClient.getDocument(id);
            XMLDocument xmlDocument = XMLDocumentAdapter.getDocument(downloadDocument);

            // set Content-Type and Header for Download
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);

            // marshal xmlObject...
            JAXBContext jaxbContext = JAXBContext.newInstance(xmlDocument.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(xmlDocument, writer);
            // create stream object
            byte[] bytes = writer.toString().getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            inputStream.close();
            facesContext.responseComplete();
        } catch (IOException | JAXBException | RestAPIException e) {
            e.printStackTrace();
        }
    }

}