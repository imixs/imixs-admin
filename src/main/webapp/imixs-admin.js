// Imixs-Admin Core API
// V 1.0

	var imixs = IMIXS.org.imixs.core, 
				imixsXML = IMIXS.org.imixs.xml;
			
	// ImixsDoc represents a document class with a dynamic list of items.
	// This class is used to display the search result
//	var ImixsDoc = class {
//		  constructor(items) {
//			 if (items) {
//			   var self=this;
//			   $.each(items, function (j, _item) {
//				  var _name=_item.name;
//				  var _val=_item.values[0].text;
//      			  self[_name]=_val;
//      		  });
//		    }
//		  }
//		};
		
// INIT vue
$(document).ready(function() {	
	var app = new Vue({
	  el: '#app',
	  data: {
		connection_status: 0,
		api: 'https://demo.office-workflow.de/api',
		token: '',
		info: '',
		auth_method: 'form',
		auth_secret: '',
		auth_userid: '',
		index_fields: '',
		index_fields_analyze: '',
		index_fields_noanalyze: '',
		index_fields_store: '',
		query:'(type:*)',
		page_size:30,
		page_index:0,
		sort_by:'$modified',
		sort_order:"DESC",
		search_result:  [
				
	    ],
	    message: 'Test'
	    },
	    
	  created () {
		    console.log("...imixs-admin started");
	  },
	 
	  methods: {
	    // connect api endpoint
		apiConnect: function (event) { 
			var requestURL='/api/connect';
			var connectionData=new imixs.ImixsDocument();
	    	connectionData.setItem('api',app.api);
	    	connectionData.setItem('authmethod',app.auth_method);
	    	connectionData.setItem('userid',app.auth_userid);
	    	connectionData.setItem('secret',app.auth_secret);
			
	    	// convert to xml
	    	var xmlData = imixsXML.json2xml(connectionData);
	    	
	    	app.token='';
		    console.log("...connecting '" +requestURL + "'...");
		    $("#imixs-content").addClass("loading");
            	$.ajax({		            		
                    url: requestURL,
                    type: 'POST',
                    data: xmlData,
                    dataType: 'xml',
                    crossDomain:true,
                    contentType: 'application/xml',
                    success: function (response) { 
                    	app.connection_status=200;
                    	// convert rest response to a document instance
                    	//workitem=new imixs.ImixsDocument(imixsXML.xml2document(response));
                    	workitem=imixsXML.xml2document(response);
                    	app.token=workitem.getItem('token');
                    	console.log("token="+app.token);
                    	app.index_fields=workitem.getItemList('lucence.fulltextfieldlist');
                    	app.index_fields_analyze=workitem.getItemList('lucence.indexfieldlistanalyze');
                    	app.index_fields_noanalyze=workitem.getItemList('lucence.indexfieldlistnoanalyze');
                    	app.index_fields_store=workitem.getItemList('lucence.indexfieldliststore');
                    	
                    	showSection('search');
                    	$("#imixs-content").removeClass("loading");
                    },
                    error : function (xhr, ajaxOptions, thrownError){
                    	$("#imixs-content").removeClass("loading");
                    	app.connection_status=xhr.status;
                    	app.output=xhr.statusText;
                        console.log(xhr.status);          
                        console.log(thrownError);
                    }
                });
            	
            	
	    	
		    },
		    
		    
		    
		    
		 // connect api endpoint
			search: function (event) {
				var requestURL='/api/search';
		    	var requestData=new imixs.ImixsDocument();
		    	requestData.setItem('api',app.api);
		    	requestData.setItem('query',app.query);
		    	requestData.setItem('pagesize',app.page_size);
		    	requestData.setItem('pageindex',app.page_index);
		    	requestData.setItem('sortby',app.sort_by);
		    	requestData.setItem('sortorder',app.sort_order);
		    	// convert to xml
		    	var xmlData = imixsXML.json2xml(requestData);
		    	$("#imixs-content").addClass("loading");
	            	$.ajax({		            		
	                    url: requestURL,
	                    type: 'POST',
	                    beforeSend: function (xhr) {
	                        xhr.setRequestHeader('Authorization', app.token);
	                    },
	                    data: xmlData,
	                    dataType: 'xml',
	                    crossDomain:true,
	                    contentType: 'application/xml',
	                    success: function (response) {
	                    	app.connection_status=200;
	                    	// convert rest response to a document instance
	                    	//var liste=imixsXML.xml2collection(response);
	                    	app.search_result=imixsXML.xml2collection(response);
	                    	$("#imixs-content").removeClass("loading");
	                    },
	                    error : function (xhr, ajaxOptions, thrownError){
	                    	$("#imixs-content").removeClass("loading");
	                    	app.connection_status=xhr.status;
	                    	app.output=xhr.statusText;
	                        console.log(xhr.status);          
	                        console.log(thrownError);
	                    }
	                });
	            },
		    
		    
	         // open a document by its id
			 openDocument: function (event, doc) {
				 
			    	alert(doc.getItem('$uniqueid'));
			 },
			    
		    
		    
		    // invalidate token
		    logout: function (event) {
		    	app.workitem=null;
		    	app.token=null;
		    	app.connection_status=0;
		    	app.api='';
		    	app.auth_secret= '';
		    	app.auth_userid= '';
		    	showSection('connect');
		    },
		    
		    
		    // method to simulate click on cacel ($event=90)
		    submitCancel: function (event) {
		    	alert('cancel');
		    }
		    
	    }
	  
	});
	
	
	// show connect
	showSection('connect');
	
	
});

	

	// hides all panels and shows only the given form-panel
	showSection = function (section) {
		$('.form-section').hide();
		$('#'+section).show();
		$('textarea, input','#'+section).first().focus();
	}
	
	var toggleState = false;
	togglemenu = function() {
		if (!toggleState) {
	
			$('.nav-sidebar label').hide();
			$('.content').css('margin-left', '60px');
			$('.nav-sidebar').css('width', '60px');
	
		} else {
			$('.content').css('margin-left', '200px');
			$('.nav-sidebar').css('width', '200px');
			$('.nav-sidebar label').show();
		}
	
		toggleState = !toggleState;
	
	};