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
		if(top.document.body.tagName == "BODY") {
			return;
		}
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
