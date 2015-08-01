function layoutSection(templ, context) {
	// $(context).i18n();
	// $(context).imixsLayout();

};

/*******************************************************************************
 * 
 * MODELS
 * 
 ******************************************************************************/

var RestService = function() {
	this.baseURL;
};

/* WorklistController */
var Worklist= function() {
	this.query;
	this.view;
};

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

	var url=restServiceController.model.baseURL;
	url=url+"/workflow/";
	$.getJSON(url, function(data) {
		console.debug("worklist loaded");
		this.model.view = data;
		// change route...
	//	ProjectRoute.route();

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

});