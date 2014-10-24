package org.imixs.workflow.jee.adminclient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.jee.ejb.EntityService;
import org.imixs.workflow.jee.ejb.EntityServiceRemote;

@ManagedBean
@SessionScoped
public class EntityController {
	private EntityServiceRemote entityService = null;
	private String module = null;
	private String globalJNDIName = null;
	private List<String> moduleList = null;
	private List<ItemCollection> entities = null;
	private String indexName = null;
	private int indexType;
	private List<String[]> indexList = null;

	private String filename = null;

	private int row = 0, count = 10;
	private String updateField = null;
	private String updateValue = null;
	private String updateFieldType = null;
	private boolean endOfList;
	private String loginfo = "";

	private String query = "SELECT entity FROM Entity entity \nORDER BY entity.modified DESC";
	private ItemCollection entity = null;

	public final String COOKIE_JNDI_NAMES = "imixs.workflow.adminclient.jndinames";
	private final String DEPRECATED_NO_VERSION = "DEPRECATED-NO-VERSION";

	private final static Logger logger = Logger
			.getLogger(EntityController.class.getName());

	public String getGlobalJNDIName() {
		return globalJNDIName;
	}

	public List<String> getModuleList() {
		return moduleList;
	}

	public String getModule() {
		if (module == null)
			module = "";
		return module;
	}

