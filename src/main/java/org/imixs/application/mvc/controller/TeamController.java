package org.imixs.application.mvc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.Path;

import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.mvc.controller.DocumentController;
import org.imixs.workflow.mvc.controller.WorkitemEvent;

/**
 * Controller to manage active imixs-workflow instances.
 * 
 * @author rsoika
 *
 */
@Controller
@Path("teams")
@Named
public class TeamController extends DocumentController {

	private static Logger logger = Logger.getLogger(TeamController.class.getName());

	/**
	 * Initialize TeamController
	 */
	public TeamController() {
		super();
		setDocumentType("team");
		setDocumentView("team.xhtml");
		setDocumentsView("teams.xhtml");
	}

	/**
	 * WorkItemEvent listener to convert team item
	 * 
	 * @param workitemEvent
	 * @throws AccessDeniedException
	 */
	@SuppressWarnings("unchecked")
	public void onWorkflowEvent(@Observes WorkitemEvent workitemEvent) throws AccessDeniedException {
		if (workitemEvent == null)
			return;

		// skip if not a workItem...
		if (workitemEvent.getWorkitem() != null
				&& !workitemEvent.getWorkitem().getItemValueString("type").startsWith("team"))
			return;

		int eventType = workitemEvent.getEventType();

		// convert list to string with newlines
		if (WorkitemEvent.WORKITEM_CHANGED == eventType) {
			List<String> members = workitemEvent.getWorkitem().getItemValue("members");
			String result = "";
			for (String member : members) {
				result += member + "\n";
			}
			workitemEvent.getWorkitem().replaceItemValue("members", result);
		}

		// convert string with newlines to list
		if (WorkitemEvent.WORKITEM_BEFORE_SAVE == eventType) {
			String value = workitemEvent.getWorkitem().getItemValueString("members");
			workitemEvent.getWorkitem().replaceItemValue("members", Arrays.asList(value.split("\\r?\\n")));
		}

	}

}