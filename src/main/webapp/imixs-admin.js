// Imixs-Admin Core API
// V 1.0

	var imixs = IMIXS.org.imixs.core, 
				imixsXML = IMIXS.org.imixs.xml;
			
			
// INIT
$(document).ready(function() {	
	var app = new Vue({
	  el: '#app',
	  data: {
		connection_status: 0,
		api: '',
		info: '',
		auth_method: 'form',
		auth_secret: '',
		auth_userid: '',
	    message: 'Test'
	    },
	    
	  created () {
		    console.log("servus");
	  },
	 
	  methods: {
		  
		    apiConnect: function (event) {
		      // something
		    	
		    	var requestURL='/api/connect';
		    	
		    	
		    	var connectionData=new imixs.ItemCollection();
		    	connectionData.setItem('api',app.api);
		    	connectionData.setItem('authmethod',app.auth_method);
		    	connectionData.setItem('userid',app.auth_userid);
		    	connectionData.setItem('secret',app.auth_secret);
		    	// convert to xml
		    	var xmlData = imixsXML.json2xml(connectionData);
		    	
		    	
			    console.log("...connecting '" +requestURL + "'...");

	            	$.ajax({		            		
	                    url: requestURL,
	                    type: 'POST',
	                    data: xmlData,
	                    dataType: 'xml',
	                    crossDomain:true,
	                    contentType: 'application/xml',
	                    success: function (response) {
	                    	
	                    	app.connection_status=200;
	                    	// output=response.msg;
	                    	
	                    	
	                    	
	                    	// convert rest response to a document instance
	                    	workitem=new imixs.ItemCollection(imixsXML.xml2document(response));
	                    	
	                    	var token=workitem.getItem('token');
	                    	console.log("token="+token);
	                    	//app.updateHeadUnit(workitem);
	                    },
	                    error : function (xhr, ajaxOptions, thrownError){
	                    	app.connection_status=xhr.status;
	                    	app.output=xhr.statusText;
	                        console.log(xhr.status);          
	                        console.log(thrownError);
	                    }
	                    //JSON.stringify(result);
	                });
		    	
		    	
		    	
		    	
		    	
		    	
		    	
		    	
		    },
		    
		    // method to simulate click on cacel ($event=90)
		    submitCancel: function (event) {
		    	alert('cancel');
		    }
		    
	    }
	  
	});
	
	
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