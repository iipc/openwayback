var waybackHost = "wayback.archive-it.org";
var waybackPrefix = "http://" + waybackHost + "/";
var waybackProxyPort = 8081;

var proxyColl = "3701";

var proxyMode = false;

var tabIdToUrl = {};

var archivalURLRegex = /([^/]*)\/(\d{1,14})([a-z]{2}_)*\/(.*)/;


function extractTopLevelInfo(url)
{
  var rest = url.substr(waybackPrefix.length);
  var parts = rest.match(archivalURLRegex);
  
  if (!parts) {
    return;
  }
  
  return {"fullWaybackUrl": url, "coll": parts[1], "timestamp": parts[2], "mod": parts[3], "url": parts[4]};
}


function makeReplayUrl(replayInfo, url)
{
  return waybackPrefix + replayInfo.coll + "/" + replayInfo.timestamp + "id_/" + url;
}


function isWaybackReplayTab(tabId)
{
  return (tabIdToUrl && tabIdToUrl[tabId]);
}


function archivalRedirect(details)
{  
  if (details.url.indexOf("#wb_pass") >= 0) {
    return {};
  }
    
  if (details.url.indexOf(waybackPrefix) >= 0) {
    if (details.type == "main_frame") {
      replayInfo = extractTopLevelInfo(details.url);
      
      if (replayInfo) {
        
        // Redirect to id_       
        if (!replayInfo.mod || replayInfo.mod == "") {
           //console.log(replayInfo.mod);
           return { redirectUrl: makeReplayUrl(replayInfo, replayInfo.url) };
        }
      
        tabIdToUrl[details.tabId] = replayInfo;
      }
    }
    return {};
  }
  
  if (details.url.indexOf("/crossdomain.xml") >= 0) {
    return {};
  }
  
  if (!isWaybackReplayTab(details.tabId)) {
    return {};
  }
      
  var replayInfo = tabIdToUrl[details.tabId];
  
  return { redirectUrl: makeReplayUrl(replayInfo, details.url) };
}


// Exec on startup below

chrome.tabs.onUpdated.addListener(function(tabId, changeinfo, tab) {
  if (isWaybackReplayTab(tabId)) {
    chrome.tabs.executeScript(null, {file: "banner.js"});
  }
});

// Proxy Mode
if (proxyMode) {

  var config = {
    mode: "fixed_servers",
    rules: {
      proxyForHttp: {
        host: waybackHost,
        port: waybackProxyPort
      },
      
      bypassList: [waybackHost]
    }
  };
  
  chrome.proxy.settings.set(
      {value: config, scope: 'regular'},
      function() {});
    
  /*    
  chrome.webRequest.onBeforeRequest.addListener(
    function(details)
    {
      if (details.url.indexOf("https://") == 0) {
        return { redirectUrl: "http://" + details.url.substr("https://".length) };
      } 
    },
    
    {urls: ["<all_urls>"]},
    ["blocking"]
  );*/
  
  chrome.webRequest.onBeforeSendHeaders.addListener(
    function(details)
    {
      if (proxyColl) {
        if (details.requestHeaders) {
          headers = details.requestHeaders;
        } else {
          headers = [];
        }
        headers.push({"name": "X-Coll", "value": proxyColl});
        return { requestHeaders: headers };
      }
      
      return {};
    },
    {urls: ["<all_urls>"]},
    ["blocking"]    
  );
 
 /*   
  chrome.webRequest.onAuthRequired.addListener(
    function(details)
    {
      return { authCredentials: { username: proxyColl, password: "" } };
    },
    {urls: ["<all_urls>"]},
    ["blocking"]    
  );*/
    
} else {

  var config = {
    mode: "direct",
  };
  
  chrome.proxy.settings.set(
      {value: config, scope: 'regular'},
      function() {});

  chrome.webRequest.onBeforeRequest.addListener(
    archivalRedirect, 
    {urls: ["<all_urls>"]},
    ["blocking"]
  );

}
