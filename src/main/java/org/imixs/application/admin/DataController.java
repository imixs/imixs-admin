package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;

/**
 * Session Scoped CID Bean to hold current adminp-job data.
 * 
 * @author rsoika
 *
 */
@Named
@SessionScoped
public class DataController implements Serializable {

	private static Logger logger = Logger.getLogger(DataController.class.getName());

	private static final long serialVersionUID = 1L;

	public static int DEFAULT_PAGE_SIZE = 30;

	// search
	String query;
	int pageIndex;
	int pageSize;
	String sortBy;
	String sortOrder;

	// bulk update
	String fieldName;
	String fieldType;
	String fieldValues;
	int workflowEvent;
	boolean appendValues;

	// backup
	String backupPath;

	ItemCollection document;
	List<ItemCollection> documents;
	List<ItemCollection> adminPJobs;

	public DataController() {
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

	public String getBackupPath() {
		if (backupPath == null || backupPath.isEmpty()) {
			// create default path from current time
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HHmm");
			backupPath = "backup_" + df.format(new Date()) + ".xml";
		}
		return backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getSortBy() {
		if (sortBy == null) {
			sortBy = "$modified";
		}
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	
	

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(String fieldValues) {
		this.fieldValues = fieldValues;
	}

	public int getWorkflowEvent() {
		return workflowEvent;
	}

	public void setWorkflowEvent(int workflowEvent) {
		this.workflowEvent = workflowEvent;
	}

	public boolean isAppendValues() {
		return appendValues;
	}

	public void setAppendValues(boolean appendValues) {
		this.appendValues = appendValues;
	}

	public ItemCollection getDocument() {
		return document;
	}

	public void setDocument(ItemCollection document) {
		this.document = document;
	}

	public List<ItemCollection> getDocuments() {
		return documents;
	}

	public void setDocuments(List<ItemCollection> documents) {
		this.documents = documents;
	}

	public List<ItemCollection> getAdminPJobs() {
		return adminPJobs;
	}

	public void setAdminPJobs(List<ItemCollection> adminPJobs) {
		this.adminPJobs = adminPJobs;
	}

	/**
	 * Returns a formated html string of a item value
	 * 
	 * @param itemname
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getFormatedItemValue(String itemname) {
		String result = "";
		if (document != null) {
			List itemvalues = document.getItemValue(itemname);
			for (Object o : itemvalues) {
				result += "<strong>" + o.getClass().getSimpleName() + ":</strong> " + o;
				result += "<br />";
			}
		}
		return result;
	}
	
	
	/**
	 * This method URL-encodes the query string so it can be used by the rest api
	 *  
	 * @return
	 */
	public String getEncodedQuery() {
		String escapedQuery=getQuery();
		try {
			escapedQuery=URLEncoder.encode(escapedQuery,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.warning("encoding of query string failed!");
		}
		return escapedQuery;
	}
}