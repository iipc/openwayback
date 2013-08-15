## Chrome Plugin for Wayback

This is a new experimental plugin that attempts to use [chrome.webRequest](http://developer.chrome.com/extensions/webRequest.html) API to handle wayback url rewriting on the client.

### Archival Mode Support ###
When enabled, the plugin will attempt to 'intercept' requests to a list of `knownWaybackPrefixs` (declared in the plugin) and redirect the archival url to the id_ (transparent replay) and handle rewriting on the client side.


### Proxy Mode Support ###

Proxy mode support not fully implemented yet, but the intent is for the plugin to be able to optionally enable proxy mode when user goes to a known wayback prefix, and allow user to toggle proxy mode via UI. Proxy mode is still needed because chrome does not provide a way to override `window.location` and related settings to the original archived page, so the only method of simulating original location is via proxy mode.

## Deploying ##

1) Pull this code

2) In chrome://extensions, enable Developer Mode

3) Hit 'Load Unpacked Extension' and point to local directory for this plugin
