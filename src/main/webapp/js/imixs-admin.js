// Imixs-Admin Core API
// V 1.1

	// on Load Document we check the toggle status
	document.addEventListener('DOMContentLoaded', function() {
	    toggleCookie=getCookie('imixs-admin.toggleStatus');
	    if (toggleCookie=='hidden') {
			togglemenu();
		}
	}, false);

	
	var toggleState = false;
	togglemenu = function() {
		if (!toggleState) {
			document.querySelectorAll(".nav-sidebar label").forEach(el => el.style.display = "none");
    		document.querySelector(".content").style['margin-left'] = "60px";
			document.querySelector(".nav-sidebar").style['width'] = "60px";
			setCookie('imixs-admin.toggleStatus','hidden', 30);
		} else {
			document.querySelector(".content").style['margin-left'] = "200px";
			document.querySelector(".nav-sidebar").style['width'] = "200px";
			document.querySelectorAll(".nav-sidebar label").forEach(el => el.style.display = "inherit");
			setCookie('imixs-admin.toggleStatus','show', 30);
		}
		toggleState = !toggleState;
	
	};
	

	setCookie = function (cname, cvalue, exdays) {
	  const d = new Date();
	  d.setTime(d.getTime() + (exdays*24*60*60*1000));
	  let expires = "expires="+ d.toUTCString();
	  document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
	};
	
	getCookie = function (cname) {
	  let name = cname + "=";
	  let decodedCookie = decodeURIComponent(document.cookie);
	  let ca = decodedCookie.split(';');
	  for(let i = 0; i <ca.length; i++) {
	    let c = ca[i];
	    while (c.charAt(0) == ' ') {
	      c = c.substring(1);
	    }
	    if (c.indexOf(name) == 0) {
	      return c.substring(name.length, c.length);
	    }
	  }
	  return "";
	};

	