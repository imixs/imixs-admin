/*******************************************************************************
 *  Imixs IX Workflow Technology
 *  Copyright (C) 2001, 2008 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *******************************************************************************/
package deprecated;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.ejb.EntityServiceRemote;
import org.imixs.workflow.jee.jpa.EntityIndex;

/**
 * This Class defines a BackingBean which acts as a SessionFacade for the
 * org.imixs.workflow.jee.ejb.EntityPersistenceManager Session EJB.
 * 
 * This Backing Bean interacts with the EntityBean backing bean to grant access
 * to the current selected Entity.
 * 
 * The ManagedBean should be declared in faces-config like this: <code>
 * <managed-bean>
 *      <managed-bean-name>entityServiceFacade</managed-bean-name> 
 *      <managed-bean-class> org.imixs.workflow.jee.adminclient.EntityServiceFacade
 *      </managed-bean-class> <managed-bean-scope>session</managed-bean-scope>
 * </managed-bean>
 * </code>
 * 
 * the assumes that a local instance of org.imixs.workflow.jee.ejb.EntityService
 * is deployed locally. You can bind the Bean to any other remote interface of
 * an EntityService using the 'ejbName' attribute.
 * 
 * @author rsoika
 * @version 1.2
 * @see deprecated.EntityBean
 */

public class EntityServiceFacade {

	private final String DEPRECATED_NO_VERSION = "DEPRECATED-NO-VERSION";
	private String indexName;

	private int indexType;

	private ArrayList indexList = new ArrayList();

	private List<ItemCollection> entities = null;
	private ArrayList<Map> models = null;

	private String query = "SELECT entity FROM Entity entity \nORDER BY entity.created DESC";

	private String filename = "/home/ix-jee-export"; // export- import
	private String loginfo = ""; // export- import log

	// JNDI ejb name
	private String ejbEntityServiceName = "";

	int row = 0, count = 30;

	private boolean endOfList = false;
	private HtmlDataTable dataTable;

	private EntityBean entityBean = null;

	EntityServiceRemote entityService = null;

	// @EJB
	// EntityService entityService;

	String errorMessage = "";

	String currentModelVersion;

	// bulk update
	private String updateField;
	private String updateValue;
	private String updateFieldType;

	public EntityServiceFacade() {
		entityBean = (EntityBean) FacesContext
				.getCurrentInstance()
				.getApplication()
				.getELResolver()
				.getValue(FacesContext.getCurrentInstance().getELContext(),
						null, "entityBean");
	}

