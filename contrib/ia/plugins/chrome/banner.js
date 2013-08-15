
var BANNER_ID = "_wayback_banner";

var banner = document.getElementById(BANNER_ID);

if (!banner) {
  banner = document.createElement("div");
  banner.setAttribute("id", BANNER_ID);
  banner.style.cssText = "width: 100%; border: 1px solid; background-color: lightYellow; text-align: center";

  //banner.innerHTML = "<img src='http://wbgrp-svc112.us.archive.org:8080/images/logo_WM.png#wb_pass'/>";
  banner.innerHTML = "<h1>Wayback Plugin</h1>"

  document.body.insertBefore(banner, document.body.firstChild);
}
