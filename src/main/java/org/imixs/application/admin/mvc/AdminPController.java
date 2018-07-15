package org.imixs.application.admin.mvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.imixs.application.admin.ConnectionController;
import org.imixs.application.admin.DataController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ProcessingErrorException;

/**
 * The QueryController is used to search for workitems
 * 
 * @author rsoika
 *
 */
@Controller
@Path("/adminp")
@Named
public class AdminPController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AdminPController.class.getName());

	@Inject
	ConnectionController connectionController;

	@Inject
	DataController dataController;

	public AdminPController() {
		super();
	}

	@GET
	public String home() {
		findJobs();
		return "adminp.xhtml";
	}
	
	private void findJobs() {
		if (connectionController.getConfiguration() != null && !connectionController.getUrl().isEmpty()) {
			String uri = "documents/search/(type:\"adminp\")?pageSize=100&sortBy=$modified&sortReverse=true";
			List<ItemCollection> jobs = connectionController.getWorkflowCLient().getCustomResource(uri);
			dataController.setAdminPJobs(jobs);
		}
	}

	
	@POST
	@Path("/rebuildindex")
	public String rebuildIndex(InputStream requestBodyStream) {
		createJob(requestBodyStream);
		return "redirect:adminp/";
	}
	
	@POST
	@Path("/renameuser")
	public String renameUser(InputStream requestBodyStream) {
		createJob(requestBodyStream);
		return "redirect:adminp/";
	}
	
	@POST
	@Path("/upgrade")
	public String upgrade(InputStream requestBodyStream) {
		createJob(requestBodyStream);
		return "redirect:adminp/";
	}
	
	@POST
	@Path("/migration")
	public String migration(InputStream requestBodyStream) {
		createJob(requestBodyStream);
		return "redirect:adminp/";
	}

	

	@GET
	@Path("/action/delete/{uniqueid}")
	public String actionDeleteJob(@PathParam("uniqueid") String uniqueid) {

		logger.finest("......delete job: " + uniqueid);
		connectionController.getWorkflowCLient().deleteWorkitem(uniqueid);

		return "redirect:adminp/";
	}

	
	/**
	 * Creates a new job instance
	 * @param requestBodyStream
	 */
	private void createJob(InputStream requestBodyStream) {
		ItemCollection job = null;
		connectionController.setErrorMessage("");
		try {
			// start andminp job
			job = parseWorkitem(requestBodyStream);
		
			if (job.getItemValueInteger("numinterval")==0) {
				job.replaceItemValue("numinterval", 1);
			}
			connectionController.getWorkflowCLient().createAdminPJob(job);
			
		} catch (AccessDeniedException | ProcessingErrorException e) {
			connectionController.setErrorMessage("Unable to create job instance: " + e.getMessage());
			
		}
	}

	/**
	 * This method expects a form post. The method parses the input stream to
	 * extract the provides field/value pairs. NOTE: The method did not(!) assume
	 * that the put/post request contains a complete workItem. For this reason the
	 * method loads the existing instance of the corresponding workItem (identified
	 * by the $uniqueid) and adds the values provided by the put/post request into
	 * the existing instance.
	 * 
	 * The following kind of lines which can be included in the InputStream will be
	 * skipped
	 * 
	 * <code>
	 * 	------------------------------1a26f3661ff7
		Content-Disposition: form-data; name="query"
		Connection: keep-alive
		Content-Type: multipart/form-data; boundary=---------------------------195571638125373
		Content-Length: 5680
	
		-----------------------------195571638125373
	 * </code>
	 * 
	 * @param requestBodyStream
	 * @return a workitem
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final static ItemCollection parseWorkitem(InputStream requestBodyStream) {
		Vector<String> vMultiValueFieldNames = new Vector<String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(requestBodyStream));
		String inputLine;
		ItemCollection workitem = new ItemCollection();

		logger.fine("[WorkflowRestService] parseWorkitem....");

		try {
			while ((inputLine = in.readLine()) != null) {
				// System.out.println(inputLine);

				// split params separated by &
				StringTokenizer st = new StringTokenizer(inputLine, "&", false);
				while (st.hasMoreTokens()) {
					String fieldValue = st.nextToken();
					logger.finest("[WorkflowRestService] parse line:" + fieldValue + "");
					try {
						fieldValue = URLDecoder.decode(fieldValue, "UTF-8");

						if (!fieldValue.contains("=")) {
							logger.finest("[WorkflowRestService] line will be skipped");
							continue;
						}

						// get fieldname
						String fieldName = fieldValue.substring(0, fieldValue.indexOf('='));

						// if fieldName contains blank or : or --- we skipp the
						// line
						if (fieldName.contains(":") || fieldName.contains(" ") || fieldName.contains(";")) {
							logger.finest("[WorkflowRestService] line will be skipped");
							continue;
						}

						// test for value...
						if (fieldValue.indexOf('=') == fieldValue.length()) {
							// no value
							workitem.replaceItemValue(fieldName, "");
							logger.fine("[WorkflowRestService] no value for '" + fieldName + "'");
						} else {
							fieldValue = fieldValue.substring(fieldValue.indexOf('=') + 1);
							// test for a multiValue field - did we know
							// this
							// field....?
							fieldName = fieldName.toLowerCase();
							if (vMultiValueFieldNames.indexOf(fieldName) > -1) {

								List v = workitem.getItemValue(fieldName);
								v.add(fieldValue);
								logger.fine("[WorkflowRestService] multivalue for '" + fieldName + "' = '" + fieldValue
										+ "'");
								workitem.replaceItemValue(fieldName, v);
							} else {
								// first single value....
								logger.fine(
										"[WorkflowRestService] value for '" + fieldName + "' = '" + fieldValue + "'");
								workitem.replaceItemValue(fieldName, fieldValue);
								vMultiValueFieldNames.add(fieldName);
							}
						}

					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		} catch (IOException e1) {
			logger.severe("[WorkflowRestService] Unable to parse workitem data!");
			e1.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return workitem;
	}
}