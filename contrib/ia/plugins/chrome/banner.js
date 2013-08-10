
var BANNER_ID = "_wayback_banner";

var banner = document.getElementById(BANNER_ID);

if (!banner) {
  banner = document.createElement("div");
  banner.setAttribute("id", BANNER_ID);
  banner.style.cssText = "width: 100%; border: 1px; background-color: #fef; text-align: center";

  banner.innerHTML = "<img src='https://archive.org/images/wayback.gif#wb_pass'/>";

  document.body.insertBefore(banner, document.body.firstChild);
}
