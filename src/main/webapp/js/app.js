function layoutSection(templ, context) {
	// $(context).i18n();
	// $(context).imixsLayout();
	$("#imixs-error").hide();
};

/*******************************************************************************
 * 
 * MODELS
 * 
 ******************************************************************************/

var RestService = function() {
	this.baseURL = "http://localhost:8080/backlog-rest";
};

/* WorklistController */
var Worklist = function() {
	this.query = "SELECT entity FROM Entity entity ORDER BY entity.modified DESC";
	this.view;
	this.start = 0;
	this.count = 10;

	/* return summary or txtnam */
	this.getSummary = function(model) {
		var val = this.getItem(model, "txtworkflowsummary");
		if (!val)
			url = this.getItem(model, "txtname");
		return val;
	}
};
Worklist.prototype = new ItemCollection();

/* WorklistController */
var Workitem = function() {
	this.id = '';
	this.entity = null;

	/* return all items sorted by name */
	this.getSortedItemlist = function() {

		return this.entity.item.sort(function(a, b) {
			if (a.name > b.name)
				return 1;
			else if (a.name < b.name)
				return -1;
			else
				return 0;
		});
	}
};
Workitem.prototype = new ItemCollection();

/*******************************************************************************
 * 
 * CONTROLLERS
 * 
 ******************************************************************************/

var restServiceController = benJS.createController("restServiceController",
		new RestService());
var worklistController = benJS.createController("worklistController",
		new Worklist());
var workitemController = benJS.createController("workitemController",
		new Workitem());

restServiceController.connect = function() {
	this.pull();
	QueryRoute.route();
}
/* Custom method to load a worklist */
worklistController.loadWorklist = function() {
	worklistController.pull();
	console.debug("load worklist: '" + worklistController.model.query + "'...");

	var url = restServiceController.model.baseURL;
	url = url + "/workflow/worklistbyquery/" + worklistController.model.query;
	url = url + "?start=" + worklistController.model.start + "&count="
			+ worklistController.model.count;

	$.ajax({
		type : "GET",
		url : url,
		dataType : "xml",
		success : function(response) {
			json = xml2json(response);

			worklistController.model.view = json.collection.entity;
			QueryRoute.route();
		},
		error : function(jqXHR, error, errorThrown) {

			message = errorThrown;
			$("#error-message").text(message);
			$("#imixs-error").show();
		}
	});

}

/* Custom method to load a single workite */
workitemController.loadWorkitem = function(context) {

	var entry = $('span', context);
	if (entry.length == 1) {

		var id = $(entry).text();

		workitemController.model.id = id;
	}

	console.debug("load workitem: '" + workitemController.model.id + "'...");

	var url = restServiceController.model.baseURL;
	url = url + "/workflow/workitem/" + workitemController.model.id;

	$.ajax({
		type : "GET",
		url : url,
		dataType : "xml",
		success : function(response) {
			json = xml2json(response);

			workitemController.model.entity = json.entity;
			WorkitemRoute.route();
		},
		error : function(jqXHR, error, errorThrown) {

			message = errorThrown;
			$("#error-message").text(message);
			$("#imixs-error").show();
		}
	});

}

/*******************************************************************************
 * 
 * ROUTES & TEMPLATES
 * 
 ******************************************************************************/
var RestServiceRoute = benJS.createRoute('restservice-route', {
	"content" : "view_restservice.html"
});

RestServiceRoute.beforeRoute.add(function(router) {
	restServiceController.pull();
});

RestServiceRoute.afterRoute.add(function(router) {
	$("#imixs-nav ul li").removeClass('active');
	$("#imixs-nav ul li:nth-child(1)").addClass('active');
});

var QueryRoute = benJS.createRoute('query-route', {
	"content" : "view_query.html"
});

QueryRoute.afterRoute.add(function(router) {
	$("#imixs-nav ul li").removeClass('active');
	$("#imixs-nav ul li:nth-child(2)").addClass('active');
});

var WorkitemRoute = benJS.createRoute('workitem-route', {
	"content" : "view_workitem.html"
});

WorkitemRoute.beforeRoute.add(function(router) {

});

WorkitemRoute.afterRoute.add(function(router) {
	$("#imixs-nav ul li").removeClass('active');
	$("#imixs-nav ul li:nth-child(1)").addClass('active');
});

var contentTemplate = benJS.createTemplate("content");
contentTemplate.afterLoad.add(layoutSection);

$(document).ready(function() {

	// start view
	benJS.start({
		"loadTemplatesOnStartup" : false
	});

	RestServiceRoute.route();
	$("#imixs-error").hide();

});