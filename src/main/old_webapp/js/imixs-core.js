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
 * This library provides the core module functionality
 * 
 * Version 1.0.2
 */

var IMIXS = IMIXS || {};

// namespace function (by Stoyan Stefanov - JavaScript Patterns)
IMIXS.namespace = function(ns_string) {
	var parts = ns_string.split('.'), parent = IMIXS, i;

	// strip redundant leading global
	if (parts[0] === "IMIXS") {
		parts = parts.slice(1);
	}

	for (i = 0; i < parts.length; i += 1) {
		// create a property if it dosn't exist yet
		if (typeof parent[parts[i]] === "undefined") {
			parent[parts[i]] = {};
		}
		parent = parent[parts[i]];
	}
	return parent;

};

IMIXS.namespace("org.imixs.core");

IMIXS.org.imixs.core = (function() {

	// private properties
	var _not_used,

	/**
	 * Helper method to test for HTML 5 localStorage...
	 * 
	 * @returns {Boolean}
	 */
	hasLocalStorage = function() {
		try {
			return 'localStorage' in window && window['localStorage'] !== null;
		} catch (e) {
			return false;
		}
	},

	/* Imixs ItemCollection */
	ItemCollection = function(itemarray) {

		if (!itemarray) {
			// if no itemarray is provided than create an empty one
			this.item = new Array();
		} else {
			if ($.isArray(itemarray)) {
				this.item = itemarray;
			} else {
				// we test now which object is provided - entity or an
				// item[]....
				if (itemarray.entity && $.isArray(itemarray.entity.item)) {
					this.item = itemarray.entity.item;
				} else if ($.isArray(itemarray.item)) {
					this.item = itemarray.item;
				}
			}
		}

		// returns the index pos of an item
		this.findItem = function(fieldName) {
			var resultKey = -1;
			$.each(this.item, function(index, aitem) {
				if (aitem && aitem.name == fieldName) {
					resultKey = index;
					return false;
				}
			});
			return resultKey;

		}

		/**
		 * This method is used to return the first value of an item with the given name
		 * inside the current ItemCollection. If no item with this name exists the
		 * method adds a new element with this name.
		 */
		this.getItem = function(fieldName) {
			if (!this.item)
				return "";

			var resultKey = -1;

			resultKey = this.findItem(fieldName);

			// check if field exists?
			if (resultKey == -1) {
				// create a new element
				valueObj = {
					"name" : fieldName,
					"value" : [ {
						"xsi:type" : "xs:string",
						"$" : ""
					} ]
				};
				this.item.push(valueObj);
				resultKey = this.item.length - 1;
			}

			var valueObj = this.item[resultKey].value[0];
			if (valueObj) {
				if (typeof (valueObj['$']) == "undefined")
					return valueObj;
				else
					return valueObj['$'];
			} else
				return "";

		}
		
		
		/**
		 * This method is used to return the value array of a name item inside
		 * the current ItemCollection. If no item with this name exists the
		 * method adds a new element with this name.
		 */
		this.getItemList = function(fieldName) {
			if (!this.item)
				return "";

			var resultKey = -1;

			resultKey = this.findItem(fieldName);

			// check if field exists?
			if (resultKey == -1) {
				// create a new element
				valueObj = {
					"name" : fieldName,
					"value" : [ {
						"xsi:type" : "xs:string",
						"$" : ""
					} ]
				};
				this.item.push(valueObj);
				resultKey = this.item.length - 1;
			}

			var valueListObj = this.item[resultKey].value;
			var valueList = new Array();
			if (valueListObj) {
				// extract values...
				$.each(valueListObj, function(index, valueObj) {
					if (typeof (valueObj['$']) == "undefined")
						valueList.push( valueObj);
					else
						valueList.push( valueObj['$']);
					
				});
			}
	
			return valueList;

		}
		

		/**
		 * Adds a new item into the collection
		 */
		this.setItem = function(fieldName, value, xsiType) {
			// test if item still exists?
			var resultKey = this.findItem(fieldName);

			if (resultKey>-1) {
				 this.item[resultKey].value[0]['$']=value;
			} else {
				// create item...
				if (!xsiType)
					xsiType = "xs:string";
				var valueObj = {
					"name" : fieldName,
					"value" : [ {
						"xsi:type" : xsiType,
						"$" : value
					} ]
				};
				this.item.push(valueObj);
			}
		}

		/**
		 * formats a date output.
		 * 
		 * The method accepts a format parameter to format the date output. If
		 * no format is defined the method test if the imixs.ui library is
		 * available. If not the default output format 'dd.mm.yy' is used.
		 */
		this.getItemDate = function(fieldName, format) {
			// set default date format
			if (!format) {
				// test if UI dateFormat is available
				if (IMIXS.org.imixs.ui) {
					format = IMIXS.org.imixs.ui.dateFormat;
				} else {
					format = 'dd.mm.yy';
				}
			}
			var value = this.getItem(fieldName);
			if (value) {
				return $.datepicker.formatDate(format, new Date(value));
			} else {
				return "";
			}
		}

		/**
		 * Update the item array depending on the provided object type. The
		 * method accepts entity, item[] or XMLDocuments
		 */
		this.setEntity = function(data) {

			if (!data) {
				// if no itemarray is provided than create an empty one
				this.item = new Array();
			} else
			// test if xmlDocument
			if ($.isXMLDoc(data)) {
				// parse xml doc...
				var json = IMIXS.org.imixs.xml.xml2json(data);
				this.item = json.entity.item;
			} else
			// test if data is an item[]
			if ($.isArray(data)) {
				this.item = data;
			} else
			// test if data is entity
			if (data.entity && $.isArray(data.entity.item)) {
				this.item = data.entity.item;
			} else if ($.isArray(data.item)) {
				this.item = data.item;
			}
		}

	};

	// public API
	return {
		hasLocalStorage : hasLocalStorage,
		ItemCollection : ItemCollection
	};

}());
