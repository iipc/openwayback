var waybackDefs = [
                   //Global Wayback
                   {"waybackUrl": "http://web.archive.org/",
                    "waybackName": "Internet Archive Wayback Machine",
                    
                    "proxyHost": null,
                    "proxyPort": null,
                    
                    "collections": ["web"]},

                   //AIT Wayback
                   {"waybackUrl": "http://wayback.archive-it.org/",
                    "waybackName": "AIT Wayback",
                    
                    "proxyHost": "wayback.archive-it.org",
                    "proxyPort": 8081,
                    
                    "collections": ["all", "194", "1068", "2106"]}                    
                  ];

//TODO:
var currWayback = waybackDefs[0];
var currColl = waybackDefs[0].collections[0];

var state = {"isClientRewrite": false,
             "isRedirectErrors": false,
             "isProxyMode": false};

               
chrome.extension.onMessage.addListener(
  function(request, sender, sendResponse) {
    if (request.type == "LIST_WAYBACKS") {
      sendResponse({"waybacks": waybackDefs, "opts": state, "currWayback": currWayback});
      return;
    }
    
    if (request.type == "SEL_WAYBACK") {
      currWayback = waybackDefs[request.index];
      currColl = currWayback.collections[0];
      sendResponse({"currWayback": currWayback});
      return;
    }
    
    if (request.type == "TOGGLE_OPT") {
      //console.log(request.id + " " + request.enabled);
      
      switch (request.id) {
        case "redirectErrors":
          toggleRedirectErrors(request.enabled);
          break;
          
        case "proxyMode":
          toggleProxyMode(request.enabled);
          break;
          
        case "clientRewrite":
          toggleClientRewrite(request.enabled);
          break;
      }
      return;
    }
  });

//********* Client Rewriter

// All known tabs
var tabIdToUrl = {};

// Archival Url Regex
var archivalURLRegex = /([^/]*)\/(\d{1,14})([a-z]{2}_)*\/(.*)/;

function toggleClientRewrite(enabled)
{
  state.isClientRewrite = enabled;
  
  if (enabled) {
    chrome.tabs.onUpdated.addListener(bannerInsert);

    chrome.webRequest.onBeforeRequest.addListener(
        archivalRedirect, 
        {urls: ["<all_urls>"]},
        ["blocking"]
      );

    // Init tabs
    tabIdToUrl = {};
    
    chrome.tabs.getAllInWindow(null, loadAllTabs); 
    
  } else {
    chrome.tabs.onUpdated.removeListener(bannerInsert);
    chrome.webRequest.onBeforeRequest.removeListener(archivalRedirect);
  }
}

function bannerInsert(tabId, changeinfo, tab) {
  if (isWaybackReplayTab(tabId)) {
    if (tab.status == "loading") {
      chrome.tabs.executeScript(null, {file: "banner.js"});
    }
  }
}

function loadAllTabs(tabs)
{
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
}

function findWaybackPrefix(url)
{
  for (i in waybackDefs) {
    if (url.indexOf(waybackDefs[i].waybackUrl) == 0) {
      return waybackDefs[i].waybackUrl;
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
  if (state.isProxyMode) {
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

// ********* End Client Rewriter

// ********* 404 Handler
function toggleRedirectErrors(enabled)
{
  state.isRedirectErrors = enabled;
  
  if (enabled) {
    chrome.webRequest.onErrorOccurred.addListener(errorHandler, {urls: ["<all_urls>"]});
    chrome.webRequest.onCompleted.addListener(errorHandler, {urls: ["<all_urls>"]});
  } else {
    chrome.webRequest.onErrorOccurred.removeListener(errorHandler);
    chrome.webRequest.onCompleted.removeListener(errorHandler);
  }
}

function getWBTestUrl(url)
{
  //return wayback404Prefix + "cdx/search/cdx?url=" + encodeURIComponent(url) + "&filter=statuscode:[23]..&limit=-2&gzip=false";
  return currWayback.waybackUrl + currColl + "/3id_/" + url;
}

function doRedirectTo(tabId, url)
{
  //var mod = "";
  //var waybackUrl = wayback404Prefix + coll + "/" + timestamp + mod + "/" + url;  
  //console.log("redirect to: " + waybackUrl);
  
  var redirUrl = currWayback.waybackUrl + currColl + "/" + url;
  
  chrome.tabs.update(tabId, {"url": redirUrl});
}


function handleWaybackRedirect(tabId, url)
{
  var xhr = new XMLHttpRequest();
  
  xhr.onreadystatechange = function parseCdxAndRedirect()
  {
    if (xhr.readyState != 4) {
      return;
    }
    
    if (xhr.status < 200 || xhr.status >= 400) {
      return;
    }
    
    doRedirectTo(tabId, url);    
    
//    if (!xhr.responseText || (xhr.responseText == "")) {
//      return;
//    }
    
//    var lines = xhr.responseText.split('\n');
//    var fields = lines[0].split(' ');
//    if (fields[1]) {
//      doRedirectTo(tabId, url, fields[1]);
//    }
  };
  
  xhr.onload = function doneLoading()
  {
    if (xhr.status < 200 || xhr.status >= 400) {
      return;
    }
  }
  
  //var cdxUrl = getCdxTestUrl(url);
  var testUrl = getWBTestUrl(url);
  xhr.open("HEAD", testUrl, true);
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

// ********* End 404 Handler


// ********* Proxy Mode ************
function toggleProxyMode(enabled)
{
  state.isProxyMode = enabled;
  proxyHost = currWayback.proxyHost;
  proxyPort = currWayback.proxyPort;
  
  //TODO: raise error?
  if (!proxyHost || !proxyPort) {
    console.log("Missing proxyHost" + proxyHost + " or port " + proxyPort);
    state.isProxyMode = false;
  }

  if (state.isProxyMode) {
  
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
          
    chrome.webRequest.onBeforeRequest.addListener( 
      proxyHttpsToHttp,
      {urls: ["<all_urls>"]},
      ["blocking"]
    );
    
    chrome.webRequest.onBeforeSendHeaders.addListener(
      proxySendHeaders,
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
    
    chrome.proxy.settings.set({value: config, scope: 'regular'}, function() {});
    
    chrome.webRequest.onBeforeRequest.removeListener(proxyHttpsToHttp);
    chrome.webRequest.onBeforeSendHeaders.removeListener(proxySendHeaders);
  }
  
  // Alter the proxy settings here
  chrome.proxy.settings.set({value: config, scope: 'regular'}, function() {});
}

function proxyHttpsToHttp(details)
{
  // Rewrite https -> http as https proxying not supported
  if (details.url.indexOf("https://") == 0) {
    var httpUrl = "http://" + details.url.substr("https://".length);
    //console.log(details.url + " -> " + httpUrl);
    
    return { redirectUrl: httpUrl };
  } 
}

function proxySendHeaders(details)
{
  if (currColl) {
    if (details.requestHeaders) {
      headers = details.requestHeaders;
    } else {
      headers = [];
    }
    headers.push({"name": "X-Wayback-Proxy-Coll", "value": currColl});
    //headers.push({"name": "Proxy-Timestamp", "value": "20130605191057"});
    return { requestHeaders: headers };
  }
  
  return {};
}

//********* End Proxy Mode ************