function ensureSmaller(smaller,larger,change) {
  if(smaller.value != "" && larger.value != "") {
    if(smaller.value > larger.value) {
      if(smaller == change) {
        smaller.value = larger.value;
      } else {
        larger.value = smaller.value;
      }
    }
  }
}
function mySetupCal(id,fn) {
  var dateFormatString = "%Y-%m-%d %H:%M:%S";
  Calendar.setup(
  {
    inputField  : id,
    ifFormat    : dateFormatString,
    showsTime   : true,
    timeFormat  : "24",
    onUpdate    : fn,
  });
}

function setupDateRangeCalendars(startField, endField) {
	var capStart = document.getElementById(startField);
	var capEnd = document.getElementById(endField);
	
	function ensureCapStart(cal) { ensureSmaller(capStart,capEnd,capEnd)   }
	function ensureCapEnd(cal)   { ensureSmaller(capStart,capEnd,capStart) }
	
	mySetupCal(startField,ensureCapStart);
	mySetupCal(endField,ensureCapEnd);
}