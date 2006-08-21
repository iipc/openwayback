
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

            var resolved = "";
            if(aCollection[i][sProp].indexOf("http") == 0) {
                resolved = encodeURIComponent(aCollection[i][sProp]);
            } else {
                resolved = encodeURIComponent(xResolveUrl(aCollection[i][sProp]));
            }
            if(sProp == "href") {
               aCollection[i]["target"] = "_top";
               aCollection[i][sProp] = sWayBackFramesetCGI + resolved;
            } else {
               aCollection[i][sProp] = sWayBackReplayCGI + resolved;
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
						f.target = "_top";
						// this does not work on firefox...
					    	f.action = sWayBackFramesetCGI + resolved;
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

var notice = 
     "<div style='" +
     "position:relative;z-index:99999;"+
     "border:1px solid;color:black;background-color:lightYellow;font-size:10px;font-family:sans-serif;padding:5px'>" + 
     wmNotice +
  	 " [ <a style='color:blue;font-size:10px;text-decoration:underline' href=\"javascript:void(top.disclaimElem.style.display='none')\">" + wmHideNotice + "</a> ]" +
     "</div>";

function getFrameArea(frame) {
	if(frame.innerWidth) return frame.innerWidth * frame.innerHeight;
	if(frame.document.documentElement && frame.document.documentElement.clientHeight) return frame.document.documentElement.clientWidth * frame.document.documentElement.clientHeight;
	if(frame.document.body) return frame.document.body.clientWidth * frame.document.body.clientHeight;
	return 0;
}

function disclaim() {
	if(top!=self) {
		largestArea = 0;
		largestFrame = null;
		for(i=0;i<top.frames.length;i++) {
			frame = top.frames[i];
			area = getFrameArea(frame);
			if(area > largestArea) {
				largestFrame = frame;
				largestArea = area;
			}
		}
		if(self!=largestFrame) {
			return;
		}
	}
	disclaimElem = document.createElement('div');
	disclaimElem.innerHTML = notice;
	top.disclaimElem = disclaimElem;
	document.body.insertBefore(disclaimElem,document.body.firstChild);
}
disclaim();
