// Imixs-Admin Core API
// V 1.0

	var imixs = IMIXS.org.imixs.core, 
				imixsXML = IMIXS.org.imixs.xml;
			

		
// INIT vue
$(document).ready(function() {	
	

	
	var app = new Vue({
	  el: '#app',
	  data: {
		connection_status: 0,
		api: 'http://imixssample-app:8080/api',
		token: '',
		info: '',
		auth_method: 'basic',
		auth_secret: 'adminadmin',
		auth_userid: 'admin',
		index_fields: '',
		index_fields_analyze: '',
		index_fields_noanalyze: '',
		index_fields_store: '',
		query:'(type:*)',
		query_short:'',
		page_size:25, 
		page_index:0,
		sort_by:'$modified',
		sort_order:"DESC",
		search_result: [],
	    document: new imixs.ImixsDocument(),
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
                    contentType: 'application/xml',
                    success: function (response) { 
                    	app.connection_status=200;
                    	// convert rest response to a document instance
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
		    
		    
		    
		    
		 // search query
			search: function (event) {
				if (app.token=='') {
					return;
				}
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
   				    var requestURL='/api/documents/'+doc.getItem('$uniqueid');
			    	$("#imixs-content").addClass("loading");
		            	$.ajax({		            		
		                    url: requestURL,
		                    type: 'GET',
		                    beforeSend: function (xhr) {
		                        xhr.setRequestHeader('Authorization', app.token);
		                    },
		                    contentType: 'application/xml',
		                    success: function (response) {
		                    	app.connection_status=200;
		                    	// convert rest response to a document instance
		                    	app.document=imixsXML.xml2document(response);	
		                    	showSection('document');
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
			    
		    
			 // forward
			 searchForward: function () {
			    	app.page_index=app.page_index+1;
			    	app.search();
  		     },
			 searchRewind: function () {
				 if (app.page_index>0) {
				    	app.page_index=app.page_index-1;
				    	app.search();
				 }
		     },
		     
		     // search from top nav
		     quickSearch: function () {
		    	 app.query="(" + app.query_short.toLowerCase() + ")";
		    	 app.search();
		     },
		     
		     
		     // open document via rest api
		     linkDocument: function ( event, doc) {
		    	 window.open(app.api + '/documents/'+doc.getItem('$uniqueid'))
		     },
		     
		     
		     // Delete a single document
		     deleteDocument: function ( event, doc) {
		    	 if (!confirm('Are you sure?\n\nDelete Document: \n\n' + doc.getItem('$uniqueid'))) {
						return false;
					}
					// delete...
		    	    var requestURL='/api/documents/'+doc.getItem('$uniqueid');
			    	$("#imixs-content").addClass("loading");
		            	$.ajax({		            		
		                    url: requestURL,
		                    type: 'DELETE',
		                    beforeSend: function (xhr) {
		                        xhr.setRequestHeader('Authorization', app.token);
		                    },
		                    contentType: 'application/xml',
		                    success: function (response) {
		                    	app.connection_status=200;
		                    	app.search();
		                    	
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
	
	$("#quick_search").on('keyup', function (e) {
	    if (e.keyCode === 13) {
	        app.quickSearch();
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