var tabIdToUrl = {};

var archivalURLRegex = /([^/]*)\/(\d{1,14})([a-z]{2}_)*\/(.*)/;

// Access to Waybacks at these urls will be intercepted and rewritten on the client
var knownWaybackPrefixList = 
  [
   "http://web.archive.org/", 
   "http://wayback.archive-it.org/", 
   "http://webharvest.gov/"];

var wayback404Prefix = "http://web.archive.org/"

function findWaybackPrefix(url)
{
  for (i in knownWaybackPrefixList) {
    if (url.indexOf(knownWaybackPrefixList[i]) == 0) {
      return knownWaybackPrefixList[i];
    }
  }
  
  return null;
}

function extractHostPrefix(url)
{
  var schemeIndex = url.indexOf("://");
  var start = (schemeIndex > 0 ? schemeIndex + 3 : 0);
  
  var end = url.indexOf("/", start);
  if (end > 0) {
    return url.substr(0, end + 1);
  } else {
    return url.substr(0) + "/";
  }  
}

function isReplayUrl(waybackPrefix, url)
{
  if (url.indexOf(waybackPrefix) != 0) {
    return false;
  }
  
  var rest = url.substr(waybackPrefix.length);
  var parts = rest.match(archivalURLRegex);
  
  if (!parts) {
    return false;
  }
  
  return true;
}

function extractWaybackReplayInfo(waybackPrefix, url)
{
  var rest = url.substr(waybackPrefix.length);
  var parts = rest.match(archivalURLRegex);
  
  if (!parts) {
    return null;
  }
  
  return {"fullWaybackUrl": url,
    
          "waybackPrefix": waybackPrefix,
          "coll": parts[1], 
          "timestamp": parts[2], 
          "mod": parts[3],
          "hostPrefix": extractHostPrefix(parts[4]),
          "url": parts[4]};
}


function makeReplayUrl(replayInfo, url)
{
  // Partial Wayback URL
  if (url.indexOf(replayInfo.waybackPrefix) == 0) {
    
    var urlFromPrefix = url.substr(replayInfo.waybackPrefix.length);
    var path;
    
    if (urlFromPrefix && (urlFromPrefix.indexOf(replayInfo.coll) == 0)) {
      // Don't rewrite urls that have collection path
      return url;
      //var urlFromColl = urlFromPrefix.substr(replayInfo.coll.length + 1);
      //path = urlFromColl;
    } else {
      path = urlFromPrefix;
    }
    
    if (path) {
      url = replayInfo.hostPrefix + path;
    }
  }  
  
  var newUrl = replayInfo.waybackPrefix + replayInfo.coll + "/" + replayInfo.timestamp + "id_/" + url;  
  return newUrl;
}


function isWaybackReplayTab(tabId)
{
  if (proxyMode) {
    return true;
  }
  
  return (tabIdToUrl && tabIdToUrl[tabId]);
}


function archivalRedirect(details)
{        
  // Top Level Request
  if (details.type == "main_frame") {
    var waybackPrefix = findWaybackPrefix(details.url);
    
    if (waybackPrefix) {
      replayInfo = extractWaybackReplayInfo(waybackPrefix, details.url);
      
      if (replayInfo) {
        
        tabIdToUrl[details.tabId] = replayInfo;
        
        // Redirect to id_       
        if (!replayInfo.mod || replayInfo.mod == "") {
           //console.log(replayInfo.mod);
           return { redirectUrl: makeReplayUrl(replayInfo, replayInfo.url) };
        }
        
        return {};
      }
    }
  }
  
  if (details.url.indexOf("/crossdomain.xml") >= 0) {
    return {};
  }
  
  // If not in wayback tab, pass through
  if (!isWaybackReplayTab(details.tabId)) {
    return {};
  }
      
  var replayInfo = tabIdToUrl[details.tabId];
  
  // If already a wayback url, pass through
  if (isReplayUrl(replayInfo.waybackPrefix, details.url)) {
    return {};
  }
  
  var url = makeReplayUrl(replayInfo, details.url);
  
  //console.log(details.url + " -> " + url);
   
  return { redirectUrl: url };
}


