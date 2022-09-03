// Imixs-Admin Core API
// V 1.1

	
	var toggleState = false;
	togglemenu = function() {
		if (!toggleState) {
			document.querySelectorAll(".nav-sidebar label").forEach(el => el.style.display = "none");
    		document.querySelector(".content").style['margin-left'] = "60px";
			document.querySelector(".nav-sidebar").style['width'] = "60px";
		} else {
			document.querySelector(".content").style['margin-left'] = "200px";
			document.querySelector(".nav-sidebar").style['width'] = "200px";
			document.querySelectorAll(".nav-sidebar label").forEach(el => el.style.display = "inherit");
		}
		toggleState = !toggleState;
	
	};