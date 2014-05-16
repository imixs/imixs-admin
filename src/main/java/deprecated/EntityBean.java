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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.imixs.workflow.ItemCollection;

/**
 * This Class defines a Bean to be used as a ManagedBean in a JavaServer Faces
 * Application. The Bean can be used to bind the current selected Entity to an jsf page
 * This backing bean will be initialized by the   
 * org.imixs.workflow.jee.adminclient.EntityServiceFacade.
 * 
 * The bean supports a map attribute "entity". See method 'getEntity()'
 * To bind a Value to a input or output field use ValueBinding like this: 
 * 
 * <h:outputText value="#{entityBean.entity[attribute_name][0]}" />
 * 
 * where attrubuite_name is an existing item in the current Entity 
 * 
 * @author Ralph Soika
 * @version 1.0
 * @see deprecated.EntityServiceFacade
 */

public class EntityBean { 
	
	private ItemCollection entity = null;
	
	private HashMap indexImage=null;
	private List indexList=null;

	String error_Message;

	public EntityBean() {

	}

	/**
	 * returns all Items for the teamItemCollection
	 * 
	 * @return
	 */
	public ItemCollection getEntity() throws Exception {
		if (entity == null)
			entity=new ItemCollection();
		return entity;
	}
	/**
	 * updates all attributes by the supported map into the teamItemCollection
	 * 
	 * @param aentity
	 */
	public void setEntity(ItemCollection aentity) {
		try {
			if (aentity != null)
				entity = aentity;
			else
				entity = new ItemCollection();
			
				
			createImageIndex(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setIndexList( List aIndexList) {
		indexList=aIndexList;
	}
	
	
	private void createImageIndex(ItemCollection itemcol) {
		indexImage=new HashMap();
		
		// create a image for each index
		Iterator iter=indexList.iterator();
		while (iter.hasNext()) {
			Vector v =(Vector) iter.next();
			String sName=(String)v.elementAt(0);
			Integer iType=(Integer)v.elementAt(1);
			
			
			indexImage.put(sName,"layout/img/index_typ_"+iType+".gif");
			
		}
		
	}
	
	/**
	 * returns all Items for the teamItemCollection
	 * 
	 * @return
	 */
	public Map getIndexImage() throws Exception {
		if (indexImage == null)
			indexImage=new HashMap();
		return indexImage;
	}
	
	/**
	 * Returns a list of Item Objects related to the corresponding MapEntries.
	 * 
	 * @return List
	 */
	public List getAttributNames() {
		ArrayList<String> names = new ArrayList<String>();
		Map item=entity.getAllItems();
		if (item != null) {
			Iterator iter = item.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry mapEntry = (Map.Entry) iter.next();
				names.add(mapEntry.getKey().toString());
			}
		}
		
		//sort by name
        Comparator<String> comparator = 
			new Comparator<String>() {
        		public int compare(final String o1,
        				final String o2) {
		    	return o1.compareToIgnoreCase(o2);
		    }
		};
		
		Collections.sort(names, comparator);

		return names;
	}

	

	
}
