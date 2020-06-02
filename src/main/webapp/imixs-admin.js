// Imixs-Admin Core API
// V 1.1

	var imixs = IMIXS.org.imixs.core, 
				imixsXML = IMIXS.org.imixs.xml;
			

		
// INIT vue
$(document).ready(function() {	
	
	
	var app = new Vue({
	  el: '#app',
	  data: {
		connection_status: 0,
		api: '',
		token: '',
		info: '',
		auth_method: '',
		auth_secret: '',
		auth_userid: '',
		index_fields: '',
		index_fields_analyze: '',
		index_fields_noanalyze: '',
		index_fields_store: '',
		query:'(type:workitem)',
		query_short:'',
		page_size:25, 
		page_index:0,
		sort_by:'$modified',
		sort_order:"DESC",
		search_result: [],
		adminp_jobs: [],
		models: [],
		models_upload: [],
	    document: new imixs.ImixsDocument(),
	    update_fieldname:'',
	    update_fieldtype:'',
	    update_append:'',
	    update_values:'',
	    update_event:'',
	    filepath: 'backup_'+formatDate(),
	    adminp_job: '',
	    adminp_interval: '',
	    adminp_blocksize: '',
	    adminp_from: '',
	    adminp_to: '',
	    adminp_filter: '',
	    adminp_userfrom: '',
	    adminp_userto:'',
	    adminp_userreplace: '',
	    message: '',
	    error: '',
	  },
	    
	  created () {
		    console.log("...imixs-admin started");
		 
	  },
	 
	  methods: {
		  
		  // returns the api endpoint without ending /
		  getEndpoint: function () {
			  var e=location.href;
			  // strip #
			  if (e.indexOf('#')>-1) {
				  e=e.substring(0,e.indexOf('#')-1);
			  }
			  if (e.endsWith('/')) {
				  e=e.substring(0,e.length-1);
			  }
			  return e;
		  },
		   
		  // connect api endpoint
		  apiConnect: function (event) { 			
			var requestURL=app.getEndpoint() + '/api/connect';
			var connectionData=new imixs.ImixsDocument();
			// trim api
			app.api=app.api.trim();
	    	connectionData.setItem('api',app.api);
	    	connectionData.setItem('authmethod',app.auth_method);
	    	connectionData.setItem('userid',app.auth_userid);
	    	connectionData.setItem('secret',app.auth_secret);
			
	    	// convert to xml
	    	var xmlData = imixsXML.json2xml(connectionData);
	    	
	    	app.message='';
	    	app.error='';
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
                    	//console.log("token="+app.token);
                    	app.index_fields=workitem.getItemList('lucence.fulltextfieldlist');
                    	app.index_fields_analyze=workitem.getItemList('lucence.indexfieldlistanalyze');
                    	app.index_fields_noanalyze=workitem.getItemList('lucence.indexfieldlistnoanalyze');
                    	app.index_fields_store=workitem.getItemList('lucence.indexfieldliststore');
                    	
                    	app.showSection('search');
                    	$("#imixs-content").removeClass("loading");
                    	
                    	// store api endpoint
                    	localStorage.setItem("adminclient.api",app.api);
                    },
                    error : function (xhr, ajaxOptions, thrownError){
                    	$("#imixs-content").removeClass("loading");
                    	app.connection_status=xhr.status;
                    	app.output=xhr.statusText;
                        console.log(xhr.status);          
                        console.log(thrownError);
                        app.error='Failed to connect: ' + thrownError;
                    }
                });
		  },
		    
		    
		    
		    
		  // search query
		  search: function (event) {
			if (app.token=='') {
				return;
			}
	    	app.message='';
	    	app.error='';
			var requestURL=app.getEndpoint() + '/api/search';
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
	                    app.error=xhr.status+ " " + thrownError;
	                }
	            });
		  },
	    
	    
		  // open a document by its id
		  openDocument: function (event, doc) {
	    	app.message='';
	    	app.error='';
		    var requestURL=app.getEndpoint() + '/api/documents/'+doc.getItem('$uniqueid');
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
                    	app.showSection('document');
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
			    
			 
			 
	    
		  // Bulk Update
		  bulkUpdate: function (event) {
			if (app.token=='') {
				return;
			}
			if (!confirm('Staring Bulk Update now?\n\nOperation can not be undone!')) {
				return false;
			}
	    	app.message='';
	    	app.error='';
			
			var requestURL=app.getEndpoint() + '/api/update';
	    	var requestData=new imixs.ImixsDocument();
	    	requestData.setItem('api',app.api);
	    	requestData.setItem('query',app.query);
	    	requestData.setItem('pagesize',app.page_size);
	    	requestData.setItem('pageindex',app.page_index);
	    	requestData.setItem('sortby',app.sort_by);
	    	requestData.setItem('sortorder',app.sort_order);
	    	
	    	requestData.setItem('fieldname',app.update_fieldname);
	    	requestData.setItem('fieldtype',app.update_fieldtype);
	    	requestData.setItem('append',app.update_append);
	    	requestData.setItem('values',app.update_values);
	    	requestData.setItem('event',app.update_event);

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
                    	workitem=imixsXML.xml2document(response);		                    	
                    	app.message=workitem.getItem('message');
                    	$("#imixs-content").removeClass("loading");
                    	//app.search();
                    	app.search_result=new Array();
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
	    
		  // Bulk Delete
		  bulkDelete: function (event) {
				if (app.token=='') {
					return;
				}
				if (!confirm('Staring Bulk Delete now?\n\nOperation can not be undone!')) {
					return false;
				}
		    	app.message='';
		    	app.error='';
				
				var requestURL=app.getEndpoint() + '/api/delete';
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
                    	workitem=imixsXML.xml2document(response);		                    	
                    	app.message=workitem.getItem('message');	           
                    	$("#imixs-content").removeClass("loading");
                    	//app.search();
                    	app.search_result=new Array();
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
		    		   
	            
	            
        
		  // adminP
		  adminP: function () {
   			if (app.token=='') {
   				return;
   			}
   			if (!confirm('Staring AdminP Job ' + app.adminp_job + ' now?\n\nOperation can not be undone!')) {
   				return false;
   			}
   	    	app.message='';
   	    	app.error='';
   			
   			var requestURL=app.getEndpoint() + '/api/adminp';
   	    	var requestData=new imixs.ImixsDocument();
   	    	requestData.setItem('api',app.adminp_job);
   	    	requestData.setItem('numinterval',app.adminp_interval);
   	    	requestData.setItem('blocksize',app.adminp_blocksize);
  	    	requestData.setItem('datfrom',app.adminp_from);
 	   	    requestData.setItem('datto',app.adminp_to);
   	    	requestData.setItem('typelist',app.adminp_filter);
   	    	
   	    	requestData.setItem('namfrom',app.adminp_userfrom);
   	    	requestData.setItem('namto',app.adminp_userto);
   	    	requestData.setItem('keyreplace',app.adminp_userreplace);
    	    	
   	    	requestData.setItem('job',app.adminp_job);
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
                         $("#imixs-content").removeClass("loading");
                         app.loadAdminpJobs();
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
		  loadAdminpJobs: function () {
       			if (app.token=='') {
       				return;
       			}
       	    	app.message='';
       	    	app.error='';
       			var requestURL=app.getEndpoint() + '/api/jobs';
       	    	var requestData=new imixs.ImixsDocument();
       	    	requestData.setItem('api',app.api);
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
       	                	app.adminp_jobs=imixsXML.xml2collection(response);
       	                	$("#imixs-content").removeClass("loading");
       	                },
       	                error : function (xhr, ajaxOptions, thrownError){
       	                	$("#imixs-content").removeClass("loading");
       	                	app.connection_status=xhr.status;
       	                    app.error=xhr.status+ " " + thrownError;
       	                }
       	            });
		  	},
       	    
       	    
            
		  	// Import 
			dataImport: function (e) {
				if (app.token=='') {
					return;
				}
				if (!confirm('Staring Import now?\n\nOperation can take some time!')) {
					return false;
				}
		    	app.message='';
		    	app.error='';
				
				var requestURL=app.getEndpoint() + '/api/import';
		    	var requestData=new imixs.ImixsDocument();
		    	requestData.setItem('api',app.api);
		    	requestData.setItem('filepath',app.filepath);

		    	// convert to xml
		    	var xmlData = imixsXML.json2xml(requestData);
		    	$("#imixs-content").addClass("loading");
	            	$.ajax({		            		
	                    url: requestURL,
	                    type: 'PUT',
	                    beforeSend: function (xhr) {
	                        xhr.setRequestHeader('Authorization', app.token);
	                    },
	                    data: xmlData,
	                    dataType: 'xml',
	                    contentType: 'application/xml',
	                    success: function (response) {
	                    	app.connection_status=200;		           
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
            
			            
	            //  Export
				dataExport: function () {
					if (app.token=='') {
						return;
					}
					if (!confirm('Staring Export now?\n\nOperation can take some time!')) {
						return false;
					}
			    	app.message='';
			    	app.error='';
					
					var requestURL=app.getEndpoint() + '/api/export';
					
			    	var requestData=new imixs.ImixsDocument();
			    	requestData.setItem('api',app.api);
			    	requestData.setItem('query',app.query);
			    	requestData.setItem('filepath',app.filepath);

			    	// convert to xml
			    	var xmlData = imixsXML.json2xml(requestData);
			    	$("#imixs-content").addClass("loading");
		            	$.ajax({		            		
		                    url: requestURL,
		                    type: 'PUT',
		                    beforeSend: function (xhr) {
		                        xhr.setRequestHeader('Authorization', app.token);
		                    },
		                    data: xmlData,
		                    dataType: 'xml',
		                    contentType: 'application/xml',
		                    success: function (response) {
		                    	app.connection_status=200;		           
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
		    	 app.showSection('search');
		    	 app.query="(" + app.query_short.toLowerCase() + ")";
		    	 app.search();
		     },
		     
		     
		     // open document via rest api
		     linkDocument: function ( event, doc) {
		    	 window.open(app.api + '/documents/'+doc.getItem('$uniqueid'))
		     },
		     
		     // open a bpmn model via rest api
		     linkModel: function ( event, doc) {
		    	 window.open(app.api + '/model/' + doc.getItem('txtname') + '/bpmn/')
		     },
		     
		     
		     // extracts the first filename of an attachment 
		     getFileName: function (  doc) {
		    	 var file=doc['$file'];
		    	 if (file && file[0]) {
		    		return file[0].item.name;
		    	 }
		    	 return '-- undefined --';
		     },
		     
		     
		     // Delete a single document
		     deleteDocument: function ( event, doc) {
		    	 if (!confirm('Are you sure?\n\nDelete Document: \n\n' + doc.getItem('$uniqueid'))) {
						return false;
					}
			    	app.message='';
			    	app.error='';

					// delete...
		    	    var requestURL=app.getEndpoint() + '/api/documents/'+doc.getItem('$uniqueid');
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
		     
		     
		     // Delete a single document
		     deleteAdminPJob: function ( event, doc) {
		    	 if (!confirm('Are you sure?\n\nDelete Document: \n\n' + doc.getItem('$uniqueid'))) {
						return false;
					}
			    	app.message='';
			    	app.error='';

					// delete...
		    	    var requestURL=app.getEndpoint() + '/api/documents/'+doc.getItem('$uniqueid');
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
		                    	app.loadAdminpJobs();
		                    	
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
		     
		     
		     // Delete a single document
		     deleteModel: function ( event, version) {
		    	if (!confirm('Are you sure?\n\nDelete Model Version: \n\n' + version)) {
						return false;
				}
		    	app.message='';
		    	app.error='';

				// delete...
	    	    var requestURL=app.getEndpoint() + '/api/model/'+version;
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
	                    	app.loadModels();
	                    	
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
		     
		     
		     
		     // upload one or many model files 
	   		 uploadModel: function () {
	   			if (app.token=='' || app.models_upload.length==0) {
	   				return;
	   			}
	   	    	app.message='';
	   	    	app.error='';
	   			var requestURL=app.getEndpoint() + '/modelupload';
	   	    	$("#imixs-content").addClass("loading");
	   	    	var uploaderForm = new FormData(); // Create new FormData
	   	    	$.each(app.models_upload, function (index, file) {		
	   	    		uploaderForm.append("file", file);
	   	    	});
               	$.ajax({		            		
                       url: requestURL,
                       type: 'POST',
                       beforeSend: function (xhr) {
                           xhr.setRequestHeader('Authorization', app.token);
                       },
                       data: uploaderForm,
                       dataType: 'xml',
                       mimeType: 'multipart/form-data', // this too
                       contentType: false,
                       cache: false,
                       processData: false,
                       success: function (response) {
                         app.connection_status=200;		           
                       	 app.models_upload=[]; 
                       	 $("#model-input").val('');
                       	 $('#model-file-list-display').empty();
        	   	    	 app.loadModels();
                       },
                       error : function (xhr, ajaxOptions, thrownError){
                       	 app.connection_status=xhr.status;
                       	 app.output=xhr.statusText;
                         console.log(xhr.status);          
                         console.log(thrownError);
                       }
                 });
	   	    	 $("#imixs-content").removeClass("loading");
	   		 },	
	               
	               
	   		 // cancel upload model
	   		 cancelUploadModel: function () {
	  	   			 app.models_upload=[];
	  	   			 $('#model-file-list-display').empty();
	  	   			 $("#model-input").val('');
	   		 },
	               
		    
		    
	   		 // method to simulate click on cancel ($event=90)
	   		 loadModels: function () {	    	
		    	app.showSection('models');
		    	app.message='';
		    	app.error='';
		    	var requestData=new imixs.ImixsDocument();
       	    	requestData.setItem('api',app.api);
       	    	// convert to xml
       	    	var xmlData = imixsXML.json2xml(requestData);
				// delete...
	    	    var requestURL=app.getEndpoint() + '/api/model';
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
	   	                	app.models=imixsXML.xml2collection(response);
	   	                	// sort result by model version
	   	                	app.models.sort(imixs.compareDocuments('txtname'));
	   	                	$("#imixs-content").removeClass("loading");
	   	                },
	   	                error : function (xhr, ajaxOptions, thrownError){
	   	                	$("#imixs-content").removeClass("loading");
	   	                	app.connection_status=xhr.status;
	   	                    app.error=xhr.status+ " " + thrownError;
	   	                }
	   	            });
	   		 },
	   		 
             
	   		 // log out and invalidate token
	   		 logout: function (event) {
		    	app.workitem=null;
		    	app.token=null;
		    	app.connection_status=0;
		    	app.api='';
		    	app.auth_secret= '';
		    	app.auth_userid= '';
		    	app.showSection('connect');
	   		 },
	   		 
	   		 
	   		 // hides all panels and shows only the given form-panel
	   		 showSection : function (section) {
				app.message='';
		    	app.error='';
				$('.form-section').hide();
				$('#'+section).show();
				$('textarea, input[type=text], select','#'+section).first().focus();
				
				// load jobs in case of adminp
				if (section=='adminp') {
					app.loadAdminpJobs();
				}
			}
	    }
	  
	});
	
	$("#quick_search").on('keyup', function (e) {
	    if (e.keyCode === 13) {
	        app.quickSearch();
	    }
	});
	
	
	$("#token-input, #password-input").on('keyup', function (e) {
	    if (e.keyCode === 13) {
	        app.apiConnect();
	    }
	});
	
	
	$("#model-input").on('change',function (e) {
		app.models_upload=[];
		var fileListDisplay = $('#model-file-list-display');
		fileListDisplay.empty();
		$.each(this.files, function (index, file) {			
			if (file.name.endsWith('.bpmn')) {
				app.models_upload.push(file);
				$(fileListDisplay).append( "<p>" + (index+1) + ". " + file.name +"</p>");
			} else {
				$(fileListDisplay).append( "<p style='color:red;'>" + (index+1) + ". " + file.name + " - invalid file type!</p>" );
			}
			
		});
	});
	
	// load last api endpoint from local storage
    app.api=localStorage.getItem("adminclient.api");
    console.log("...last api endpoint="+app.api);
	// show connect
	app.showSection('connect');
	
	
});

	

	
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
	
	
	formatDate = function() {
	    var d = new Date(),
	        month = '' + (d.getMonth() + 1),
	        day = '' + d.getDate(),
	        year = d.getFullYear(),
	        hour='' +d.getHours(),
	        minute='' +d.getMinutes(),
	        second='' +d.getSeconds();
	    
	    	

	    if (month.length < 2) 
	        month = '0' + month;
	    if (day.length < 2) 
	        day = '0' + day;

	    if (hour.length < 2) 
	    	hour = '0' + hour;
	    if (minute.length < 2) 
	    	minute = '0' + minute;
	    if (second.length < 2) 
	    	second = '0' + second;
	    
	    return [year, month, day,hour,minute,second].join('');
	}
	
	

	

	