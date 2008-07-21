function getFrameArea(frame) {
  if(frame.innerWidth) return frame.innerWidth * frame.innerHeight;
  if(frame.document.documentElement && frame.document.documentElement.clientHeight) return frame.document.documentElement.clientWidth * frame.document.documentElement.clientHeight;
  if(frame.document.body) return frame.document.body.clientWidth * frame.document.body.clientHeight;
  return 0;
}

function disclaimElement(element) {
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
  element.style.display="block";
  document.body.insertBefore(element,document.body.firstChild);
}
