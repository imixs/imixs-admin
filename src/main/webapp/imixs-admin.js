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
                    	console.log("token="+app.token);
                    	app.index_fields=workitem.getItemList('lucence.fulltextfieldlist');
                    	app.index_fields_analyze=workitem.getItemList('lucence.indexfieldlistanalyze');
                    	app.index_fields_noanalyze=workitem.getItemList('lucence.indexfieldlistnoanalyze');
                    	app.index_fields_store=workitem.getItemList('lucence.indexfieldliststore');
                    	
                    	app.showSection('search');
                    	$("#imixs-content").removeClass("loading");
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
	                    app.error=xhr.status+ " " + thrownError;
	                }
	            });
	        },
	    
	    
         // open a document by its id
		 openDocument: function (event, doc) {
	    	app.message='';
	    	app.error='';
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
			
			var requestURL='/api/update';
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
				
				var requestURL='/api/delete';
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
   			
   			var requestURL='/api/adminp';
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
       			var requestURL='/api/jobs';
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
				
				var requestURL='/api/import';
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
					
					var requestURL='/api/export';
					
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
			    	app.message='';
			    	app.error='';

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
		     
		     
		     // Delete a single document
		     deleteAdminPJob: function ( event, doc) {
		    	 if (!confirm('Are you sure?\n\nDelete Document: \n\n' + doc.getItem('$uniqueid'))) {
						return false;
					}
			    	app.message='';
			    	app.error='';

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
		    
		    // invalidate token
		    logout: function (event) {
		    	app.workitem=null;
		    	app.token=null;
		    	app.connection_status=0;
		    	app.api='';
		    	app.auth_secret= '';
		    	app.auth_userid= '';
		    	app.showSection('connect');
		    },
		    
		    
		    // method to simulate click on cacel ($event=90)
		    submitCancel: function (event) {
		    	alert('cancel');
		    },
		    
		    

			// hides all panels and shows only the given form-panel
			showSection : function (section) {
				app.message='';
		    	app.error='';
				$('.form-section').hide();
				$('#'+section).show();
				$('textarea, input, select','#'+section).first().focus();
				
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
	
	
	
	
	