// Exec on startup below

chrome.tabs.onUpdated.addListener(function(tabId, changeinfo, tab) {
  if (isWaybackReplayTab(tabId)) {
    if (tab.status == "loading") {
      chrome.tabs.executeScript(null, {file: "banner.js"});
    }
  }
});

chrome.tabs.getAllInWindow(null, function(tabs){
  for (var i = 0; i < tabs.length; i++) {
    
    var url = tabs[i].url;
    var tabId = tabs[i].id;
    
    var waybackPrefix = findWaybackPrefix(url);
    
    if (waybackPrefix) {
      var replayInfo = extractWaybackReplayInfo(waybackPrefix, url);
      
      if (replayInfo) {
        //console.log("Found " + tabId + " " + url);
        //console.log(replayInfo);
        tabIdToUrl[tabId] = replayInfo;
      }
    }
  }
});

// ********* 404 Handler

function getCdxTestUrl(url)
{
  return wayback404Prefix + "cdx/search/cdx?url=" + encodeURIComponent(url) + "&filter=statuscode:[23]..&limit=-2&gzip=false";
}

function doRedirectTo(tabId, url, timestamp)
{
  var coll = "web";
  var mod = "";
  var waybackUrl = wayback404Prefix + coll + "/" + timestamp + mod + "/" + url;
  
  //console.log("redirect to: " + waybackUrl);
  
  chrome.tabs.update(tabId, {"url": waybackUrl});
}


function handleWaybackRedirect(tabId, url)
{
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function parseCdxAndRedirect()
  {
    if (xhr.readyState != 4) {
      return;
    }
    
    if (xhr.status != 200) {
      return;
    }
    
    if (!xhr.responseText || (xhr.responseText == "")) {
      return;
    }
    
    // TODO: Need to get two lines due to bug in cdx server at moment, will fix
    var lines = xhr.responseText.split('\n');
    var fields = lines[lines.length - 2].split(' ');
    if (fields[1]) {
      doRedirectTo(tabId, url, fields[1]);
    }
  };
  
  var cdxUrl = getCdxTestUrl(url);
  //console.log(cdxUrl);
  xhr.open("GET", cdxUrl, true);
  xhr.send();
}

function errorHandler(details)
{
  if (details.type == "main_frame") {
    if ((details.statusCode && details.statusCode > 400) || details.error) {
      //console.log("Not Found: " + details.url);
      handleWaybackRedirect(details.tabId, details.url);
    }
  }
};

chrome.webRequest.onErrorOccurred.addListener(errorHandler, {urls: ["<all_urls>"]});
chrome.webRequest.onCompleted.addListener(errorHandler, {urls: ["<all_urls>"]});

// ********* End 404 Handler


// Proxy Mode
var proxyHost = "wayback.archive-it.org";
var proxyPort = 8081;
var proxyColl = "3701";
var proxyMode = false;

if (proxyMode) {

  var config = {
    mode: "fixed_servers",
    rules: {
      singleProxy: {
        host: proxyHost,
        port: proxyPort
      },
      
      bypassList: [proxyHost]
    }
  };
  
  chrome.proxy.settings.set(
      {value: config, scope: 'regular'},
      function() {});
    
      
  chrome.webRequest.onBeforeRequest.addListener(
    function(details)
    {
      // Rewrite https -> http as https proxying not supported
      if (details.url.indexOf("https://") == 0) {
        httpUrl = "http://" + details.url.substr("https://".length);
        //console.log(details.url + " -> " + httpUrl);
        
        return { redirectUrl: httpUrl };
      } 
    },
    
    {urls: ["<all_urls>"]},
    ["blocking"]
  );
  
  chrome.webRequest.onBeforeSendHeaders.addListener(
    function(details)
    {
      if (proxyColl) {
        if (details.requestHeaders) {
          headers = details.requestHeaders;
        } else {
          headers = [];
        }
        headers.push({"name": "X-Wayback-Proxy-Coll", "value": proxyColl});
        //headers.push({"name": "Proxy-Timestamp", "value": "20130605191057"});
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
