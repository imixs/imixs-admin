package org.imixs.application.admin;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

/**
 * Rest API Endpoint to load a xml document from the remote instance This
 * endpoint is used for download links
 *
 */
@Path("/download")
public class DownloadResource {

    @Inject
    ConnectionController connectionController;

    @GET
    public Response downloadFile(@QueryParam("fileId") String fileId, @QueryParam("filename") String filename) {
        try {
            // Holen Sie sich das XML-Dokument
            XMLDocument xmlDocument = getXmlDocument(fileId);

            // Erstellen Sie einen StreamingOutput, um die Datei zu schreiben
            StreamingOutput streamingOutput = output -> {
                JAXBContext jaxbContext;
                try {
                    jaxbContext = JAXBContext.newInstance(XMLDocument.class);
                    Marshaller marshaller = jaxbContext.createMarshaller();
                    marshaller.marshal(xmlDocument, output);
                } catch (JAXBException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            };

            // Setzen Sie die HTTP-Header für den Download
            return Response.ok(streamingOutput)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"").type("application/xml")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Generieren der Datei: " + e.getMessage()).build();
        }
    }

    /**
     * This method loads a document by its UniqueID and initialzes a download
     * stream. Used in the search view and in reports.xhtml to download a Document
     * objects as an XML file. The filename is the name the download will be shown
     * in the browser.
     */
    public XMLDocument getXmlDocument(String id) {
        XMLDocument xmlDocument = null;
        try {
            // load document
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            ItemCollection downloadDocument = workflowClient.getDocument(id);
            xmlDocument = XMLDocumentAdapter.getDocument(downloadDocument);
        } catch (RestAPIException e) {
            e.printStackTrace();
        }
        return xmlDocument;
    }

}