	/**
	 * This method lookups the entity Service based on the global jndi name
	 * 
	 * @param module
	 */
	public void setModule(String module) {

		this.module = module;
		if (module == null || "".equals(module))
			return;
		try {
			// building the global jndi name....

			// if name starts with java:global/
			if (!module.startsWith("java:global/")) {
				module = "java:global/" + module;
			}

			globalJNDIName = module;

			// test if name ends with EntityService
			if (!globalJNDIName
					.endsWith("/EntityService!org.imixs.workflow.jee.ejb.EntityServiceRemote")) {
				globalJNDIName += "/EntityService!org.imixs.workflow.jee.ejb.EntityServiceRemote";
			}

			logger.info("[Imixs-Admin] globalJNDIName=" + globalJNDIName);
			InitialContext ic = new InitialContext();
			entityService = (EntityServiceRemote) ic.lookup(globalJNDIName);

			indexList = null;
			setModuleCookie(module);
			logger.info("[Imixs-Admin] connection ok!");

		} catch (Exception e) {
			e.printStackTrace();
			entityService = null;
			globalJNDIName = "";

			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Invalid JNDI Name -  Lookup failed!", e
									.getLocalizedMessage()));
		}
	}

	public boolean isServiceLoaded() {
		return entityService != null;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getLoginfo() {
		return loginfo;
	}

	public void setLoginfo(String loginfo) {
		this.loginfo = loginfo;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getUpdateField() {
		return updateField;
	}

	public void setUpdateField(String updateField) {
		this.updateField = updateField;
	}

	public String getUpdateValue() {
		return updateValue;
	}

	public void setUpdateValue(String updateVlaue) {
		this.updateValue = updateVlaue;
	}

	public String getUpdateFieldType() {
		return updateFieldType;
	}

	public void setUpdateFieldType(String updateFieldType) {
		this.updateFieldType = updateFieldType;
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

	public List<ItemCollection> getEntities() {
		if (entities == null)
			doSearch(null);
		return entities;
	}

	/**
	 * returns the last selected item
	 * 
	 * @return
	 */
	public ItemCollection getEntity() {
		if (entity == null)
			entity = new ItemCollection();
		return entity;
	}

	/**
	 * Returns a list of all ItemNames for the current entity
	 * 
	 * @return List
	 */
	public List getEntityProperties() {
		ArrayList<String> names = new ArrayList<String>();
		Map item = entity.getAllItems();
		if (item != null) {
			Iterator iter = item.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry mapEntry = (Map.Entry) iter.next();
				names.add(mapEntry.getKey().toString());
			}
		}

		// sort by name
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(final String o1, final String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		};

		Collections.sort(names, comparator);

		return names;
	}

	/**
	 * This method returns a list of available models. The model descriptions
	 * are provided in Map Objects. The method is called by the page
	 * /modellist.xhtml
	 * 
	 * @return
	 */
	public ArrayList<Map> getModels() {
		ArrayList models = new ArrayList<Map>();
		if (entityService != null) {

			// select all Models versions - identified by the
			// environment.profile entity included in each model
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
				map.put("$uniqueid", aworkitem.getItemValueString("$uniqueid"));
				map.put("$created", aworkitem.getItemValueDate("$created"));

				String sModel = DEPRECATED_NO_VERSION;
				if (aworkitem.hasItem("$modelversion"))
					sModel = aworkitem.getItemValueString("$modelversion");
				map.put("$modelversion", sModel);

				map.put("$modified", aworkitem.getItemValueDate("$modified"));
				models.add(map);
			}

		}

		return models;
	}

	/**
	 * returns the entityService index list
	 * 
	 * @return
	 */
	public List<String[]> getIndexList() {
		if (indexList != null)
			return indexList;
		indexList = new ArrayList<String[]>();

		if (entityService != null) {
			Map<String, Integer> map = entityService.getIndices();

			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();

				String sValue = "";
				if (value == EntityService.TYP_TEXT)
					sValue = "TEXT";
				else if (value == EntityService.TYP_CALENDAR)
					sValue = "CALENDAR";
				else if (value == EntityService.TYP_DOUBLE)
					sValue = "DOUBLE";
				else if (value == EntityService.TYP_INT)
					sValue = "INT";
				else
					sValue = "UNDEFINED";

				String[] aIndex = { key, value.toString(), sValue };
				indexList.add(aIndex);
			}
		}

		return indexList;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public int getIndexType() {
		return indexType;
	}

	public void setIndexType(int indexType) {
		this.indexType = indexType;
	}

	/**
	 * This method try to guess the property type of a property in the current
	 * selected Entity
	 * 
	 * @param property
	 * @return
	 */
	public String propertyType(String property) {
		if (entity == null)
			return "null";

		List values = entity.getItemValue(property);
		if (values.size() > 0) {
			Object o = values.get(0);
			if (o != null)
				return o.getClass().getSimpleName();
		}
		// no value....
		return "";
	}

	/**
	 * This method returns the EntityIndex image url of a property in the
	 * current selected Entity
	 * 
	 * @param property
	 * @return
	 */
	public String propertyImage(String property) {
		if (entity == null)
			return "";

		List<String[]> list = getIndexList();
		for (String[] aIndex : list) {
			if (aIndex[0].equals(property.toLowerCase()))
				return "layout/img/index_typ_" + aIndex[1] + ".gif";
		}

		return "";
	}

	/**
	 * This method searches the search query
	 * 
	 * @return List
	 */
	public void doSearch(ActionEvent e) {

		entities = new ArrayList<ItemCollection>();
		// clear error Message
		// this.setErrorMessage("");
		if (entityService != null) {
			if (query != null && !"".equals(query)) {
				logger.info("[ImixsAdminClient] Load Entities: " + query);
				try {
					Collection<ItemCollection> col = entityService
							.findAllEntities(query, row, count);

					endOfList = col.size() < count;
					for (ItemCollection aworkitem : col) {
						entities.add(aworkitem);
					}
				} catch (Exception eqle) {
					eqle.printStackTrace();
				}

			}
		}
	}

	/**
	 * Removes the current selected entity from a view
	 * 
	 * @param event
	 * @throws AccessDeniedException
	 * @throws Exception
	 */
	public String deleteEntityAction(ItemCollection currentSelection)
			throws AccessDeniedException {
		entityService.remove(currentSelection);
		// reset search result
		doResetSearchResult(null);
		return null;

	}

	public String selectEntityAction(ItemCollection currentSelection) {
		this.entity = currentSelection;

		return "entity";
	}

	/**
	 * Removes an EntityIndex
	 * 
	 * @param event
	 * @throws AccessDeniedException
	 * @throws Exception
	 */
	public String deleteEntityIndexAction(String aindexName)
			throws AccessDeniedException {
		entityService.removeIndex(aindexName);
		indexList = null;
		// reset search result
		doResetSearchResult(null);
		return null;
	}

	/**
	 * Removes the current selected workflow model
	 * 
	 * @param event
	 * @throws AccessDeniedException
	 * 
	 */
	public String deleteModelAction(String currentModelVersion)
			throws AccessDeniedException {

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

		return null;

	}

	/**
	 * refreshes the current result list. so the list will be loaded again. but
	 * start pos will not be changed!
	 * 
	 * @throws AccessDeniedException
	 */
	public void doAddEntityIndex(ActionEvent event)
			throws AccessDeniedException {
		entityService.addIndex(indexName, indexType);
		indexList = null;
		// reset search result
		doResetSearchResult(null);

	}

	/**
	 * refreshes the current result list. so the list will be loaded again. but
	 * start pos will not be changed!
	 */
	public void doResetSearchResult(ActionEvent event) {
		entities = null;
	}

	/**
	 * This method starts a new EQL query and update the results with the
	 * updateValue in the field updateField
	 * 
	 * @return List
	 */
	public void doBulkUpdate(ActionEvent event) throws Exception {
		if (entityService != null) {

			if (query == null | "".equals(query))
				return;

			if (updateField == null | "".equals(updateField))
				return;

			if (updateValue == null)
				return;

			logger.info("[ImixsAdminClient] Starting bulk update....");
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

			logger.info("[ImixsAdminClient]   update Field: " + updateField);
			logger.info("[ImixsAdminClient]   new Value: " + vNewValueList);

			entities = null;
			Collection<ItemCollection> col = entityService.findAllEntities(
					query, row, count);

			logger.info("[ImixsAdminClient]   updating " + col.size()
					+ " entries....");
			for (ItemCollection aworkitem : col) {

				// update spcific field value
				aworkitem.replaceItemValue(updateField, vNewValueList);

				entityService.save(aworkitem);
			}

			logger.info("[ImixsAdminClient]   bulk update successfull!");
		}
		// return "search";

	}

	/**
	 * This method starts a new EQL query and removes the results
	 * 
	 * @return List
	 */
	public void doBulkDelete(ActionEvent event) throws Exception {
		if (entityService != null) {

			if (query == null | "".equals(query))
				return;

			logger.info("[ImixsAdminClient] Starting bulk delete....");
			entities = null;
			Collection<ItemCollection> col = entityService.findAllEntities(
					query, row, count);
			logger.info("[ImixsAdminClient]   deleteing " + col.size()
					+ " entries....");
			for (ItemCollection aworkitem : col) {
				entityService.remove(aworkitem);
			}
			logger.info("[ImixsAdminClient]   bulk delete successfull!");
		}
		// return "search";

	}

	/**
	 * This method starts a new EQL query and exports the results into a export
	 * file
	 * 
	 * @return List
	 */
	public String exportEntities() throws Exception {
		boolean hasMoreData = true;
		int JUNK_SIZE = 100;
		long totalcount = 0;
		int startpos = 0;

		if (entityService != null) {
			int icount = 0;
			if (filename == null || "".equals(filename))
				return null;
			if (query == null | "".equals(query))
				return null;

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
					logger.info("[ImixsAdminClient] ExportWorktiems - read "
							+ totalcount + " entries....");

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
		return null;

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
	public String importEntities() {
		int JUNK_SIZE = 100;
		long totalcount = 0;
		long errorCount = 0;
		int icount = 0;
		loginfo = "";

		if (entityService != null) {
			if (filename == null || "".equals(filename))
				return null;
			if (query == null | "".equals(query))
				return null;

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

							// remove the $version property!
							itemCol.removeItem("$Version");

							// now save imported data
							entityService.save(itemCol);
							totalcount++;
							icount++;
							if (icount >= JUNK_SIZE) {
								icount = 0;
								logger.info("[ImixsAdminClient] ImportWorktiems - read "
										+ totalcount + " entries....");

							}

						} else
							break;
					} catch (java.io.EOFException eofe) {
						break;
					} catch (ClassNotFoundException e) {
						errorCount++;
						logger.warning("[ImixsAdminClient] error importing workitem at position "
								+ (totalcount + errorCount)
								+ " Error: "
								+ e.getMessage());
					} catch (AccessDeniedException e) {
						errorCount++;
						logger.warning("[ImixsAdminClient] error importing workitem at position "
								+ (totalcount + errorCount)
								+ " Error: "
								+ e.getMessage());
					}
				}
				loginfo += "Import successfull! " + totalcount
						+ " Entities imported. " + errorCount
						+ " Errors.  Import FileName:" + filename;

				logger.info(loginfo);
				doResetSearchResult(null);

				in.close();

			} catch (IOException ex) {
				loginfo = "Export Error : " + ex.getMessage();

				logger.severe(loginfo);
				ex.printStackTrace();
			}
		}
		return null;

	}

	/**
	 * after initialize try to read cookie for known jndi names....
	 */
	@SuppressWarnings("unused")
	@PostConstruct
	private void readJNDINamesFromCookie() {
		// initialize empty module list
		moduleList = new Vector<String>();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String cookieName = null;
		Cookie cookie[] = ((HttpServletRequest) facesContext
				.getExternalContext().getRequest()).getCookies();
		if (cookie != null && cookie.length > 0) {
			for (int i = 0; i < cookie.length; i++) {
				cookieName = cookie[i].getName();
				if (cookieName.equals(COOKIE_JNDI_NAMES)) {
					String cookievalue = cookie[i].getValue();
					String[] cookieList = Pattern.compile("~").split(
							cookievalue);

					Collections.addAll(moduleList, cookieList);
					// moduleList = Arrays.asList(cookieList);
					break;
				}

			}
		}

	}

	/**
	 * Adds a new module name to the module cookie
	 * 
	 * @param locale
	 */
	private void setModuleCookie(String newName) {

		if (moduleList.indexOf(newName) > -1)
			return;

		moduleList.add(newName);
		// update cookie
		HttpServletResponse response = (HttpServletResponse) FacesContext
				.getCurrentInstance().getExternalContext().getResponse();
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();

		// create string....
		String sCookieValue = "";
		for (String aModule : moduleList) {
			sCookieValue += aModule + "~";
		}

		Cookie cookieModules = new Cookie(COOKIE_JNDI_NAMES, sCookieValue);
		cookieModules.setPath(request.getContextPath());

		// 30 days
		cookieModules.setMaxAge(2592000);
		response.addCookie(cookieModules);

	}

}
