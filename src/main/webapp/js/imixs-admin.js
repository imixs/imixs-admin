"use strict";

// define namespace
IMIXS.namespace("org.imixs.workflow.adminclient");

// define core module
IMIXS.org.imixs.workflow.adminclient = (function() {
	if (!BENJS.org.benjs.core) {
		console.error("ERROR - missing dependency: benjs.js");
	}
	if (!IMIXS.org.imixs.core) {
		console.error("ERROR - missing dependency: imixs-core.js");
	}
	if (!IMIXS.org.imixs.xml) {
		console.error("ERROR - missing dependency: imixs-xml.js");
	}

	var benJS = BENJS.org.benjs.core, imixs = IMIXS.org.imixs.core, imixsXML = IMIXS.org.imixs.xml,
	/***************************************************************************
	 * 
	 * MODELS
	 * 
	 **************************************************************************/

	// Model for RestServiceController 
	RestService = function() {
		this.baseURL = "http://localhost:8080/workflow/rest-service";
		this.apiVersion = "4.0";
		this.connected = false;
		this.indexMap = null;
		this.indexName = null;
		this.indexType = null;
		this.configuration= new Workitem();

		/* returns an 2 dimensional array of the index map */
		this.getIndexList = function() {
			var result = new Array();
			var entry;
			for ( var property in this.indexMap) {
				var sonderding = {
					name : property,
					type : this.indexMap[property]
				};
				result.push(sonderding);

			}

			return result;
		}

		this.getStatus = function() {
			if (this.connected) {
				return "<strong>Connected: </strong>" + this.baseURL;
			} else {
				return "<strong>Disconnected!</strong>"
			}
		}

	},

	/* Model for WorklistController */
	Worklist = function() {
		this.query ="";
		this.view;
		this.start = 0;
		this.count = 10;
		this.maxresult=10; // apiVersion 4.0
		this.page=0; // apiVersion 4.0
		this.sortby="$modified";
		this.sortrevrse=true;
		this.sortby_update="$created";
		this.sortrevrse_update=true;
		this.maxresult_deletion=10; // apiVersion 4.0
		this.page_deletion=0; // apiVersion 4.0
		this.maxresult_update=10; // apiVersion 4.0
		this.page_update=0; // apiVersion 4.0
		this.fieldName = "";
		this.fieldType = "";
		this.newValue = "";
		this.filePath = "";
		this.$activityid = 0;
	},

	/* Model for WorklistController */
	Workitem = function(itemarray) {
		imixs.ItemCollection.call(this, itemarray);
		this.id = '';

		/* return summary or txtname */
		this.getSummary = function() {
			var val = this.getItem("$workflowsummary");
			if (!val)
				val = this.getItem("txtname");
			return val;
		}

		/*
		 * return all items sorted by name and provides a index item if the
		 * field has an Imixs-Entity-Index
		 */
		this.getSortedItemlist = function() {
			// add index type and indexIcon
			$.each(this.item, function(index, aitem) {

				if (restServiceController.model.apiVersion=="4.0") {
					if (restServiceController.model.fieldListNoAnalyse.indexOf(aitem.name)>-1) {
						aitem.indexIcon = "<img src='img/index_typ_0.gif' title='no-analyze' />";
					}
					
				} else {
					aitem.index = restServiceController.model.indexMap[aitem.name];
					if ((typeof aitem.index) == 'number') {
						var iconTitle = "";
						if (aitem.index == 0)
							iconTitle = "Text Index";
						else if (aitem.index == 1)
							iconTitle = "Integer Index";
						else if (aitem.index == 2)
							iconTitle = "Double Index";
						else if (aitem.index == 3)
							iconTitle = "Calendar Index";
	
						aitem.indexIcon = "<img src='img/index_typ_" + aitem.index
								+ ".gif' title='" + iconTitle + "' />";
	
						// console.log("index=" + aitem.indexIcon);
					}
				}
			});

			// now we remove items without a name....
			var resultList = new Array();
			$.each(this.item, function(index, aitem) {
				if (aitem.name) {
					resultList.push(aitem);
				}
			});

			// sort resultlist
			return resultList.sort(function(a, b) {
				if (a.name > b.name)
					return 1;
				else if (a.name < b.name)
					return -1;
				else
					return 0;
			});

		}

	},
	
	
	/* Model for AdminPController */
	AdminP = function() {
		this.jobs;
	},
	
	

	/***************************************************************************
	 * 
	 * CONTROLLERS
	 * 
	 **************************************************************************/

	restServiceController = benJS.createController({
		id : "restServiceController",
		model : new RestService()
	}),

	worklistController = benJS.createController({
		id : "worklistController",
		model : new Worklist()
	}),

	workitemController = benJS.createController({
		id : "workitemController",
		model : new Workitem()
	}),
	
	configurationController = benJS.createController({
		id : "configurationController",
		model : new Workitem()
	}),
	
	adminPJobController = benJS.createController({
		id : "adminPJobController",
		model : new Workitem()
	}),
	
	adminPViewController = benJS.createController({
		id : "adminPViewController",
		model : new AdminP()
	}),

	/***************************************************************************
	 * 
	 * ROUTES & TEMPLATES
	 * 
	 **************************************************************************/
	restServiceRoute = benJS.createRoute({
		id : "restservice-route",
		templates : {
			"content" : "view_restservice.html"
		},
		beforeRoute : function(router) {
			restServiceController.pull();
		},
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(1)").addClass('active');

			// update the api_version select box
			$('#api_version').val(restServiceController.model.apiVersion);
			$('#container').imixsLayout();
		}

	}),

	queryRoute = benJS.createRoute({
		id : "query-route",
		templates : {
			"content" : "view_query.html"
		},
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(2)").addClass('active');
			
			// update the priorVersionCheckbox
			if (restServiceController.model.apiVersion=="4.0") {
				$('#sortorderreverse').prop('checked', worklistController.model.sortrevrse);
			}
			
			worklistController.loadWorklist();
			$('#container').imixsLayout();
		}
	}),


	workitemRoute = benJS.createRoute({
		id : "workitem-route",
		templates : {
			"content" : "view_workitem.html"
		},
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(2)").addClass('active');
			$('#container').imixsLayout();
		}
	}),

	bulkUpdateRoute = benJS.createRoute({
		id : "bulkupdate-route",
		templates : {
			"content" : "view_bulkupdate.html"
		},
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(3)").addClass('active');
			
			// update the priorVersionCheckbox
			if (restServiceController.model.apiVersion=="4.0") {
				$('#sortorderreverse_update').prop('checked', worklistController.model.sortrevrse_update);
			}
			
			$('#container').imixsLayout();
		}
	}),

	bulkDeleteRoute = benJS.createRoute({
		id : "bulkdelete-route",
		templates : {
			"content" : "view_bulkdelete.html"
		},
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(4)").addClass('active');
			$('#container').imixsLayout();
		}
	}),

	backupRoute = benJS.createRoute({
		id : "backup-route",
		templates : {
			"content" : "view_backup.html"
		},
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(5)").addClass('active');
			$('#container').imixsLayout();
		}
	}), 
	
	adminpRoute = benJS.createRoute({
		id : "adminp-route",
		templates : {
			"content" : "view_adminp.html"
		},
		
		beforeRoute : function(router) {
			console.log('create empty adminp job');
			// init adminp
			adminPJobController.model=new Workitem();
			adminPJobController.model.setItem('datfrom','','xs:dateTime');
			adminPJobController.model.setItem('datto','','xs:dateTime');
			adminPJobController.model.setItem('typelist','');

			adminPJobController.model.numblocksize_update=100;
			adminPJobController.model.numblocksize_migration=100;
			adminPJobController.model.numblocksize_renameuser=100;

			
			adminPJobController.model.numindex_update=0;
			adminPJobController.model.numindex_renameuser=0;
			adminPJobController.model.numindex_migration=0;
			
			adminPJobController.model.numinterval_update=1;
			adminPJobController.model.numinterval_migration=1;
			adminPJobController.model.numinterval_renameuser=1;
			
			
		},
		
		
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(6)").addClass('active');
			adminPViewController.loadJobs();
			$('#container').imixsLayout();
		}
	}),
	
	configurationRoute = benJS.createRoute({
		id : "configuration-route",
		templates : {
			"content" : "view_index.html"
		},
		
	
		afterRoute : function(router) {
			$("#imixs-nav ul li").removeClass('active');
			$("#imixs-nav ul li:nth-child(7)").addClass('active');
			$('#container').imixsLayout();
		}
	}),
	



	contentTemplate = benJS.createTemplate({
		id : "content",
		afterLoad : layoutSection
	}),

	/**
	 * Start the BenJS Application
	 */
	start = function() {
		var loc, url, service = "";
		console.debug("starting application...");

		// compute application root....
		loc = window.location;
		url = window.location.href;
		url = url.substring(0, url.indexOf(loc.pathname));

		// test if a service query param is provided....
		if (url.indexOf('service=') > -1) {
			service = url.substring(url.indexOf('service=') + 8);
			restServiceController.model.baseURL = url + "/" + service;
		} else {
			// test if we know a cookie...
			var lastURL = getCookie("org.imixs.workflow.adminclient.baseurl");
			if (lastURL) {
				restServiceController.model.baseURL =lastURL;
			}
		}

		
		// start 
		benJS.start();
	
		// default route....
		restServiceRoute.route();
		$("#imixs-error").hide();
	};

	
	/* Custom method to process a single workitem */
	workitemController.processWorkitem = function(workitem) {

		var xmlData;
		if (restServiceController.model.apiVersion!="4.0") {
			xmlData= imixsXML.json2xmlEntity(workitem);
		} else {
			xmlData= imixsXML.json2xml(workitem);
		}
		
		// console.debug(xmlData);
		console.debug("process workitem: '" + workitem.getItem('$uniqueid')
				+ "'...");

		var url = restServiceController.model.baseURL;
		url = url + "/workflow/workitem/";

		$.ajax({
			type : "POST",
			url : url,
			data : xmlData,
			contentType : "text/xml",
			dataType : "xml",
			cache : false,
			error : function(jqXHR, error, errorThrown) {
				var message = errorThrown;
				var json = imixsXML.xml2document(jqXHR.responseXML);
				var workitem = new Workitem(json);
				workitemController.model.item = json.entity.item;
				var uniqueid = workitem.getItem('$uniqueid');
				var error_code = workitem.getItem('$error_code');
				var error_message = workitem.getItem('$error_message');

				printLog("<br />" + uniqueid + " : " + error_code + " - "
						+ error_message, true);

				$("#error-message").text("BulkUpdate failed");
				$("#imixs-error").show();
			},
			success : function(xml) {
				printLog(".", true);
			}
		});

	};

	/* Custom method to save a single workitem */
	workitemController.saveWorkitem = function(workitem) {

		var xmlData;
		if (restServiceController.model.apiVersion!="4.0") {
			xmlData= imixsXML.json2xmlEntity(workitem);
		} else {
			xmlData= imixsXML.json2xml(workitem);
		}

		
		// console.debug(xmlData);
		console.debug("save workitem: '" + workitem.getItem('$uniqueid')
				+ "'...");

		var url = restServiceController.model.baseURL;
		if (restServiceController.model.apiVersion == "4.0") {
			url = url + "/documents/";
		} else {
			url = url + "/entity/";
		}

		$.ajax({
			type : "POST",
			url : url,
			data : xmlData,
			contentType : "text/xml",
			dataType : "xml",
			cache : false,
			error : function(jqXHR, error, errorThrown) {
				var message = errorThrown;
				var json = imixsXML.xml2document(jqXHR.responseXML);
				var workitem = new Workitem(json);
				workitemController.model.item = json.entity.item;
				var uniqueid = workitem.getItem('$uniqueid');
				var error_code = workitem.getItem('$error_code');
				var error_message = workitem.getItem('$error_message');

				printLog("<br />" + uniqueid + " : " + error_code + " - "
						+ error_message, true);

				$("#error-message").text("BulkUpdate failed");
				$("#imixs-error").show();
			},
			success : function(xml) {
				printLog(".", true);
			}
		});

	};

	/* Custom method to load a single workitem */
	workitemController.loadWorkitem = function(context) {

		var entry = $('span', context);
		if (entry.length == 1) {

			var id = $(entry).text();

			workitemController.model.id = id;
		}

		console
				.debug("load workitem: '" + workitemController.model.id
						+ "'...");

		var url = restServiceController.model.baseURL;
		
		if (restServiceController.model.apiVersion == "4.0") {
			url = url + "/documents/" + workitemController.model.id;
		} else {
			// old version
			url = url + "/workflow/workitem/" + workitemController.model.id;
		}

		$.ajax({
			type : "GET",
			url : url,
			dataType : "xml",
			success : function(response) {
				console.debug(response);
				workitemController.model.item = imixsXML.xml2document(response);
				workitemRoute.route();
				// workitemController.push();
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	/**
	 * deletes a worktiem. Expects a config element containing optional context,
	 * id and callback:
	 * 
	 * <code> 
	 *   { context:this, 
	 *     confirm: boolean
	 *     uniqueid:String, 
	 *     callback:function 
	 *   }
	 * </code>
	 */
	workitemController.deleteWorkitem = function(config) {

		if (config.context) {
			var entry = $(config.context).closest('[data-ben-entry]');
			var entryNo = $(entry).attr("data-ben-entry");
			var workitem = new imixs.ItemCollection(
					worklistController.model.view[entryNo]);

			var id = workitem.getItem('$uniqueid');
			if (id) {
				config.uniqueid = id;
			}
		}

		if (typeof config.confirm === "undefined") {
			config.confirm = true;
		}

		if (config.confirm === true) {
			// confirm dialog
			if (!confirm('Delete Entity ' + id + ' ?')) {
				return false;
			}

		}

		// delete workitem
		console.debug("delete entity: '" + config.uniqueid + "'...");

		var url = restServiceController.model.baseURL;
		if (restServiceController.model.apiVersion=="4.0") {
			url = url + "/documents/" + config.uniqueid;
		} else {
			url = url + "/entity/" + config.uniqueid;
		}

		$.ajax({
			type : "DELETE",
			url : url,
			success : function(response) {
				printLog(".", true);

				// callback
				if (typeof config.callback === "function") {
					config.callback();
				}
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	/*
	 * Read the index list and open the query view
	 */
	restServiceController.connect = function() {

		worklistController.model.view = null;
		
		this.pull();

		// get apiVersion 
		restServiceController.model.apiVersion = $('#api_version').val();

		// remove last / if provided
		restServiceController.model.baseURL = restServiceController.model.baseURL
				.trim();
		if (restServiceController.model.baseURL.endsWith("/")) {
			restServiceController.model.baseURL = restServiceController.model.baseURL
					.substring(0,
							restServiceController.model.baseURL.length - 1);
		}

		console.log("baseURL=" + restServiceController.model.baseURL);
		console.log("apiVersion=" + restServiceController.model.apiVersion);

		// read indexlist if api version < 4.0...
		if (restServiceController.model.apiVersion=="4.0") {
			$.ajax({
				
				type : "GET",
				url : this.model.baseURL + "/documents/configuration",
				dataType : "xml",
				contentType : "application/xml",
				success : function(response) {
					restServiceController.model.connected = true;
					setCookie("org.imixs.workflow.adminclient.baseurl", restServiceController.model.baseURL,30);
					restServiceController.model.configuration =imixsXML.xml2document(response);
					
					configurationController.model.item = imixsXML.xml2document(response);
					
					
					$.each(restServiceController.model.configuration, function(index, aitem) {
						if (aitem.name=="lucence.indexfieldlistnoanalyze") {
							restServiceController.model.indexfieldlistnoanalyze=aitem.value;
							var fieldListNoAnalyse = new Array();
							$.each(aitem.value, function(index, aitem) {
								fieldListNoAnalyse.push(aitem.$);
							});
							restServiceController.model.fieldListNoAnalyse=fieldListNoAnalyse;
							return true;
						}						
					});

					
					// setup routes
					configurationRoute.templates= {	"content" : "view_configuration.html" };
					queryRoute.templates= {	"content" : "view_search.html" };
					
					// set default query
					if (!worklistController.model.query) {
						worklistController.model.query= "(type:\"workitem\")";
					}
					
					$("#imixs-error").hide();
					$("#imixs-status").show();
					
					queryRoute.route();
				},
				error : function(jqXHR, error, errorThrown) {
					restServiceController.model.connected = false;
					$("#error-message").text(errorThrown);
					$("#imixs-error").show();
					$("#imixs-status").hide();
				}
			});
			
			
		} else {
			$.ajax({
				type : "GET",
				url : this.model.baseURL + "/entity/indexlist",
				dataType : "json",
				success : function(response) {
					restServiceController.model.connected = true;
					setCookie("org.imixs.workflow.adminclient.baseurl", restServiceController.model.baseURL,30);
					restServiceController.model.indexMap = response.map;
					
					// setup routes
					configurationRoute.templates= {	"content" : "view_index.html" };
					queryRoute.templates= {	"content" : "view_query.html" };
					
					// set default query
					if (!worklistController.model.query) {
						worklistController.model.query= "SELECT entity FROM Entity entity where entity.type='workitem' ORDER BY entity.modified DESC";
					}
					
					
					$("#imixs-error").hide();
					$("#imixs-status").show();

					queryRoute.route();
				},
				error : function(jqXHR, error, errorThrown) {
					restServiceController.model.connected = false;
					$("#error-message").text(errorThrown);
					$("#imixs-error").show();
					$("#imixs-status").hide();

				}
			});
		}
	}

	/*
	 * Updtes the index list and open the index view
	 */
	restServiceController.updateIndexlist = function() {
		this.pull();
		// read indexlist...
		$.ajax({
			type : "GET",
			url : this.model.baseURL + "/entity/indexlist",
			dataType : "json",
			success : function(response) {
				restServiceController.model.indexName = "";
				restServiceController.model.indexType = "";
				restServiceController.model.indexMap = response.map;
				configurationRoute.route();
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});
	}

	/* removes an index */
	restServiceController.removeIndex = function(context) {
		var entry = $(context).closest('[data-ben-entry]');
		var entryNo = $(entry).attr("data-ben-entry");

		var indexEntry = restServiceController.model.getIndexList()[entryNo];
		if (confirm('Delete Index ' + indexEntry.name + ' ?')) {
			var url = restServiceController.model.baseURL;
			url = url + "/entity/index/" + indexEntry.name;

			$.ajax({
				type : "DELETE",
				url : url,
				success : function(response) {
					restServiceController.updateIndexlist();
				},
				error : function(jqXHR, error, errorThrown) {
					$("#error-message").text("Unable to remove index");
					$("#imixs-error").show();
				}
			});
		}
	}

	/* removes an index */
	restServiceController.addIndex = function() {
		restServiceController.pull();
		if (confirm('Add new Index ' + this.model.indexName + ' ?')) {
			var url = restServiceController.model.baseURL;
			url = url + "/entity/index/" + this.model.indexName + "/"
					+ this.model.indexType;

			$.ajax({
				type : "PUT",
				url : url,
				success : function(response) {
					restServiceController.model.indexName = "";
					restServiceController.model.indexType = "";
					restServiceController.updateIndexlist();
				},
				error : function(jqXHR, error, errorThrown) {
					$("#error-message").text(
							"Unable to add index - wrong format");
					$("#imixs-error").show();
				}
			});
		}
	}

	/* Custom method to load a worklist */
	worklistController.loadWorklist = function() {
		worklistController.pull();
		console.debug("load worklist: '" + worklistController.model.query
				+ "'...");

		var url = restServiceController.model.baseURL;
		var query = worklistController.model.query;
		if (query=="")
			return;
		
		// get sortorderreverse flag manually (not yet supported by benJS)
		worklistController.model.sortreverse=$('#sortorderreverse').is(':checked');
		
		
		// replace new lines..
		query = query.replace(/(\r\n|\n|\r)/gm, " ");

		if (restServiceController.model.apiVersion == "3.7") {
			url = url + "/entity/entitiesbyquery/" + query;
			url = url + "?start=" + worklistController.model.start + "&count="
			+ worklistController.model.count;
		} else if (restServiceController.model.apiVersion == "3.8") {
			url = url + "/entity/query/" + query;
			url = url + "?start=" + worklistController.model.start + "&count="
			+ worklistController.model.count;
		} else {
			// default support 4.0.x
			url = url + "/documents/search/" + query;
			url = url + "?pageSize=" + worklistController.model.maxresult + "&pageIndex="
			+ worklistController.model.page + "&sortBy=" +  worklistController.model.sortby + "&sortReverse=" +  worklistController.model.sortreverse;
		}
	

		$.ajax({
			type : "GET",
			url : url,
			dataType : "xml",
			success : function(response) {
				worklistController.model.view = imixsXML
						.xml2collection(response);
				// push content
				worklistController.push();
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	/**
	 * Bulk Update - processes a selection of workitems and updates a field
	 * information
	 * 
	 */
	worklistController.bulkUpdate = function() {

		worklistController.pull();
		
		// get bulkupdateappendvalue flag manually (not yet supported by benJS)
		var bAppendValue=$('#bulkupdateappendvalue').is(':checked');
		
		// test fieldname...
		var re = /[a-zA-Z0-9\-\_\$\~]$/
		if (!re.test(worklistController.model.fieldName)
				|| worklistController.model.fieldName.indexOf('\\') > -1) {
			alert("Invalid Fieldname");
			return false;
		}

		if (!confirm("Do you really want to start a bulk update now?")) {
			return false;
		}

		clearLog();
		printLog("Load worklist: '" + worklistController.model.query + "'...");

		var url = restServiceController.model.baseURL;
		if (restServiceController.model.apiVersion=="3.7") {
			url = url + "/entity/entitiesbyquery/"
			+ worklistController.model.query;
			url = url + "?start=" + (worklistController.model.maxresult_update*worklistController.model.page_update) + "&count="
			+ worklistController.model.maxresult_update;
		} else  if (restServiceController.model.apiVersion=="3.8") {
			url = url + "/entity/query/" + worklistController.model.query;
			url = url + "?start=" + worklistController.model.start + "&count="
			+ worklistController.model.maxresult_update;
		} else {
			// default 4.0.0
			url = url + "/documents/search/" + worklistController.model.query;
			url = url + "?pageSize=" + worklistController.model.maxresult_update + "&pageIndex="
			+ worklistController.model.page_update;
			
			// get sortorderreverse flag manually (not yet supported by benJS)
			worklistController.model.sortreverse_update=$('#sortorderreverse_update').is(':checked');
			
			url = url + "&sortBy=" +  worklistController.model.sortby_update + "&sortReverse=" +  worklistController.model.sortreverse_update;
		}

		$.ajax({
			type : "GET",
			url : url,
			dataType : "xml",
			success : function(response) {
				worklistController.model.view = imixsXML
						.xml2collection(response);
				printLog("Start processing "
						+ worklistController.model.view.length + " workitems",
						true);

				// var itemCol=new ItemCollection();
				$.each(worklistController.model.view,
						function(index, entity) {
							var workitem = new Workitem(entity);
							var uniqueid = workitem.getItem('$uniqueid');
							// printLog(".", true);

							// construct workitem to be
							// processed....
							var updatedWorkitem = new Workitem();

							updatedWorkitem.setItem("$uniqueid", uniqueid,
									"xs:string");
							
							// create an array form the new value...
							var newValueList;
							worklistController.model.newValue=worklistController.model.newValue.trim();
							// if the new value contains \n than we deal this as a single value 
							if (worklistController.model.newValue.indexOf("\\n")>-1) {
								// deal as single value 
								var singleValue = worklistController.model.newValue.split('\\n').join('');
								// create a single-value-array
								newValueList= new Array(singleValue);
							} else {
								//split new lines into an array with separate values
								newValueList=worklistController.model.newValue.split("\n");
							}
							// if bAppendValue = true 
							// than we need to add the new values to the existing ones...
							if (bAppendValue) { 
								var oldValueList=workitem.getItemList(worklistController.model.fieldName.trim());
								// now we have a value array
								// append the new values
								newValueList = oldValueList.concat(newValueList);
							} 
							// create the item...
							var valueObj = {
								"name" : worklistController.model.fieldName.trim(),
								"value" : []
							};
							
							// put all items values....
							$.each(newValueList,
									function(index, avalue) {
								valueObj.value[index]={
										"xsi:type" : worklistController.model.fieldType,
										"$" : avalue
									}
							});
							
							updatedWorkitem.item.push(valueObj);
							
					
			
							// process or save the workitem?
							if (worklistController.model.$activityid > 0) {
								// set activityID
								updatedWorkitem.setItem("$activityid",
										worklistController.model.$activityid,
										"xs:int");
								workitemController
										.processWorkitem(updatedWorkitem);
							} else {
								// save entity
								workitemController
										.saveWorkitem(updatedWorkitem);
							}

						});

			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	/**
	 * Bulk Delete - removes a selection of workitems
	 * 
	 */
	worklistController.bulkDelete = function() {
		if (!confirm("Do you realy want to start a bulk delete now?")) {
			return false;
		}
		worklistController.pull();
		clearLog();
		printLog("Load worklist: '" + worklistController.model.query + "'...");

		var url = restServiceController.model.baseURL;

		if (restServiceController.model.apiVersion=="3.7") {
			url = url + "/entity/entitiesbyquery/"
			+ worklistController.model.query;
			url = url + "?start=" + (worklistController.model.page_deletion*worklistController.model.maxresult_deletion) + "&count="
			+ worklistController.model.maxresult_deletion;
		} else if (restServiceController.model.apiVersion=="3.8") { 
			url = url + "/entity/query/" + worklistController.model.query;
			url = url + "?start=" + (worklistController.model.page_deletion*worklistController.model.maxresult_deletion)+ "&count="
			+ worklistController.model.maxresult_deletion;
		} else {
			// default 4.0.0
			url = url + "/documents/search/" + worklistController.model.query;
			url = url + "?pageSize=" + worklistController.model.maxresult_deletion + "&pageIndex="
			+ worklistController.model.page_deletion;
		}
			

		$.ajax({
			type : "GET",
			url : url,
			dataType : "xml",
			success : function(response) {
				worklistController.model.view = imixsXML
						.xml2collection(response);
				printLog("Start deleting "
						+ worklistController.model.view.length + " entities",
						true);

				// var itemCol=new ItemCollection();
				$.each(worklistController.model.view, function(index, entity) {
					var workitem = new Workitem(entity);
					var uniqueid = workitem.getItem('$uniqueid');
					// delete entity
					workitemController.deleteWorkitem({
						uniqueid : uniqueid,
						confirm : false
					});
				});
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	/**
	 * Backup a result set into the filesystem
	 * 
	 */
	worklistController.backup = function() {
		if (!confirm("Do you realy want to start a backup data now?")) {
			return false;
		}
		worklistController.pull();
		clearLog();
		printLog("Backup started....");

		var url = restServiceController.model.baseURL;
		if (restServiceController.model.apiVersion=="4.0") {
			url = url + "/documents/backup/" + worklistController.model.query;
		} else {
			url = url + "/entity/backup/" + worklistController.model.query;
		}
		url = url + "?filepath=" + worklistController.model.filePath;

		$.ajax({
			type : "PUT",
			url : url,
			success : function(response) {
				printLog("Backup finished successful!", true);
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	/**
	 * restores a backup
	 * 
	 */
	worklistController.restore = function() {
		if (!confirm("Do you realy want to start a restore now?")) {
			return false;
		}
		worklistController.pull();
		clearLog();
		printLog("Restore started....");

		var url = restServiceController.model.baseURL;
		
		if (restServiceController.model.apiVersion=="4.0") {
			url = url + "/documents/restore?filepath="
			+ worklistController.model.filePath;
		} else {
			url = url + "/entity/restore?filepath="
			+ worklistController.model.filePath;
		}

		$.ajax({
			type : "GET",
			url : url,
			success : function(response) {
				printLog("Restore finished successful!", true);
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}
	
	
	
	
	/* Custom method to load the adminP jobs */
	adminPViewController.loadJobs = function() {
		//worklistController.pull();
		console.debug("load adminP Jobs..");

		var url = restServiceController.model.baseURL;
		// default support 4.0.x
		url = url + "/adminp/jobs?pageSize=50";
	
		$.ajax({
			type : "GET",
			url : url,
			dataType : "xml",
			success : function(response) {
				adminPViewController.model.jobs = imixsXML
						.xml2collection(response);
				// push content
				adminPViewController.push();
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	/**
	 * Create AdminP Job REBUILD_LUCENE_INDEX
	 * 
	 */
	adminPJobController.createReindexJob = function() {

		adminPJobController.pull();
		adminPJobController.model.setItem("type","adminp");
		adminPJobController.model.setItem("job","REBUILD_LUCENE_INDEX");
		
		// update blocksize/index properties
		adminPJobController.model.setItem("numblocksize",adminPJobController.model.numblocksize_update,'xs:int');
		adminPJobController.model.setItem("numinterval",adminPJobController.model.numinterval_update,'xs:int');

		 // convert date objects into ISO 8601 format
 		imixsUI.convertDateTimeInput(adminPJobController.model);
		var xmlData;
		if (restServiceController.model.apiVersion!="4.0") {
			xmlData= imixsXML.json2xmlEntity(adminPJobController.model);
		} else {
			xmlData= imixsXML.json2xml(adminPJobController.model);
		}
		console.debug("create new adminp job...");

		var url = restServiceController.model.baseURL;
		url = url + "/adminp/jobs";

		$.ajax({
			type : "POST",
			url : url,
			data : xmlData,
			contentType : "text/xml",
			dataType : "xml",
			cache : false,
			error : function(jqXHR, error, errorThrown) {
				var message = errorThrown;
				var json = imixsXML.xml2document(jqXHR.responseXML);
				var workitem = new Workitem(json);
				if (json.document)
					workitemController.model.item = json.document.item;
				else if (json.entity)
					workitemController.model.item = json.entity.item;
				var uniqueid = workitem.getItem('$uniqueid');
				var error_code = workitem.getItem('$error_code');
				var error_message = workitem.getItem('$error_message');

				printLog("<br />" + uniqueid + " : " + error_code + " - "
						+ error_message, true);

				$("#error-message").text("Create new job failed");
				$("#imixs-error").show();
			},
			success : function(xml) {
				printLog(".", true);
				adminPViewController.loadJobs();
			}
		});
	};
	
	
	/**
	 * Create AdminP Job RENAME_USER
	 * 
	 */
	adminPJobController.createRenameUserJob = function() {
		$("#imixs-error").hide();
		adminPJobController.pull();
		adminPJobController.model.setItem("type","adminp");
		adminPJobController.model.setItem("job","RENAME_USER");
		
		// update blocksize/index properties
		adminPJobController.model.setItem("numblocksize",adminPJobController.model.numblocksize_renameuser,'xs:int');
		adminPJobController.model.setItem("numinterval",adminPJobController.model.numinterval_renameuser,'xs:int');
		
		adminPJobController.model.setItem("namfrom",adminPJobController.model.from_user);
		adminPJobController.model.setItem("namto",adminPJobController.model.to_user);
		adminPJobController.model.setItem("typelist",adminPJobController.model.typelist_renameuser);
		
		
		if (!adminPJobController.model.from_user || !adminPJobController.model.to_user) {
			$("#error-message").text("Enter the UserID to migrate");
			$("#imixs-error").show();
			return;
		}
		
		// get renamefullreplace flag manually (not yet supported by benJS)
		var bFullReplace=$('#renamefullreplace').is(':checked');
		adminPJobController.model.setItem('keyreplace',bFullReplace,'xs:boolean');
		
		
		 // convert date objects into ISO 8601 format
 		imixsUI.convertDateTimeInput(adminPJobController.model);
 		var xmlData;
		if (restServiceController.model.apiVersion!="4.0") {
			xmlData= imixsXML.json2xmlEntity(adminPJobController.model);
		} else {
			xmlData= imixsXML.json2xml(adminPJobController.model);
		}
		console.debug("create new adminp job...");

		var url = restServiceController.model.baseURL;
		url = url + "/adminp/jobs";

		$.ajax({
			type : "POST",
			url : url,
			data : xmlData,
			contentType : "text/xml",
			dataType : "xml",
			cache : false,
			error : function(jqXHR, error, errorThrown) {
				var message = errorThrown;
				var json = imixsXML.xml2document(jqXHR.responseXML);
				var workitem = new Workitem(json);
				if (json.document)
					workitemController.model.item = json.document.item;
				else if (json.entity)
					workitemController.model.item = json.entity.item;
				var uniqueid = workitem.getItem('$uniqueid');
				var error_code = workitem.getItem('$error_code');
				var error_message = workitem.getItem('$error_message');

				printLog("<br />" + uniqueid + " : " + error_code + " - "
						+ error_message, true);

				$("#error-message").text("Create new job failed");
				$("#imixs-error").show();
			},
			success : function(xml) {
				printLog(".", true);
				adminPViewController.loadJobs();
			}
		});
	};
	
	
	
	
	/**
	 * Create AdminP Job MIGRATION
	 * 
	 */
	adminPJobController.createMigrationJob = function() {

		adminPJobController.pull();
		adminPJobController.model.setItem("type","adminp");
		adminPJobController.model.setItem("job","MIGRATION");
		
		// update blocksize/index properties
		adminPJobController.model.setItem("numindex",adminPJobController.model.numindex_migration);
		adminPJobController.model.setItem("numblocksize",adminPJobController.model.numblocksize_migration);
		adminPJobController.model.setItem("numinterval",adminPJobController.model.numinterval_migration);
		
		 // convert date objects into ISO 8601 format
 		imixsUI.convertDateTimeInput(adminPJobController.model);
 		var xmlData;
		if (restServiceController.model.apiVersion!="4.0") {
			xmlData= imixsXML.json2xmlEntity(adminPJobController.model);
		} else {
			xmlData= imixsXML.json2xml(adminPJobController.model);
		}
		console.debug("create new adminp job...");

		var url = restServiceController.model.baseURL;
		url = url + "/adminp/jobs";

		$.ajax({
			type : "POST",
			url : url,
			data : xmlData,
			contentType : "text/xml",
			dataType : "xml",
			cache : false,
			error : function(jqXHR, error, errorThrown) {
				var message = errorThrown;
				var json = imixsXML.xml2document(jqXHR.responseXML);
				var workitem = new Workitem(json);
				if (json.document)
					workitemController.model.item = json.document.item;
				else if (json.entity)
					workitemController.model.item = json.entity.item;
				var uniqueid = workitem.getItem('$uniqueid');
				var error_code = workitem.getItem('$error_code');
				var error_message = workitem.getItem('$error_message');

				printLog("<br />" + uniqueid + " : " + error_code + " - "
						+ error_message, true);

				$("#error-message").text("Create new job failed");
				$("#imixs-error").show();
			},
			success : function(xml) {
				printLog(".", true);
				adminPViewController.loadJobs();
			}
		});
	};
	
	
	
	/**
	 * Deletes a AdminP job. The method Expects a config element containing optional context,
	 * id and callback:
	 * 
	 * <code> 
	 *   { context:this, 
	 *     confirm: boolean
	 *     uniqueid:String, 
	 *     callback:function 
	 *   }
	 * </code>
	 */
	adminPViewController.deleteJob = function(config) {

		if (config.context) {
			var entry = $(config.context).closest('[data-ben-entry]');
			var entryNo = $(entry).attr("data-ben-entry");
			var workitem = new imixs.ItemCollection(
					adminPViewController.model.jobs[entryNo]);

			var id = workitem.getItem('$uniqueid');
			if (id) {
				config.uniqueid = id;
			}
		}

		if (typeof config.confirm === "undefined") {
			config.confirm = true;
		}

		if (config.confirm === true) {
			// confirm dialog
			if (!confirm('Are you sure that you want to delete the AdminP Job ' + id + ' now?')) {
				return false;
			}

		}

		// delete workitem
		console.debug("delete AdminP job: '" + config.uniqueid + "'...");

		var url = restServiceController.model.baseURL;
		url = url + "/adminp/jobs/" + config.uniqueid;

		$.ajax({
			type : "DELETE",
			url : url,
			success : function(response) {
				printLog("Job deleted.", true);
				// callback
				if (typeof config.callback === "function") {
					config.callback();
				}
			},
			error : function(jqXHR, error, errorThrown) {
				$("#error-message").text(errorThrown);
				$("#imixs-error").show();
			}
		});

	}

	
	
	

	// public API
	return {
		Workitem : Workitem,
		restServiceRoute : restServiceRoute,
		restServiceController : restServiceController,
		worklistController : worklistController,
		workitemController : workitemController,
		configurationController : configurationController,
		adminPJobController : adminPJobController,
		adminPViewController : adminPViewController,
		queryRoute : queryRoute,
		bulkUpdateRoute : bulkUpdateRoute,
		bulkDeleteRoute : bulkDeleteRoute,
		configurationRoute : configurationRoute,
		backupRoute : backupRoute,
		adminpRoute : adminpRoute,
		start : start
	};

}());

function layoutSection(templ, context) {
	// $(context).i18n();
	// $(context).imixsLayout();
	$("#imixs-error").hide();

};

function printLog(message, noLineBrake) {
	console.debug(message);

	$("#imixs-log #log-message").append(message);
	if (!noLineBrake)
		$("#imixs-log #log-message").append("<br />");
}

function clearLog(message, noLineBrake) {

	$("#imixs-log #log-message").empty();
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length,c.length);
        }
    }
    return "";
}

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}