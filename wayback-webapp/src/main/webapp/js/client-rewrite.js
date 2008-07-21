
function xResolveUrl(url) {
   var image = new Image();
   image.src = url;
   return image.src;
}
function xLateUrl(aCollection, sProp) {
   var i = 0;
   for(i = 0; i < aCollection.length; i++) {
      if(aCollection[i].getAttribute(sProp) &&
         (aCollection[i].getAttribute(sProp).length > 0) &&
         (typeof(aCollection[i][sProp]) == "string")) {

         if(aCollection[i][sProp].indexOf("mailto:") == -1 &&
            aCollection[i][sProp].indexOf("javascript:") == -1) {

            var wmSpecial = aCollection[i].getAttribute("wmSpecial");
            if(wmSpecial && wmSpecial.length > 0) {
            } else {
                if(aCollection[i][sProp].indexOf(sWayBackCGI) == -1) {
                    if(aCollection[i][sProp].indexOf("http") == 0) {
                        aCollection[i][sProp] = sWayBackCGI + aCollection[i][sProp];
                    } else {
                        aCollection[i][sProp] = sWayBackCGI + xResolveUrl(aCollection[i][sProp]);
                    }
                }
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
