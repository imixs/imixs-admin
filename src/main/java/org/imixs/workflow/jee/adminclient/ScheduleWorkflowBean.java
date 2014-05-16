package org.imixs.workflow.jee.adminclient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIParameter;
import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.ejb.WorkflowSchedulerRemote;

public class ScheduleWorkflowBean {
	// JNDI ejb name
	private String ejbScheduledWorkflowServiceName = "";
 
	WorkflowSchedulerRemote scheduledWorkflowService = null;
	//@EJB
	//WorkflowScheduler scheduledWorkflowService;
	boolean enabled;

	// Date startDate;
	Date stopDate;
	// String id = null;
	int hours = 24;
	int minutes;

	private ItemCollection timerDescription;

	/**
	 * init method. initialize default scheduledWorkflowManager
	 */
	public ScheduleWorkflowBean() {
		timerDescription = new ItemCollection();
	}

	/**
	 * check if a ScheduledWorkflowManager was loaded
	 * 
	 * @return
	 */
	public boolean isScheduledWorkflowServiceLoaded() {
		return scheduledWorkflowService != null;
	}

	/**
	 * returns the current ScheduledWorkflowManager JNDI Name
	 * 
	 * @return
	 */
	public String getEjbScheduledWorkflowServiceName() {
		return ejbScheduledWorkflowServiceName;
	}

