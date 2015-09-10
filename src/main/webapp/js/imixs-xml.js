/*******************************************************************************
 * imixs-xml.js Copyright (C) 2015, http://www.imixs.org
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You can receive a copy of the GNU General Public License at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Project:  http://www.imixs.org
 * 
 * Contributors: Ralph Soika - Software Developer
 ******************************************************************************/

/**
 * This library provides methods to convert a XML Entity Stream provided by the
 * Imixs-Workflow Rest API into a JavaScript model object. It implements the
 * representation of an ItemCollection similar to the
 * org.imixs.workflow.ItemCollection class
 * 
 * Version 1.0.1
 */

IMIXS.namespace("org.imixs.xml");
IMIXS.org.imixs.xml = (function() {

	if (!IMIXS.org.imixs.core) {
		console.error("ERROR - missing dependency: imixs-core.js");
	}

	// private properties
	var _not_used,

	/**
	 * converts a Imixs XML result set into an array of entities. This method
	 * guarantees that an array of entities is returned even if the result colletion
	 * size is 0 or 1
	 */
	xml2collection = function(xml) {
		var json = xml2json(xml)
		if (!$.isArray(json.collection.entity))
			json.collection.entity = jQuery.makeArray(json.collection.entity);

		return json.collection.entity;
	}
	
	/**
	 * converts a Imixs XML result of an entity into an item array  
	 */
	xml2entity = function(xml) {
		var json = xml2json(xml)   
		if (!$.isArray(json.entity.item))
			json.entity.item = jQuery.makeArray(json.entity.item);
		return json.entity.item;
	}

	/**
	 * converts a XML result set form the Imixs Rest Service API into a JSON
	 * object. Based on the idears from David Walsh
	 * (http://davidwalsh.name/convert-xml-json)
	 * 
	 * 
	 * </code>
	 */
			xml2json = function(xml) {
				// Create the return object
				var obj = {};

				if (xml.nodeType == 1) { // element
					// do attributes
					if (xml.attributes.length > 0) {
						for (var j = 0; j < xml.attributes.length; j++) {
							var attribute = xml.attributes.item(j);
							obj[attribute.nodeName] = attribute.nodeValue;
						}
					}
				} else if (xml.nodeType == 3) { // text
					obj = xml.nodeValue;
				}

				// process item? in this case we construct the properties name,
				// value and
				// type...
				if (xml.nodeName == "item") {
					if (xml.hasChildNodes()) {
						for (var i = 0; i < xml.childNodes.length; i++) {
							var item = xml.childNodes.item(i);
							var nodeName = item.nodeName;

							if (nodeName == 'name')
								obj.name = item.textContent;

							if (nodeName == 'value') {
								// value is an array
								if (typeof (obj['value']) == "undefined") {
									obj.value = new Array();
								}

								var valobj = {};
								valobj['$'] = item.textContent;
								if (item.attributes.length > 0) {
									for (var j = 0; j < item.attributes.length; j++) {
										var attribute = item.attributes.item(j);
										valobj[attribute.nodeName] = attribute.nodeValue;
									}
								}
								obj.value.push(valobj);
							}
						}
					}
				} else

				// do children
				if (xml.hasChildNodes()) {
					for (var i = 0; i < xml.childNodes.length; i++) {
						var item = xml.childNodes.item(i);
						var nodeName = item.nodeName;

						if (typeof (obj[nodeName]) == "undefined") {
							obj[nodeName] = xml2json(item);
						} else {
							if (typeof (obj[nodeName].push) == "undefined") {
								var old = obj[nodeName];
								obj[nodeName] = [];
								obj[nodeName].push(old);
							}
							obj[nodeName].push(xml2json(item));
						}
					}
				}
				return obj;
			},

			/**
			 * converts a itemcollection into a imixs XML string. The result can
			 * be used to post the string to a Imixs Rest Service API
			 * 
			 * <code>
			 *   <entity xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
			 *      <item><name>namlasteditor</name><value xsi:type="xs:string">ralph.soika@imixs.com</value>
			 *      </item><item><name>$isauthor</name><value xsi:type="xs:boolean">true</value></item>
			 *      ....
			 *   </entity>
			 */
			json2xml = function(workitem) {
				var result = '<entity xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">';

				if (workitem && workitem.item) {
					$.each(workitem.item, function(index, aitem) {
						result = result + '<item><name>' + aitem.name
								+ '</name>';

						if (aitem.value) {
							$.each(aitem.value, function(index, avalue) {
								result = result + '<value xsi:type="'
										+ avalue["xsi:type"] + '">'
										+ avalue["$"] + '</value>';
							});
						}

						result = result + '</item>';
					});

				}

				result = result + '</entity>';
				return result;
			};

	// public API
	return {
		// ItemCollection : ItemCollection,
		xml2json : xml2json,
		json2xml : json2xml,
		xml2collection : xml2collection,
		xml2entity : xml2entity
	};

}());
