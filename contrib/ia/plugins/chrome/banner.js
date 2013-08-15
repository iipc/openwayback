
var BANNER_ID = "_wayback_banner";

var banner = document.getElementById(BANNER_ID);

if (!banner) {
  banner = document.createElement("div");
  banner.setAttribute("id", BANNER_ID);
  banner.style.cssText = "width: 100%; height: 100px; border: 1px; background-color: #eff; text-align: center";

  //banner.innerHTML = "<img src='http://wbgrp-svc112.us.archive.org:8080/images/logo_WM.png#wb_pass'/>";
  banner.innerHTML = "<h1>wayback plugin</h1>"

  document.body.insertBefore(banner, document.body.firstChild);
}