	/**
	 * set the current scheduledWorkflowService JNDI name and try to load the
	 * EJB from the initialContext
	 * 
	 * @param ejbName
	 */
	public void setEjbScheduledWorkflowServiceName(String ejbName) {
		if (ejbName == null || "".equals(ejbName))
			return;
		try {
			this.ejbScheduledWorkflowServiceName = ejbName;

			InitialContext ic = new InitialContext();
			scheduledWorkflowService = (WorkflowSchedulerRemote) ic
					.lookup(ejbName);
		} catch (Exception e) {
			e.printStackTrace();
			scheduledWorkflowService = null;
		}

	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	/**
	 * this method test if a timerinstance with the current id is found and
	 * running
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		ItemCollection itemCol = null;
		String id = timerDescription.getItemValueString(
				"id");
		if (!"".equals(id)) {
			try {
				itemCol = scheduledWorkflowService.findTimerDescription(id);
				// compare id
				if (!id.equals(itemCol.getItemValueString("id")))
					itemCol = null;
			} catch (Exception e) {
				e.printStackTrace();
				itemCol = null;
			}

		}
		return (itemCol != null);
	}

	/**
	 * creates a new timer service
	 * 
	 * @param event
	 */
	public void doCreate(ActionEvent event) {
		// compute interval in miliseconds
		int interval = (minutes + (hours * 60)) * 60 * 1000;

		// Prepare Timer Object
		try {
			ItemCollection itmcol = new ItemCollection();
			itmcol.replaceItemValue("numInterval", interval);

			Calendar calNow = Calendar.getInstance();
			Date startDate = calNow.getTime();
			itmcol.replaceItemValue("datstart", startDate);

			// Stopdate auf 10 Jahre einstellen
			calNow = Calendar.getInstance();

			// calNow.add(Calendar.YEAR, 10);
			stopDate = calNow.getTime();
			itmcol.replaceItemValue("datstop", stopDate);

			timerDescription=itmcol;
			// doStartScheduledWorkflow(event);
		} catch (Exception e) {
			// no op;
			e.printStackTrace();
		}

	}

	/**
	 * This method starts the scheduled workflow Intervall is set to 10 min
	 * 
	 * @throws Exception
	 */
	/*
	 * public void doStartScheduledWorkflow(ActionEvent event) throws Exception
	 * { int minutes = 10; int hours = 0;
	 * 
	 * // String SCHEDULED_ID = "Das ist ein TEst";
	 * 
	 * // compute interval in miliseconds int interval = (minutes + (hours *
	 * 60)) * 60 * 1000; Calendar calNow = Calendar.getInstance(); Date
	 * startDate = calNow.getTime(); // Prepare Timer Object ItemCollection
	 * timerCol = new ItemCollection(); // timerCol.replaceItemValue("id", "");
	 * 
	 * timerCol.replaceItemValue("datstart", startDate);
	 * 
	 * // Stopdate auf 10 Jahre einstellen calNow = Calendar.getInstance();
	 * 
	 * calNow.add(Calendar.YEAR, 10); stopDate = calNow.getTime();
	 * timerCol.replaceItemValue("datstop", stopDate);
	 * 
	 * timerCol.replaceItemValue("numInterval", interval);
	 * 
	 * // scheduledWorkflowService.scheduleWorkflow(timerCol); // timerCol = //
	 * scheduledWorkflowService.findTimerDescription(SCHEDULED_ID);
	 * 
	 * timerDescription.setItemCollection(timerCol);
	 * 
	 * }
	 */

	public void doEnable(ActionEvent event) {
		// compute interval in miliseconds
		int interval = (getMinutes() + (getHours() * 60)) * 60 * 1000;

		// Prepare Timer Object
		try {
			timerDescription.replaceItemValue(
					"numInterval", interval);

			String id = timerDescription
					.getItemValueString("id");
			// Start Timer
			scheduledWorkflowService.scheduleWorkflow(timerDescription
					);

			// get Timer back from system
			timerDescription = scheduledWorkflowService
					.findTimerDescription(id);

			

		} catch (Exception e) {
			// no op;
		}

	}

	public void doDisable(ActionEvent event) {
		try {
			String id = timerDescription
					.getItemValueString("id");
			scheduledWorkflowService.cancelScheduleWorkflow(id);
			timerDescription=new ItemCollection();
			timerDescription.replaceItemValue("id", id);

		} catch (Exception e) {
			scheduledWorkflowService = null;
		}
	}

	public void doReschedule(ActionEvent event) {
		try {
			String id = timerDescription
					.getItemValueString("id");
			scheduledWorkflowService.cancelScheduleWorkflow(id);

			// compute interval in miliseconds
			int interval = (getMinutes() + (getHours() * 60)) * 60 * 1000;

			// Prepare Timer Object

			timerDescription.replaceItemValue(
					"numInterval", interval);

			// Start Timer
			scheduledWorkflowService.scheduleWorkflow(timerDescription);

			// get Timer back from system
			ItemCollection itemcol = scheduledWorkflowService
					.findTimerDescription(id);

			timerDescription=itemcol;

		} catch (Exception e) {
			// no op;
		}

	}

	/**
	 * This method loads a specific timer by its ID
	 * 
	 * @param id
	 */
	public void findTimerByID(String id) {

		try {
			ItemCollection itemcol = scheduledWorkflowService
					.findTimerDescription(id);
			if (itemcol == null) {
				itemcol = new ItemCollection();
				itemcol.replaceItemValue("id", id);
			}

			// Compute hours and minutes now
			int iIntervall = itemcol.getItemValueInteger("numinterval");

			// in sekunden
			iIntervall = iIntervall / 1000;
			// in miunten
			iIntervall = iIntervall / 60;

			hours = iIntervall / 60;

			minutes = iIntervall - (hours * 60);
			timerDescription=itemcol;

		} catch (Exception e) {
			scheduledWorkflowService = null;
		}
	}

	public void loadTimer(ActionEvent event) {
		// find timer ID fom action event
		List children = event.getComponent().getChildren();
		String sID = "";

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof UIParameter) {
				UIParameter currentParam = (UIParameter) children.get(i);
				if (currentParam.getName().equals("id")
						&& currentParam.getValue() != null)
					sID = currentParam.getValue().toString();
			}
		}
		findTimerByID(sID);
	}

	public ArrayList<ItemCollection> getTimers() throws Exception {

		ArrayList<ItemCollection> timerList = new ArrayList<ItemCollection>();

		Collection<ItemCollection> col = scheduledWorkflowService
				.findAllTimerDescriptions();

		for (ItemCollection adescription : col) {

			timerList.add(adescription);
		}
		return timerList;
	}

	public Map getItem() throws Exception {
		return timerDescription.getItem();
	}

	public Map getItemList() throws Exception {
		return timerDescription.getItemList();
	}

	public Map getItemListArray() throws Exception {
		return timerDescription.getItemListArray();
	}

}
