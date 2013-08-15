var tabIdToUrl = {};

var archivalURLRegex = /([^/]*)\/(\d{1,14})([a-z]{2}_)*\/(.*)/;

var knownWaybackPrefixList = 
  ["http://web.archive.org/", 
   "http://wayback.archive-it.org/", 
   "http://webharvest.gov/"];

function findWaybackPrefix(url)
{
  for (i in knownWaybackPrefixList) {
    if (url.indexOf(knownWaybackPrefixs[i]) == 0) {
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

function extractWaybackReplayInfo(waybackPrefix, url, matchOnly)
{
  var rest = url.substr(waybackPrefix.length);
  var parts = rest.match(archivalURLRegex);
  
  if (!parts) {
    return null;
  }
  
  if (matchOnly) {
    return true;
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
  if (extractWaybackReplayInfo(replayInfo.waybackPrefix, details.url, true)) {
    return {};
  }
  
  var url = makeReplayUrl(replayInfo, details.url);
  
  //console.log(details.url + " -> " + url);
   
  return { redirectUrl: url };
}


// Exec on startup below

chrome.tabs.onUpdated.addListener(function(tabId, changeinfo, tab) {
  if (isWaybackReplayTab(tabId)) {
    chrome.tabs.executeScript(null, {file: "banner.js"});
  }
});


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
