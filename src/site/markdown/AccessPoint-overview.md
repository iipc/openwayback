OpenWayback [[Advanced configuration]]

## AccessPoint overview

OpenWayback uses access points to handle incoming requests. There can be multiple access points each with a different context (URL prefix) or even operating on different ports (Tomcat needs to be configured accordingly). 

An AccessPoint's configuration must specify the following implementations:

* **collection** - the specific WaybackCollection being exposed via this AccessPoint.
* **query** - responsible for generating user visible content (HTML, XML, etc) in response to user queries.
* **replay** - responsible for determining the appropriate ReplayRenderer implementation based on the user's request and the particular document to be replayed.
* **uriConverter** - responsible for constructing Replay URLs from records matching user queries. See Replay Modes below.
* **parser** - responsible for translating incoming requests into WaybackRequests. See Replay Modes below.

To specify which request the access point handles you specify:

* **accessPointPath** - typically of the form `http://<hostname>/<path>/`. This will cause the access point to handle requests coming in to `path`.
* **internalPort** - the port that this access point should be listening for. It may be omitted if `accessPointPath` explicitly includes the port.

An AccessPoint's configuration may optionally specify the following, but must specify at least one of replayPrefix, queryPrefix, or staticPrefix:

* **exception** - an implementation responsible for generating error pages to users.
* **configs** - a Properties associating arbitrary key-value pairs which are accessible to .jsp files responsible for generating the UI.
* **exclusionFactory** - an implementation specifying what documents should be accessible within this AccessPoint.
* **authentication** - an implementation specifying who is allowed to connect to this AccessPoint.
* **replayPrefix** - a String URL prefix indicating the host, port, and path to the correct Replay AccessPoint. If unspecified, defaults to queryPrefix, then staticPrefix.
* **queryPrefix** - a String URL prefix indicating the host, port, and path to the correct Query AccessPoint. If unspecified, defaults to staticPrefix, then replayPrefix.
* **staticPrefix** - a String URL prefix indicating the host, port, and path to static content used within the UI. If unspecified, defaults to queryPrefix, then replayPrefix.
* **livewebPrefix** - a String URL prefix indicating the host, port, and path to an AccessPoint configured with Live Web fetching.
* **locale** - a specific Locale to use for all requests within this AccessPoint, overriding the users preferred Locale as specified by their web browser.
* **exactHostMatch** - true or false, if true, only returns results exactly matching a given request hostname (case insensitive). Default is false.
* **exactSchemeMatch** - true or false, if true, only returns results exactly matching a given request scheme. Default is true.

AccessPoints can be used to provide different levels and types of access to the same collection for different users. For example, you can provide both Proxy and Archival URL mode access to a single collection by defining two AccessPoints with different Replay User Interfaces but the same WaybackCollection. Using AccessPoints, you can also provide different levels of access to a collection. For example, users within a particular subnet may be able to access all documents within a collection via one AccessPoint, but users outside that subnet may be restricted to viewing documents allowed by a web sites current robots.txt file.

Please refer to `wayback.xml` within the `wayback.war` file for detailed example AccessPoint configurations.
