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
 * This library is based on Ben.JS and provides implementation for the
 * Imixs-Workflow framework.
 * 
 */

/* Imixs ItemCollection */
var ItemCollection = function(itemarray) {

	if (!itemarray) {
		// if no itemarray is provided than create an empty one
		this.item = new Array();
	} else {
		if ($.isArray(itemarray)) {
			this.item = itemarray;
		} else {
			if ($.isArray(itemarray.item)) {
				this.item = itemarray.item;
			}
		}
	}

	/**
	 * This method is used to return the value array of a name item inside the
	 * current ItemCollection. If no item with this name exists the method adds
	 * a new element with this name.
	 */
	this.getItem = function(fieldName) {
		if (!this.item)
			return "";

		var resultKey = -1;

		$.each(this.item, function(index, aitem) {
			if (aitem && aitem.name == fieldName) {
				resultKey = index;
				return false;
			}
		});

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
	 * formats a date output
	 */
	this.getItemDate = function(fieldName) {
		var value = this.getItem(fieldName);
		return $.datepicker.formatDate('dd. M yy', new Date(value));

	}

};

/**
 * converts a XML result set form the Imixs Rest Service API into a JSON object.
 * Based on the idears from David Walsh
 * (http://davidwalsh.name/convert-xml-json)
 */
function xml2json(xml) {
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

	// process item? in this case we construct the properties name, value and
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
};