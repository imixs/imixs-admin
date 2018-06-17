package org.imixs.workflow.mvc.controller;

import org.imixs.workflow.ItemCollection;

public class WorkitemEvent {

	public static final int WORKITEM_CREATED = 1;

	public static final int WORKITEM_CHANGED = 3;
	public static final int WORKITEM_BEFORE_PROCESS = 4;
	public static final int WORKITEM_AFTER_PROCESS = 5;
	public static final int WORKITEM_BEFORE_SAVE = 14;
	public static final int WORKITEM_AFTER_SAVE = 15;
	
	
	private int eventType;
	private ItemCollection workitem;

	public WorkitemEvent(ItemCollection workitem, int eventType) {
		this.eventType = eventType;
		this.workitem = workitem;
	}

	public int getEventType() {
		return eventType;
	}

	public ItemCollection getWorkitem() {
		return workitem;
	}

}
