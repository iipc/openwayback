
function xResolveUrl(url) {
   var image = new Image();
   image.src = url;
   return image.src;
}
var xWaybackIsIE = (navigator.appName=="Microsoft Internet Explorer");
function xLateUrl(aCollection, sProp) {
   var i = 0;
   for(i = 0; i < aCollection.length; i++) {
      if(aCollection[i].getAttribute(sProp) &&
         (aCollection[i].getAttribute(sProp).length > 0) &&
         (typeof(aCollection[i][sProp]) == "string") &&
         (aCollection[i][sProp].indexOf("mailto:") == -1) &&
         (aCollection[i][sProp].indexOf("javascript:") == -1) &&
	 (aCollection[i][sProp].indexOf(sWayBackCGI) == -1) ) {

            var wmSpecial = aCollection[i].getAttribute("wmSpecial");
            if((wmSpecial && wmSpecial.length > 0)) {
            } else {
              var newUrl;
              if(aCollection[i][sProp].indexOf("http") == 0) {
	        newUrl = sWayBackCGI + aCollection[i][sProp];
              } else {
                newUrl = sWayBackCGI + xResolveUrl(aCollection[i][sProp]);
              }
              if(navigator.appName=="Microsoft Internet Explorer") {
                var inTmp = aCollection[i].innerHTML;
                aCollection[i][sProp] = newUrl;
		if(inTmp && 
		   ( (inTmp.indexOf("@") > 0) 
		     || (inTmp.indexOf("www.") == 0)
		     || (inTmp.indexOf("http://") == 0)
		   )
		   ) {
                  aCollection[i].innerHTML = inTmp;
                }
              } else {
                aCollection[i][sProp] = newUrl;
              }
            }
      }
   }
}

xLateUrl(document.getElementsByTagName("IMG"),"src");
xLateUrl(document.getElementsByTagName("A"),"href");
xLateUrl(document.getElementsByTagName("AREA"),"href");
xLateUrl(document.getElementsByTagName("OBJECT"),"codebase");
xLateUrl(document.getElementsByTagName("OBJECT"),"data");
xLateUrl(document.getElementsByTagName("APPLET"),"codebase");
xLateUrl(document.getElementsByTagName("APPLET"),"archive");
xLateUrl(document.getElementsByTagName("EMBED"),"src");
xLateUrl(document.getElementsByTagName("IFRAME"),"src");
xLateUrl(document.getElementsByTagName("INPUT"),"src");
xLateUrl(document.getElementsByTagName("BODY"),"background");
var forms = document.getElementsByTagName("FORM");
if (forms) {
		var j = 0;
		for (j = 0; j < forms.length; j++) {
			f = forms[j];
			if (typeof(f.action)  == "string") {
				if(typeof(f.method)  == "string") {
					if(typeof(f.method) != "post") {
						var resolved = "";
						var orig = f.action;
						if(f.action.indexOf("http") == 0) {
							resolved = f.action;
						} else {
							resolved = xResolveUrl(f.action);
						}
						// this does not work on firefox...
				    	f.action = sWayBackCGI + resolved;
				    }
				}
			}
		}
}
var interceptRunAlready = false;
function intercept_js_href_iawm(destination) {
	if(!interceptRunAlready &&top.location.href != destination) {
		interceptRunAlready = true;
		top.location.href = sWayBackCGI+xResolveUrl(destination);
	}
} 
// ie triggers
href_iawmWatcher = document.createElement("a");
top.location.href_iawm = top.location.href;
if(href_iawmWatcher.setExpression) {
	href_iawmWatcher.setExpression("dummy","intercept_js_href_iawm(top.location.href_iawm)");
}
// mozilla triggers
function intercept_js_moz(prop,oldval,newval) {
	intercept_js_href_iawm(newval);
	return newval;
}
if(top.location.watch) {
	top.location.watch("href_iawm",intercept_js_moz);
}
