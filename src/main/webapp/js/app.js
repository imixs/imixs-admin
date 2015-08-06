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

// WorklistView.prototype = new ItemCollection();

/*******************************************************************************
 * 
 * CONTROLLERS
 * 
 ******************************************************************************/

var restServiceController = benJS.createController("restServiceController",
		new RestService());
var worklistController = benJS.createController("worklistController",
		new Worklist());

var contentTemplate = benJS.createTemplate("content");
contentTemplate.afterLoad.add(layoutSection);

restServiceController.connect = function() {
	this.pull();
	QueryRoute.route();
}
/* Custom method to load a single project */
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

/*******************************************************************************
 * 
 * ROUTES
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

$(document).ready(function() {

	// start view
	benJS.start({
		"loadTemplatesOnStartup" : false
	});

	RestServiceRoute.route();
	$("#imixs-error").hide();

});