	public String getLoginfo() {
		return loginfo;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setErrorMessage(String error_Message) {
		this.errorMessage = error_Message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setRow(int startpos) {
		this.row = startpos;
	}

	public int getRow() {
		return row;
	}

	public String getUpdateField() {
		return updateField;
	}

	public void setUpdateField(String updateField) {
		this.updateField = updateField;
	}

	public String getUpdateFieldType() {
		return updateFieldType;
	}

	public void setUpdateFieldType(String updateFieldType) {
		this.updateFieldType = updateFieldType;
	}

	public String getUpdateValue() {
		return updateValue;
	}

	public void setUpdateValue(String updateValue) {
		this.updateValue = updateValue;
	}

	/**
	 * this method removes the current selected worktiem from a view
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void doDelete(ActionEvent event) throws Exception {
		ItemCollection currentSelection = null;
		// suche selektierte Zeile....
		UIComponent component = event.getComponent();
		for (UIComponent parent = component.getParent(); parent != null; parent = parent
				.getParent()) {
			if (!(parent instanceof UIData))
				continue;

			// Zeile gefunden
			currentSelection = (ItemCollection) ((UIData) parent).getRowData();

			entityService.remove(currentSelection);

			doRefresh(event);
		}
	}

	/**
	 * refreshes the current workitem list. so the list will be loaded again.
	 * but start pos will not be changed!
	 */
	public void doRefresh(ActionEvent event) {
		entities = null;
	}

	/**
	 * resets the current project list and projectMB
	 * 
	 * @return
	 */
	public void doReset(ActionEvent event) {
		entities = null;
		row = 0;
	}

	/**
	 * This Method Selects the current model
	 * 
	 * @return
	 * @throws Exception
	 */
	public void doSelectModel(ActionEvent event) throws Exception {
		Map currentSelection = null;
		// ItemCollectionAdapter currentSelection = null;
		// seach selected row...
		UIComponent component = event.getComponent();

		for (UIComponent parent = component.getParent(); parent != null; parent = parent
				.getParent()) {
			if (!(parent instanceof UIData))
				continue;
			// get current project from row
			currentSelection = (Map) ((UIData) parent).getRowData();
			break;
		}

		currentModelVersion = currentSelection.get("$modelversion").toString();
		System.out.println("SELECTED MODEL: " + currentModelVersion);
	}

	/**
	 * This Method Selects the current project and refreshes the Worklist Bean
	 * so wokitems of these project will be displayed after show_worklist
	 * 
	 * Furthermore the method call loadProcessList to support a List of
	 * StartProcess ItemCollections. Forms and Views can use the
	 * getProcessList() method to show a list of StartProcesses
	 * 
	 * @return
	 * @throws Exception
	 */
	public void doDeleteModel(ActionEvent event) throws Exception {

		System.out.println("DELETING MODEL: " + currentModelVersion);
		String sQuery;
		/*
		 * To different methods to support older versions
		 */
		if (DEPRECATED_NO_VERSION.equals(currentModelVersion)) {
			sQuery = "SELECT process FROM Entity AS process "
					+ "	 JOIN process.textItems as t"
					+ "	 WHERE t.itemName = 'type' AND t.itemValue IN('ProcessEntity', 'ActivityEntity', 'WorkflowEnvironmentEntity')";
		} else {
			sQuery = "SELECT process FROM Entity AS process "
					+ "	 JOIN process.textItems as t"
					+ "	 JOIN process.textItems as v"
					+ "	 WHERE t.itemName = 'type' AND t.itemValue IN('ProcessEntity', 'ActivityEntity', 'WorkflowEnvironmentEntity')"
					+ " 	 AND v.itemName = '$modelversion' AND v.itemValue = '"
					+ currentModelVersion + "'";
		}

		Collection<ItemCollection> col = entityService.findAllEntities(sQuery,
				0, -1);

		for (ItemCollection aworkitem : col) {
			/*
			 * To different methods to support older versions
			 */
			if (DEPRECATED_NO_VERSION.equals(currentModelVersion)) {
				if (!aworkitem.hasItem("$modelversion"))
					entityService.remove(aworkitem);
			} else {
				entityService.remove(aworkitem);
			}
		}
		models = null;
	}

	/**
	 * This method resets simply the result set
	 * 
	 * @return List
	 */
	public void doSearch(ActionEvent e) {
		// reset result se
		entities = null;
	}

	/**
	 * loads the entity list based on the current query
	 */
	private void loadEntities() {
		entities = new ArrayList<ItemCollection>();
		// clear error Message
		this.setErrorMessage("");
		if (entityService != null) {
			if (query != null && !"".equals(query)) {
				System.out.println("AdminClient Load Entities: " + query);
				try {
					Collection<ItemCollection> col = entityService
							.findAllEntities(query, row, count);

					endOfList = col.size() < count;
					for (ItemCollection aworkitem : col) {
						entities.add(aworkitem);
					}
				} catch (Exception eqle) {
					this.setErrorMessage(eqle.getLocalizedMessage());
				}

			}
		}
	}

	/**
	 * adds a new index to the EntityPersistencManager
	 * 
	 * @return
	 */
	public String doSaveIndex() {
		try {
			entityService.addIndex(indexName, indexType);
			return "indexliste";
		} catch (Exception e) {
			return "failure";

		}
	}

	/**
	 * This action method deletes an existing index.
	 */
	public void doDeleteIndex(ActionEvent event) {
		try {
			// Parameter raussuchen
			List children = event.getComponent().getChildren();
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i) instanceof UIParameter) {
					UIParameter currentParam = (UIParameter) children.get(i);
					if (currentParam.getName().equals("name")
							&& currentParam.getValue() != null)
						indexName = currentParam.getValue().toString();
				}
			}
			entityService.removeIndex(indexName);
		} catch (Exception e) {
			System.out.println("m2: Index delete error: " + e);
		}
	}

	/**
	 * this method is called by datatables to select an workitem
	 * 
	 * @return
	 */
	public void doSelect(ActionEvent event) {
		ItemCollection currentSelection = null;
		// suche selektierte Zeile....
		UIComponent component = event.getComponent();
		for (UIComponent parent = component.getParent(); parent != null; parent = parent
				.getParent()) {
			if (!(parent instanceof UIData))
				continue;

			// Zeile gefunden
			currentSelection = (ItemCollection) ((UIData) parent).getRowData();
			entityBean.setEntity(currentSelection);
			break;

		}
	}

	/**
	 * This method starts a new EQL query for all existing ModelVersions and
	 * stores the results into the models object.
	 * 
	 * @return List
	 * @throws Exception
	 */
	public void doQueryModelVersions(ActionEvent e) throws Exception {
		models = new ArrayList<Map>();
		if (entityService != null) {

			String modelQuery = "SELECT process FROM Entity AS process"
					+ " JOIN process.textItems as t"
					+ " JOIN process.textItems as n"
					+ " WHERE t.itemName = 'type' AND t.itemValue = 'WorkflowEnvironmentEntity'"
					+ " AND n.itemName = 'txtname' AND n.itemValue = 'environment.profile'";

			Collection<ItemCollection> col = entityService.findAllEntities(
					modelQuery, 0, -1);

			for (ItemCollection aworkitem : col) {
				// ad entity creation and modified date
				Map map = aworkitem.getAllItems();
				map.put("entity_id", aworkitem.getItemValueString("$uniqueid"));
				map.put("entity_created",
						aworkitem.getItemValueDate("$created"));

				String sModel = DEPRECATED_NO_VERSION;
				if (aworkitem.hasItem("$modelversion"))
					sModel = aworkitem.getItemValueString("$modelversion");
				map.put("$modelversion", sModel);

				map.put("entity_modified",
						aworkitem.getItemValueDate("$modified"));
				models.add(map);
			}

		}

	}

	public ArrayList<Map> getModels() {
		if (models == null)
			try {
				this.doQueryModelVersions(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return models;
	}

	/***************************************************************************
	 * Navigation
	 */

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}

	public List<ItemCollection> getEntities() {
		if (entities == null)
			this.loadEntities();
		return entities;
	}

	/***************************************************************************
	 * INDEX Management
	 **************************************************************************/

	/**
	 * getter method for name
	 * 
	 * @return
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * setter method for name
	 * 
	 * @param map
	 */
	public void setIndexName(String aname) {
		indexName = aname;
	}

	public int getIndexType() {
		return indexType;
	}

	public void setIndexType(int type) {
		this.indexType = type;
	}

	public List getIndexList() {
		indexList = new ArrayList();

		/*
		if (entityService != null) {
			Collection col = entityService.getIndices();
			Iterator iter = col.iterator();

			while (iter.hasNext()) {
				EntityIndex aindex = (EntityIndex) iter.next();

				Vector v = new Vector();
				v.add(aindex.getName());
				v.add(Integer.valueOf(aindex.getTyp()));
				indexList.add(v);
			}
		}
*/
		return indexList;
	}

	public boolean isEndOfList() {
		return endOfList;
	}

	public void loadNext(ActionEvent e) {
		row = row + count;
		doSearch(e);

	}

	public void loadPrev(ActionEvent e) {
		row = row - count;
		if (row < 0)
			row = 0;
		doSearch(e);
	}

	/**
	 * This method starts a new EQL query and exports the results into a export
	 * file
	 * 
	 * @return List
	 */
	public String exportWorkItemList() throws Exception {
		boolean hasMoreData = true;
		int JUNK_SIZE = 100;
		long totalcount = 0;
		int startpos = 0;

		if (entityService != null) {
			int icount = 0;
			if (filename == null || "".equals(filename))
				return "";
			if (query == null | "".equals(query))
				return "";

			try {
				FileOutputStream fos = new FileOutputStream(filename);
				ObjectOutputStream out = new ObjectOutputStream(fos);
				while (hasMoreData) {
					// read a junk....
					Collection<ItemCollection> col = entityService
							.findAllEntities(query, startpos, JUNK_SIZE);

					if (col.size() < JUNK_SIZE)
						hasMoreData = false;
					startpos = startpos + col.size();
					totalcount = totalcount + col.size();
					System.out.println("ExportWorktiems - read " + totalcount
							+ " entries....");

					for (ItemCollection aworkitem : col) {
						// Jetzt die Daten exporieren....
						Map hmap = aworkitem.getAllItems();
						// out.writeObject(itemCol);
						out.writeObject(hmap);
						icount++;
					}
				}
				out.close();

				loginfo = "Export successfull! " + icount
						+ " Entities exported into " + filename + ".";

			} catch (IOException ex) {
				loginfo = "Export Error : " + ex.getMessage();
				ex.printStackTrace();
			}
		}
		return "success";

	}

	/**
	 * This method starts a file import and loads the existing workitems. Each
	 * imported Entity will be restored with its own $UnqiueID and $creation
	 * date. If an entity with the spcific $uniqueid allready exists the entity
	 * will be updated.
	 * 
	 * 
	 * 
	 * @return "success"
	 */
	public String importWorkItemList() throws Exception {
		int JUNK_SIZE = 100;
		long totalcount = 0;
		int icount = 0;
		if (entityService != null) {
			if (filename == null || "".equals(filename))
				return "";
			if (query == null | "".equals(query))
				return "";

			try {
				FileInputStream fis = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fis);

				while (true) {
					try {
						// read one more object
						Map hmap = (Map) in.readObject();
						ItemCollection itemCol = new ItemCollection(hmap);
						// try to persist itemcollection
						if (itemCol != null) {
							// Add original $unqiueID to restore $uniqueIDRef
							String aImportUniqueID = itemCol
									.getItemValueString("$Uniqueid");

							/*
							 * Now migrate old Reader and Write Access Fields
							 * from ix-workflow
							 * 
							 * namworkflowReadAccess => $ReadAccess
							 * namWrokflowWriteAccess => $WriteAccess
							 */
							if (!itemCol.hasItem("$WriteAccess"))
								itemCol.replaceItemValue(
										"$WriteAccess",
										itemCol.getItemValue("namWorkflowWriteAccess"));
							if (!itemCol.hasItem("$ReadAccess"))
								itemCol.replaceItemValue("$ReadAccess", itemCol
										.getItemValue("namWorkflowReadAccess"));

							// now save imported data
							entityService.save(itemCol);
							totalcount++;
							icount++;
							if (icount >= JUNK_SIZE) {
								icount = 0;
								System.out.println("ImportWorktiems - read "
										+ totalcount + " entries....");

							}

						} else
							break;
					} catch (java.io.EOFException eofe) {
						break;
					}
				}
				loginfo = "Import successfull! " + totalcount
						+ " Entities imported from " + filename + ".";

				in.close();

			} catch (IOException ex) {
				loginfo = "Export Error : " + ex.getMessage();

				ex.printStackTrace();
			}
		}
		return "success";

	}

	/**
	 * This method starts a new EQL query and removes the results
	 * 
	 * @return List
	 */
	public void removeWorkItemList(ActionEvent event) throws Exception {
		if (entityService != null) {

			if (query == null | "".equals(query))
				return;
			entities = null;
			Collection<ItemCollection> col = entityService.findAllEntities(
					query, row, count);
			for (ItemCollection aworkitem : col) {
				entityService.remove(aworkitem);
			}
		}
		// return "search";

	}

	/**
	 * This method starts a new EQL query and update the results with the
	 * updateValue in the field updateField
	 * 
	 * @return List
	 */
	public void bulkUpdateWorkItemList(ActionEvent event) throws Exception {
		if (entityService != null) {

			if (query == null | "".equals(query))
				return;

			if (updateField == null | "".equals(updateField))
				return;

			if (updateValue == null)
				return;

			Vector vNewValueList = new Vector();
			String[] tokens = updateValue.split("\n");
			for (int i = 0; i < tokens.length; i++) {

				String sValue = tokens[i].trim();

				// determine updateFieldType
				// 1 = TYP_INT - defines an Integer Item
				// 2 = TYP_DOUBLE - defines a Double Item
				// 3 = TYP_CALENDAR - defines a Date Item (dd/MM/yyyy:hh:mm:ss -
				// e.g: 25.12.2011:12:30:00)
				if ("1".equals(updateFieldType)) {
					vNewValueList.addElement(new Integer(sValue));
				} else if ("2".equals(updateFieldType)) {
					vNewValueList.addElement(new Double(sValue));
				} else if ("3".equals(updateFieldType)) {
					// TimeZone cetTime = TimeZone.getTimeZone("CET");
					DateFormat cetFormat = new SimpleDateFormat(
							"dd/MM/yyyy:hh:mm:ss", Locale.ENGLISH);
					// cetFormat.setTimeZone(cetTime);
					vNewValueList.addElement(cetFormat.parse(sValue));
				} else {
					// default text
					vNewValueList.addElement(sValue);
				}
			}

			entities = null;
			Collection<ItemCollection> col = entityService.findAllEntities(
					query, row, count);
			for (ItemCollection aworkitem : col) {

				// update spcific field value
				aworkitem.replaceItemValue(updateField, vNewValueList);

				entityService.save(aworkitem);
			}
		}
		// return "search";

	}

	public String getEjbEntityPersistenceManagerName() {
		return ejbEntityServiceName;
	}

	public void setEjbEntityPersistenceManagerName(String ejbModuleName)
			throws Exception {

		this.ejbEntityServiceName = ejbModuleName;
		if (ejbModuleName == null || "".equals(ejbModuleName))
			return;
		try {
			// building the global jndi name....
			String globalJNDIName = "java:global/"
					+ ejbEntityServiceName
					+ "/EntityService!org.imixs.workflow.jee.ejb.EntityServiceRemote";
			InitialContext ic = new InitialContext();
			entityService = (EntityServiceRemote) ic.lookup(globalJNDIName);

			// now set indexlist for entitybean
			entityBean.setIndexList(getIndexList());

		} catch (Exception e) {
			e.printStackTrace();
			entityService = null;
		}
	}

	public boolean isEntityPersistenceManagerLoaded() {
		return entityService != null;
	}

}
