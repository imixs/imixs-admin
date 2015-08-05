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
	this.baseURL = "http://localhost:8080/backlog-rest";
};

/* WorklistController */
var Worklist = function() {
	this.query = "SELECT entity FROM Entity entity ORDER BY entity.modified DESC";
	this.view;
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
//	$.getJSON(url, function(data) {
//		console.debug("worklist loaded");
//		worklistController.model.view = data.entity;
//		//QueryRoute.route();
//
//	});

	$.ajax({
		type : "GET",
		url : url,
		dataType : "xml",
		success : function(response) {
		
			
			
			//json = $.xml2json(response);
			json=xml2json(response);
			
			// get only entity
//			json=json.collection;
//			console.log(json);
//			console.log("-- String --");
//			console.log(JSON.stringify(json));
//			
			
			worklistController.model.view=json.collection.entity;
			QueryRoute.route();
		},
		error : function() {
			alert("An error occurred while processing XML file.");
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

});