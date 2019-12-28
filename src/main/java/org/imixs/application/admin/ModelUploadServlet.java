package org.imixs.application.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.melman.ModelClient;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.FileData;

/**
 * The ModelUploadServlet is a Multipart-Servlet 3.0. It is used by the
 * Imixs-Admin client to upload multiple model files to the workflow service
 * endpoint.
 * <p>
 * Each model file is send to the Rest API einpoint /model/bpmn to store the
 * model in the corresponding workflow instance.
 * 
 * @see org.imixs.application.admin.AdminRestService
 * @author rsoika
 */
@WebServlet(urlPatterns = { "/modelupload/*" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
public class ModelUploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String REQUEST_METHOD_POST = "POST";
	private static final String CONTENT_TYPE_MULTIPART = "multipart/";
	private static final String CONTENT_DISPOSITION = "content-disposition";
	private static final String CONTENT_DISPOSITION_FILENAME = "filename";

	private static Logger logger = Logger.getLogger(ModelUploadServlet.class.getName());

	@Inject
	RestClientHandler restClientHandler;

	/**
	 * Upload files to stored in the current user session
	 */
	@SuppressWarnings("unused")
	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse response)
			throws ServletException, IOException {
		if (isPostFileUploadRequest(httpRequest)) {

			WorkflowClient workflowClient = restClientHandler.createWorkflowClient(httpRequest);

			WebTarget target;
			try {

				ModelClient modelClient = restClientHandler.createModelClient(httpRequest);
				if (modelClient != null) {

					logger.finest("......add files...");
					List<FileData> fileDataList = getFilesFromRequest(httpRequest);

					if (fileDataList != null) {

						for (FileData file : fileDataList) {
							byte[] data = file.getContent();
							String fileName = file.getName();
							target = workflowClient.getWebTarget("model/bpmn/" + fileName);
							InputStream inputStream = new ByteArrayInputStream(data);
							Response modelapiResponse = target.request(MediaType.APPLICATION_XML)
									.post(Entity.xml(inputStream));

							// workflow versions before 5.1.6 do not allow to define a model file name.
							// in this case we receive a 404. As an alternative we post the model without
							// the filename which was the default behavior before 5.1.6
							int status = modelapiResponse.getStatus();
							if (status == Response.Status.NOT_FOUND.getStatusCode()) {
								logger.info("...redirecting model upload /model/bpmn ");
								target = workflowClient.getWebTarget("model/bpmn");
								inputStream = new ByteArrayInputStream(data);
								modelapiResponse = target.request(MediaType.APPLICATION_XML)
										.post(Entity.xml(inputStream));
							}
						}

					}

				}

			} catch (RestAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * checks if the httpRequest is a fileupload
	 * 
	 * @param httpRequest
	 * @return
	 */
	private boolean isPostFileUploadRequest(HttpServletRequest httpRequest) {
		String sContentType = httpRequest.getContentType();
		logger.finest("......contentType=" + sContentType);

		return (REQUEST_METHOD_POST.equalsIgnoreCase(httpRequest.getMethod()) && httpRequest.getContentType() != null
				&& sContentType.toLowerCase().startsWith(CONTENT_TYPE_MULTIPART));
	}

	/**
	 * test and extracts the filename of a http request part. The method returns
	 * null if the part dose not contain a file
	 * 
	 * @param part
	 * @return - filename or null if not a file content
	 */
	private String getFilename(Part part) {
		for (String cd : part.getHeader(CONTENT_DISPOSITION).split(";")) {
			if (cd.trim().startsWith(CONTENT_DISPOSITION_FILENAME)) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	/**
	 * This method converts mulitple files from the httpRequest into a list of
	 * FileData objects.
	 * 
	 * @param httpRequest
	 * @return list of FileData objects
	 */
	private List<FileData> getFilesFromRequest(HttpServletRequest httpRequest) {
		logger.finest("......Looping parts");

		List<FileData> fileDataList = new ArrayList<FileData>();
		try {
			for (Part p : httpRequest.getParts()) {
				byte[] b = new byte[(int) p.getSize()];
				p.getInputStream().read(b);
				p.getInputStream().close();
				// params.put(p.getName(), new String[] { new String(b) });

				// test if part contains a file
				String fileName = getFilename(p);
				if (fileName != null) {

					/*
					 * issue #106
					 * 
					 * https://developer.jboss.org/message/941661#941661
					 * 
					 * Here we test of the encoding and try to convert to utf-8.
					 */
					byte fileNameISOBytes[] = fileName.getBytes("iso-8859-1");
					String fileNameUTF8 = new String(fileNameISOBytes, "UTF-8");
					if (fileName.length() != fileNameUTF8.length()) {
						// convert to utf-8
						logger.finest("......filename seems to be ISO-8859-1 encoded");
						fileName = new String(fileName.getBytes("iso-8859-1"), "utf-8");
					}

					// extract the file content...
					FileData fileData = null;
					logger.finest("......filename : " + fileName + ", contentType " + p.getContentType());
					fileData = new FileData(fileName, b, p.getContentType(), null);
					fileDataList.add(fileData);

				}
			}

		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		} catch (ServletException ex) {
			logger.log(Level.SEVERE, null, ex);
		}

		return fileDataList;
	}

}
