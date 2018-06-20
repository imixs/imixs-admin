package org.imixs.application.admin.mvc;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.mvc.annotation.Controller;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.imixs.application.admin.ConnectionController;



/**
 * The Connect controller is used to establish a connectio to Imixs-Worklfow
 * remote interface.
 * 
 * @author rsoika
 *
 */
@Controller

@Path("/backup")
public class BackupController implements Serializable {

	private static final long serialVersionUID = 1L;
	private int status;

	private String query;
	private String path;

	private static Logger logger = Logger.getLogger(BackupController.class.getName());

	@Inject
	ConnectionController connectionController;

	public BackupController() {
		super();
	}

	public String getQuery() {
		if (query == null || query.isEmpty()) {
			query = "(type:\"workitem\")";
		}
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getPath() {
		if (path == null || path.isEmpty()) {
			// create default path from current time
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HHmm");
			path = "backup_" + df.format(new Date()) + ".xml";
		}
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getStatus() {
		return status;
	}

	@GET
	public String home() {
		return "backup.xhtml";
	}

	/**
	 * This method creates a backup or restore request based on the formParam
	 * 'action'
	 * 
	 * @param query
	 * @param path
	 * @return
	 */
	@POST
	public String backup(@FormParam("query") String query, @FormParam("path") String path,
			@FormParam("action") String action) {
		logger.info("backup=" + query + " path=" + path);

		setQuery(query);
		setPath(path);

		
		if ("backup".equals(action)) {
			String uri = connectionController.getWorkflowCLient().getBaseURI() + "documents/backup/" + query + "?filepath=" + path;
			// create put for backup ...
			Response response = connectionController.getWorkflowCLient().getClient().target(uri).request().put(null);
			status = response.getStatus();
		}

		if ("restore".equals(action)) {
			String uri = connectionController.getWorkflowCLient().getBaseURI() + "documents/restore?filepath=" + path;
			// create put for backup ...
			Response response = connectionController.getWorkflowCLient().getClient().target(uri).request().get();
			status = response.getStatus();
		}

		return "backup.xhtml";
	}

}