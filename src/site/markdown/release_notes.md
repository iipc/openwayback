<head><title>Release Notes</title></head>

# Release Notes

Full listing of changes and bug fixes are not available prior to release 1.2.0 and between release 1.6.0 and OpenWayback 2.0.0 BETA 1 release.

## OpenWayback 2.4.1 Release
### Bug fixes
* Don't parse HTML for robots meta tags by default when CDX indexing. Avoids infinite loops. [#402](https://github.com/iipc/openwayback/issues/402)
* Reduce effects of archived page CSS on replay toolbar. [#404](https://github.com/iipc/openwayback/issues/404)
* Add minimize functionality to replay toolbar to preclude archived site navigation being covered. [#406](https://github.com/iipc/openwayback/pull/406)
* Move CSS `<link>` tags inside of `<head>`. [#406](https://github.com/iipc/openwayback/pull/406)

## OpenWayback 2.4.0 Release
### Features
* Add scrollbar to year chart in toolbar appearing in every archived webpage. [#340](https://github.com/iipc/openwayback/issues/340)
* Add a Dockerfile for building OpenWayback. Please see the [How to build and run in Docker](https://github.com/iipc/openwayback/wiki/How-to-build-and-run-in-Docker) documentation for use. [#344](https://github.com/iipc/openwayback/pull/344) and [#362](https://github.com/iipc/openwayback/pull/362)
* Visualize snapshot density in bubble calendar using bubble background color. [#351](https://github.com/iipc/openwayback/issues/351)
* Support Brotli (`br`) content-encoding. [#395](https://github.com/iipc/openwayback/pull/395)
* Rewrite new `imagesrcset` attribute of the link element. [#394](https://github.com/iipc/openwayback/issues/394)
* Rewrite ftp and ftps scheme URIs in HTML. [#400](https://github.com/iipc/openwayback/pull/400)

### Bug fixes
* Add proxy support to IP exclusion. [#260](https://github.com/iipc/openwayback/issues/260) 
* Prevent direct access to .xml files, lib/, and classes/ under the WEB-INF directory. [#353](https://github.com/iipc/openwayback/issues/353)
* Load jQuery required by scrollbar of Toolbar.jsp in noConflict mode. [#357](https://github.com/iipc/openwayback/pull/357)
* Fix inconsistent content and background image shifting with toolbar and disclaimer. [#358](https://github.com/iipc/openwayback/issues/358)
* Fix misplaced `break` statement in WatchedCDXSource. [#369](https://github.com/iipc/openwayback/issues/369)
* Fix toolbar plurality issue when only a single capture. [#372](https://github.com/iipc/openwayback/issues/372)
* Rewrite `integrity` attribute for resources that implement Subresource Integrity. [#371](https://github.com/iipc/openwayback/issues/371)
* Upgrade httpclient from 4.3.5. [#380](https://github.com/iipc/openwayback/issues/380)
* Declare Surefire plugin explicitly due to a change in a default value that breaks builds on newer versions of Maven. [#384](https://github.com/iipc/openwayback/pull/384)
* Rewrite additional HTML5 tag attributes including `source`. [#242](https://github.com/iipc/openwayback/issues/242)
* Fix URL search with language like Arabic. [#386](https://github.com/iipc/openwayback/issues/386)
* Don't lose the first 2 bytes when content-encoding header is false. [#395](https://github.com/iipc/openwayback/pull/395)
* Add HTTPS support to setRequestUrl. [#397](https://github.com/iipc/openwayback/pull/397)

## OpenWayback 2.3.2 Release
### Bug fixes
* Make tooltips scrollable and persistent when mouse hovers over a day in BubbleCalendar. [#328](https://github.com/iipc/openwayback/issues/328)
* Add scrollbar to year chart in BubbleCalendar. [#214](https://github.com/iipc/openwayback/issues/214)
* Restore the ability to set the locale for each access-point. [#327](https://github.com/iipc/openwayback/pull/327)
* Include header/footer markup in BubbleCalendar. [#333](https://github.com/iipc/openwayback/issues/333)
* Allow any elements to happen inside NOSCRIPT without triggering bodyInsert. [#319](https://github.com/iipc/openwayback/pull/319)
* Pass `matchType=exact` explicitly for EmbeddedCDXServerIndex. [#318](https://github.com/iipc/openwayback/pull/318/files)
* Rewrite content in and after scripts of type `text/html`. [#315](https://github.com/iipc/openwayback/pull/315)
* Move `Referer` header parsing in `ServerRelativeArchivalRedirect` to a method for easier customization. [internetarchive#57](https://github.com/internetarchive/wayback/issues/57), [#312](https://github.com/iipc/openwayback/pull/312)
* Fix logo link and broken image in BubbleCalendar. [#338](https://github.com/iipc/openwayback/issues/338)
* Updated webarchive-commons dependency to version 1.1.8

## OpenWayback 2.3.1 Release
### Bug fixes
* Rewrite URLs in srcset attribute. [#310](https://github.com/iipc/openwayback/issues/310)
* Add "noscript" as a valid Head tag for Toolbar insertion. [#306](https://github.com/iipc/openwayback/issues/306)
* Remove IA-specific extension code in FlatFileResourceFileLocationDB, make it extensible. [#313](https://github.com/iipc/openwayback/pull/313)
* Updated webarchive-commons dependency to version 1.1.6

## OpenWayback 2.3.0 Release
### Features
* Allow revisit records to be resolved when using a RemoteResourceIndex by adding WARCRevisitAnnotationFilter and ConditionalGetAnnotationFilter filters. [#304](https://github.com/iipc/openwayback/pull/304)
* Use Markdown for documentation. [#265](https://github.com/iipc/openwayback/issues/265)
* Display # of snapshots of a selected year in BubbleCalendar. [#256](https://github.com/iipc/openwayback/issues/256)
* New FilenameFilter to include or exclude files using regular expressions [#237](https://github.com/iipc/openwayback/issues/237)
* Add a system to manage specific rules for some URL [#182] (https://github.com/iipc/openwayback/pull/182)
* TransparentReplayRenderer now has limited support for Range requests. [#179] (https://github.com/iipc/openwayback/issues/179)

### Bug fixes
* Fix for WatchedCDXSourceTest on MaxOSX. [#271] (https://github.com/iipc/openwayback/issues/271)
* UTF BOM detection is not working at all [#283](https://github.com/iipc/openwayback/issues/283)
* Failing test on Windows: EmbeddedCDXServerIndexTest.handleRequest() depends on plattform line ending. [#236](https://github.com/iipc/openwayback/issues/236)
* Remove "contrib/ia" directory [#264](https://github.com/iipc/openwayback/issues/264)
* Set exclusionFilter on wbRequest via AccessPoint only if not already set. [#259] (https://github.com/iipc/openwayback/issues/259)
* robots.txt exclusion: Disallow: rules after empty Disallow: get ignored. [#268] (https://github.com/iipc/openwayback/issues/268)
* Fix closestGroup race condition in RemoteResourceIndex. [#239] (https://github.com/iipc/openwayback/pull/239)
* Minor clean up of typos and references to Wayback Machine in WaybackUI.properties. [#300] (https://github.com/iipc/openwayback/issues/300)
* Use trimmed filename so readers can infer format. [#302] (https://github.com/iipc/openwayback/pull/302)
* Fix for Date range queries. [#293] (https://github.com/iipc/openwayback/pull/293)

## OpenWayback 2.2.0 Release
### Features

* WatchedCDXSource now monitors directories for new CDX files. [#181] (https://github.com/iipc/openwayback/issues/181)
* System environment variables can be used to override some basic configurations without changing the XML. [#220]
(https://github.com/iipc/openwayback/pull/220) and [#217] (https://github.com/iipc/openwayback/issues/217)
* Minor fixes to replace hardcoded port numbers aun URL prefixes with placeholders. [#223]
(https://github.com/iipc/openwayback/pull/223)
* Require Java 7. [#178] (https://github.com/iipc/openwayback/pull/178)
* Enable use of standard proxy servers (e.g. Squid) [#250] (https://github.com/iipc/openwayback/issue/250)
* WatchedCDXSource now optionally recurses and filters on filenames. [#219]
(https://github.com/iipc/openwayback/pull/219)
* Support for Internationalized Domain Name (IDN) [#27]
(https://github.com/iipc/openwayback/pull/27)
* UI localization. [#46]
(https://github.com/iipc/openwayback/pull/46)

### Bug fixes
* Removed duplicate accessPointPath property in proxy replay section of wayback.xml [#229]
(https://github.com/iipc/openwayback/issues/229)
* Failing test: testPadDateStr(org.archive.wayback.util.TimestampTest) [#231]
(https://github.com/iipc/openwayback/issues/231)
* Replace current (IA) favicon with one matching OpenWayback logo. [#247]
(https://github.com/iipc/openwayback/issues/247)
* Fixed ARCRecordingProxy times out. [#116]
(https://github.com/iipc/openwayback/issues/116)
* Moved 'SWFReplayRenderer' to external file; imported to maintain functionality. [#176]
(https://github.com/iipc/openwayback/issues/176)

## OpenWayback 2.1.0 Release
### Features
* Synchronised with latest changes from the Internet Archive fork. [#195]
(https://github.com/iipc/openwayback/pull/195)
* URL-decode timestamp segment of replay URL. [#195]
(https://github.com/iipc/openwayback/pull/195), [internerarchive#23]
(https://github.com/internetarchive/wayback/issues/23)
* Revisits can be resolved with excluded capture. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#65]
(https://github.com/internetarchive/wayback/issues/65)
* Added rudimentary mime-type sniffing (work in progress). [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#46](https://github.com/internetarchive/wayback/issues/46)
* Timestamp-collapsing can be configured to return the last best capture in each collapse group. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#64] (https://github.com/internetarchive/wayback/issues/64)
* UIResults now has makePlainCaptureQueryUrl() method for generating clean, short URL for capture query links. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#60](https://github.com/internetarchive/wayback/issues/60)
* MultipleRegexReplaceStringTransformer may also be used as RewriteRule. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#54] (https://github.com/internetarchive/wayback/issues/54)
* Allow for using different collapseTime for replay and capture search. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#49]
(https://github.com/internetarchive/wayback/issues/49)
* Make collection-dependent exclusion configurable. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#48]
(https://github.com/internetarchive/wayback/issues/48)
* Removed CustomUserResourceIndex class, which does not appear to have broad utility. [#195]
(https://github.com/iipc/openwayback/pull/195)
* Performance information in response header can now be in JSON format. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#69]
(https://github.com/internetarchive/wayback/issues/69)
* FastArchivalUrlReplayParseEventHandler no longer rewrite relative URLs for better replay quality. [#195]
(https://github.com/iipc/openwayback/pull/195)
* Made start date configurable (defaults to old value of 1996), end date dynamic to current year. [#51]
(https://github.com/iipc/openwayback/issues/51)

###Bug Fixes
* Fixed [issue #196] (https://github.com/iipc/openwayback/issues/196) to allow running under Tomcat 8. [#198]
(https://github.com/iipc/openwayback/pull/198)
* Fixed incorrect Content-Type in replay of resource record with JWATFlexResource. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#68] (https://github.com/internetarchive/wayback/issues/68)
* Fixed ClassCastException when JWATFlexResourceStore is in use. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#67]
(https://github.com/internetarchive/wayback/issues/67)
* Pass-through Content-Range header field for audio playback to work. [#195]
( https://github.com/iipc/openwayback/pull/195), [internetarchive#66]
(https://github.com/internetarchive/wayback/issues/66)
* Fixed undesirable rewrite of in-page (fragment-only) links. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#63]
(https://github.com/internetarchive/wayback/issues/63)
*Fixed XHTML parse error due to banner insert before XML declaration. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#61]
(https://github.com/internetarchive/wayback/issues/61)
* Fixed PrivTokenAuthChecker resetting ignoreRobots. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#51]
(https://github.com/internetarchive/wayback/issues/51)
* Made CharsetDetector adher to WHAT-NG recommendation. [#195]
(https://github.com/iipc/openwayback/pull/195), [internetarchive#47]
(https://github.com/internetarchive/wayback/issues/47)
* Fixed building with JDK 8. [#141]
(https://github.com/iipc/openwayback/pull/141)
* NullPointerException for RemoteResourceIndex [#193]
(https://github.com/iipc/openwayback/issues/193)
* Removed direct references to Unix specific TMP paths /tmp and /var/tmp. [#172]
(https://github.com/iipc/openwayback/issues/172)
* Initial thread-safety fix for Memento. [#188]
(https://github.com/iipc/openwayback/issues/180)
* Fixed xml-markup in Toolbar.jsp which caused probelsm on some sites. [#171] (https://github.com/iipc/openwayback/issues/171), [#60] (https://github.com/iipc/openwayback/issues/60
* Fixed some @import url's in  &lt;style&gt; section of html are not rewritten. [#131]
(https://github.com/iipc/openwayback/issues/131)
* Fixed issue [#48] (https://github.com/iipc/openwayback/issues/48) jQuery getting stomped on.
* Support for loading resources from S3 buckets. [#189]
(https://github.com/iipc/openwayback/issues/189)
* Refactored CDX Server into a war and jar module. [#164]
(https://github.com/iipc/openwayback/issues/164)

## OpenWayback 2.0.0 Release
### Features
* Fixed URL resolution in ServerRelativeArchivalRedirect in non-ROOT context. [#92](https://github.com/iipc/openwayback/issues/92)
* Deprecated use of bean name in spring to provide configuration. [#94](https://github.com/iipc/openwayback/issues/94)
* Reviewed and updated mailing lists. [#126](https://github.com/iipc/openwayback/issues/126) [#127](https://github.com/iipc/openwayback/issues/127)
* Added Java cross-reference and updated site generation with dependencies. [#128](https://github.com/iipc/openwayback/issues/128) [#130](https://github.com/iipc/openwayback/issues/130)
* Fixed Javadoc output in Java8. [#136](https://github.com/iipc/openwayback/issues/136)
* Updated 'developers' and 'contributors' lists in POM. [#137](https://github.com/iipc/openwayback/issues/137)
* Cleaned up the Memento configuration. [#150](https://github.com/iipc/openwayback/issues/150)
* Added new logos to project. [#100](https://github.com/iipc/openwayback/issues/100)
* Cleaned up default config file. [#144] (https://github.com/iipc/openwayback/pull/144)
* Updated and improved [documentation] (https://github.com/iipc/openwayback/issues?q=is%3Aissue+is%3Aclosed+label%3Adocumentation).
* Updated dependency on Webarchive-commons 1.1.4. [#157] (https://github.com/iipc/openwayback/pull/157)
* Added 'accessPointPath' to default proxy config. [#158] (https://github.com/iipc/openwayback/pull/158)

### Bug fixes
* Fixed the date locale issue. Creations of java.text.SimpleDateFormat now independent of local setting.  [#157] (https://github.com/iipc/openwayback/pull/157) [#148] (https://github.com/iipc/openwayback/pull/148) [#154] (https://github.com/iipc/openwayback/pull/154)
* Fixed support for uncompressed ARCs files [#101] (https://github.com/iipc/openwayback/issues/101)

## OpenWayback 2.0.0 BETA 2 Release
### Features

* Added PrefixFieldCollapser and RegexFieldMatcher to CDX server. [#7](https://github.com/iipc/openwayback/issues/7)
* Added support for WARC Revisit including URL-Agnostic. [#21]
(https://github.com/iipc/openwayback/issues/21)
* Added support for WARC metadata records. [#23]
(https://github.com/iipc/openwayback/issues/23)
* Added support for WARC resource records. [#24]
(https://github.com/iipc/openwayback/issues/24)
* Removed Internet Archive defaults and branding. [#45]
(https://github.com/iipc/openwayback/issues/45)
* Integrated JWAT ResorceStore. [#54]
(https://github.com/iipc/openwayback/issues/54)
* Provided an [OpenWayback Sample Overlay](https://github.com/iipc/openwayback-sample-overlay).
* Carried out and documented manual testing. [#80]
(https://github.com/iipc/openwayback/issues/80)
* Updated and improved documentation (as on the [wiki](https://github.com/iipc/openwayback/wiki)).
* Renamed artefacts and repositories “webarchive-commons” and updated POMs. [#90] (https://github.com/iipc/openwayback/issues/90)

### Bug fixes

* Query string being stripped from Memento queries. [#106]
(https://github.com/iipc/openwayback/issues/106)
* Support for uncompressed ARC files. [#101]
(https://github.com/iipc/openwayback/issues/101)

## OpenWayback 2.0.0 BETA 1 Release
### Features
* Added livewebPrefix to wayback.xml. [#3]
(https://github.com/iipc/openwayback/issues/3)
* Removed dependencies on Internet Archive’s Maven artefacts, enabling Tarvis CI builds and clean releases. [#10] (https://github.com/iipc/openwayback/issues/10)
* Moved critical code for OpenWayback from the heritrix-commons codebase into webarchive-commons. [#4]
(https://github.com/iipc/openwayback/issues/12 and https://github.com/iipc/webarchive-commons/issues/4)

### Bug fixes
* Dependency on heritrix-commons SNAPSHOT release. [#11] (https://github.com/iipc/openwayback/issues/11)

_**The following releases of the Open Source Wayback Machine (OSWM) were made by the Internet Archive. The on-going development of Wayback was handed over to the International Internet Preservation Consortium (IIPC) in October 2013. For more details please see [General overview] (https://github.com/iipc/openwayback/wiki/General-Overview).**_

## New Release – 1.8.0
* Introduced the wayback-cdx-server.

No further release notes available.

## New Release – 1.7.0
Release notes not available.

## Release 1.6.0
### Major Features
* Memento integration.
* Improved live-web fetching, enabling simpler external caching of robots.txt documents, or other arbitrary content used to improve function of a replay session.
* Customizable logging, via a logging.properties configuration file.
* Vastly improved Server-side HTML rewriting capabilities, including customizable rewriting of specific tags and attributes, rewriting of (some easily recognizable) URLs within JavaScript and CSS.
* Snazzy embedded toolbar with "sparkline" indicating the distribution of captures for a given HTML page, control elements enabling navigation between various versions of the current page, and a search box to navigate to other URLs directly from a replay session.
* Improved hadoop CDX generation capabilities for large scale indexes.
* SWF (Flash) rewriting, to contextualize absolute URLs embedded within flash content.
* ArchivalUrl mode now accepts identity ("id_") flag to indicate transparent replaying of original content.
* NotInArchive can now optionally trigger an attempt to fill in content from the live web, on the fly.
* Updated license to Apache 2.

### Major Bug Fixes
* More robust handling of chunk encoded resources.
* Fixed problem with improperly resolving path-relative URLs found in HTML, CSS, Javascript, SWF content.
* Fixed problem with improperly escaping URLs within HTML when rewriting them.
* Fixed problem where a misconfigured or missing administrative exclusion file was allowing results to be returned, instead of returning and appropriate error.
* No longer extracts resources from the ResourceStore before redirecting to the closest version, which was a major inefficiency.

Minor Features
* Now provide closeMatches list of search results which were not applicable given the users request, but that may be useful for followup requests.
* Archival Url mode now allows rotating through several character encoding detection schemes.
* Proxy Replay mode now accepts ArchivalURL format requests, allowing dates to be explicitly requested via proxy mode.
* AccessPoints can be now configured to optional require strict host matching for queries and replay requests.
* Now filters URLs which contain user-info (USER:PASSWORD@example.com) from the ResourceIndex
* ArchivalURL mode requests without a datespec are now interpreted as a request for the most recent capture of the URL.
* Improvements in mapping incoming requests to AccessPoints, to allow virtual hosts to target specific AccessPoints.
* ResourceNotAvailable exceptions now include other close search results, allowing the UI to offer other versions which may be available.
* ArchivalURL mode now forwards request flags (cs_, js_, im_, etc) when redirecting to a closer date.
* ResourceStore implementation now allows retrying when confronted with possibly-transient HTTP 502 errors.

### Minor Bug Fixes
* cdx-indexer (replacement for arc-indexer and warc-indexer) tool now returns accurate error code on failure.
* No longer sets JVM-wide default timezone to GMT - now it is set appropriately on Calendars when needed.
* Hostname comparison is now case-insensitive.
* Server-relative archival url redirects now include query arguments when redirecting.
* Server-relative archival url redirects now include a Vary HTTP header, to fix problems when a cache is used between clients and the Wayback service.
* Fixed problem with robots.txt caching within a single request, which caused serious inefficiency.
* Fixed problem with resources redirecting to alternate HTTP/HTTPS version of themselves.
* Fixed problem with accurately converting 14-digit Timestamps into Date objects for later comparison.
* Automatically remaps the oft-misused charset "iso-8859-1" to the superset "cp1252".

## Release 1.4.2
### Features
* Added exactSchemeOnly configuration to AccessPoint, allowing explicit distinction between http:// and https:// (ACC-32)
* Now times out requests to a slow/non-responsive RemoteResourceIndex and remote(HTTP 1.1) ResourceStore nodes. (ACC-38)
* Experimental OpenSearchQuery .jsp implementations(ACC-56)
* FileProxyServlet now accepts /OFFSET trailing path in addition to Content-Range HTTP header.(ACC-74)
* warc-indexer now has -all option to produce a CDX line for ALL records, not just captures and revisits(ACC-75)
* Now includes file+offset for all records, keying off mime-time of warc/revist to determine revisits at query time.(ACC-76)
* Allow prefixing of original HTTP headers with a fixed string. (ACC-77)
* Now Wayback rewrites Content-Base HTTP headers.(ACC-78)
* Timeline.jsp improvements which prevent Timeline from being severely distorted on some pages.
* Improvement to ArchivalUrl client-rewrite.js to preserve link text, working around a bug in Internet Explorer.

### Bug Fixes
* Now all mime-types are escaped to prevent spaces from getting into the CDX files.(ACC-45)
* Some CSS URLs were being rewritten twice. (ACC-53)
* No longer writing original pages Content-Length HTTP header to output, which caused original pages with Lower-Case "L" in "Content-length" to return wrong length, truncating replayed documents. This caused some replayed pages to not have embedded disclaimers, nor javascript rewriting of links and images. (ACC-60)
* Fixed severe problem with live web robots.txt retrieval where wrong offset was being writting into the live web ResourceIndex. (ACC-62)
* Charset extraction from HTTP headers is now case-insensitive. (ACC-63)
* No longer adding content to HTML pages with FrameSet tags, as they were being broken.(ACC-65)
* No longer set GMT as default timezone for entire JVM.(ACC-70)

## Release 1.4.1
### Features
* Index filter which allows including/excluding records based on HTTP response code field.(ACC-43)
* Outputs log message instead of stack dump when failing to access a Resource.
Bug Fixes
* Some redirect records were not being located in index due to bad logic in Duplicate record filter.(ACC-30)
* Wayback was not throwing a NotInArchiveException when Self-Redirect replay filter removes all records. (unreported)
* Location HTTP header values were not being escaped before placing in CDX, causing some records to have too many columns. (ACC-31)
* Search Result summary counts were incorrect in Url Prefix searches.(ACC-33)
* Implemented NoCache.jsp, a replay insert which adds a Cache-Control: no-cache HTTP header to all replayed documents.(ACC-34)
* Timeline.jsp was using Request Date, not Capture date, which caused Proxy Mode Timeline to show the wrong date. (ACC-36)
* Advanced Search reference implementation .jsp was broken. (ACC-37)
* AnchorDate and AnchorWindow functionality is now disabled by default, and can be enabled via configuration on an AccessPoint. (ACC-46)

## Release 1.4.0
### Features
* @Completely new implementation of ResourceStore classes, including recursive local directory scanning, scanning multiple local directories, an experimental remote directory scanning capability, and groundwork for future support of both non ARC/WARC file formats and large scale automatic indexing.
* @Complete overhaul of the Replay system, allowing jspInserts within ArchivalUrl, DomainPrefix, and Proxy replay modes. Also includes groundwork for future fine-grained mime-type and url-based Replay customizations.
* Added capability to explicitly set Locale to use for an AccessPoint, overriding the default behavior of using the user agents specified preferred language.
* New flat file implementation of FileLocationDB. See CDXCollection.xml within the .war file for and example usage.
* AnchorDate feature, tracking the date with which a user begins a replay session. During this session, wayback will always attempt to remain near this date, preventing time-drift within a replay session.
* AnchorWindow feature, which allows users to specify a maximum time window in either direction of the AnchorDate that they wish to view replayed content. When a user has set this option, Wayback will not display captures outside the specified window.
* New command line tool location-db to create a location DB offline, populating with lines read from STDIN.
* Added new AccessControlSettingOperation authentication control component, allowing the configuration of the appropriate Exclusion system per-request, as defined by arbitrary BooleanOperators. See ComplexAccessPoint.xml within the .war file for an example usage.
* Added .asx archival URL replay, which rewrites links inside archived .asx files, attempting to make them point back into the Wayback service.
* Now accept "http:/" as identical to "http://" in the beginning of a URL, working around a browser bug which stripped multiple "/"s in URL paths.
* @Refactoring of ResourceIndex interfaces, to allow for future update-able ResourceIndex implementations beyond BDBIndex based ResourceIndexes.
* *Major internal refactoring of WaybackRequest object, providing more stable get/set methods for accessing the standard internal fields with type-safety.
* *Major internal refactoring of SearchResults into CaptureSearchResults and UrlSearchResults, which was previously under-specified and often confusing. These new classes provide more stable get/set methods for accessing the standard internal fields with type-safety.
* *Changed locations of replay, query, and exception .jsp files within .war file to underneath WEB-INF, so they are not directly accessible via HTTP.
* German translation of default Wayback UI. Thanks Andreas!
* Czech translation of default Wayback UI. Thanks Lukáš Matějka! (<< ACC-29)
* All threads now notified of shut downs, allowing resources to be released cleanly.
* *Refactor of all Request and Result related constants from WaybackConstants to WaybackRequest and the SearchResult(s) classes.
* *Refactor of the various UI Results classes, which are used by Query, Replay, and Exception .jsp files to access context information into the single class, UIResults, which has a more stable interface.
* New AccessPoint.urlRoot optional configuration, enabling explicit control over URLs generated for the UI.

### Bug Fixes
* (ACC-24) Fixed bug in Proxy mode which prevented the correct number of results from being returned from the index during Replay.
* (ACC-21) fixed bug where some CSS import declarations where not being correctly rewritten.
* (ACC-26) fixed rare String OOB exception when marking up pages with some forms of Javascript generated HTML.
* (ACC-28) verifies that detected encoding is supported in local JVM before attempting to decode a resource into a String.
* Fixed declared page encoding of help, advanced search and index page to UTF-8.
* Explicitly set character encoding on returned documents, instead of relying on Tomcat to return the correct encoding.

### Migration notes to 1.4.0 from 1.2.X
Wayback 1.4.0 includes substantial code changes aimed at extending current capabilities, enabling planned future features, and stabilizing interfaces used in .jsp customizations. Since these changes would already require a significant update of existing customizations made to .jsp files, many non-vital cleanups to the source tree were included. The goal of implementing all of these features within this single release is to minimize future required updates.

Below is a somewhat inclusive list of changes that will be required when upgrading to Wayback 1.4.0 from 1.2.X, divided into two main categories: changes required to Spring configuration, and changes required for .jsp customizations. Depending on the scope of the existing customizations in your installations, it may be simpler to modify your existing customizations to conform to new interfaces and packages, and in other cases, it may be simpler to begin with the new reference implementations and modify them to meet your needs.
If there are changes not addressed here, or if you have questions regarding specific issues when upgrading, please direct these questions to the archive-access-discuss forum.

### Spring upgrade information
New features with the @ mark indicate features that will directly impact Spring XML configuration files used with 1.2.X.

* **org.archive.wayback.resourcestore.http.FileLocationDB** now: **org.archive.wayback.resourcestore.locationdb.BDBResourceFileLocationDB**
* **org.archive.wayback.resourcestore.http.FileLocationDBServlet** now: **org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDBServlet**
* **org.archive.wayback.resourcestore.http.ArcProxyServlet** now: **org.archive.wayback.resourcestore.locationdb.FileProxyServlet**
* All ReplayUI implementations changed completely, now located in: ArchivalUrlReplay.xml, DomainPrefixReplay.xml, ProxyReplay.xml. Customizations to jspInserts should be straightforward on inspecting these files.
* **org.archive.wayback.resourcestore.Http11ResourceStore** now: **org.archive.wayback.resourcestore.SimpleResourceStore. See RemoteCollection.xml** for configuration example.
* The new automatic indexing is most simply upgraded by modifying the new example in BDBCollection.xml with your custom paths.

### .jsp upgrade information
New features with the * mark indicate features that will directly impact customizations made to .jsp files used with 1.2.X. The bulk of the changes fit three categories:

* class name and package changes requiring import tag updates. Please see .jsps in new distribution for updated packages.
* .jsp path changes due to webapp directory tree cleanup. Again, please see the current locations in the new distribution.
* Java changes within .jsp files due to UIResults refactoring. Previously each type of response page had a unique class used to marshal context information to the .jsp files. These have all been refactored into a single class, **org.archive.wayback.core.UIResults** which has methods to access the appropriate data in each case. Additionally, many convenience methods that were present on the various UI\*Results classes have been removed, since convenience methods are now available on the core classes: **WaybackRequest**, **CaptureSearchResult**, **CaptureSearchResults** and **UrlSearchResult**
As an example, the Timestamp class is no longer used in the .jsp files, since all time information uses the Date class for localization. All of the above classes now have methods to directly return Dates.

For specific examples, please see the reference .jsp files included with the new distribution.

## Release 1.2.1
### Features
* Now explicitly sets the charset component of replayed HTML page Content-Type HTTP headers in Archival URL mode. This overrides Tomcat's default behavior of explicitly setting this value to Tomcat's defaultencoding character set, if a document does not set it explicitly. The original Content-Type HTTP header value is now returned as HTTP header X-Wayback-Orig-Content-Type.

### Bug Fixes
* Added getter/setter for replay image, css, javascript, and html error handling .jsps
* Now returns "closest" indicator on XML query results, fixing problem with WAXToolbar/Proxy mode. (ACC-11)
* auto-indexer now closes ARC/WARC files after indexing, fixing out-of-filehandle problem (ACC-12)
* location-client now syncs .warc and .warc.gz files with locationDB, in addition to .arc and .arc.gz files. (ACC-13)
* Fixed problem which prevented captures archived after webapp was deployed from being returned. Now captures up to the current moment are returned. (ACC-14)
* Changed all .jsp files to return UTF-8(ACC-18)
* Now sending correct end Date to remote NutchWAX index. (ACC-20)
* Fixed String OOB exception when attempting to rewrite some CSS text (ACC-17)
* Now updates CSS "import 'URL';" and 'import "URL";' content. Previously only updated "import url(URL);" content.
* Fixed Replay redirect loop when using RemoteResourceIndex (ACC-15)

## Release 1.2.0
### Features
* Now supports compressed and uncompressed ARC and WARC files.
* Initial revision of "deduplicated" WARC record handling, which returns the last version that was actually stored when subsequent captures are not saved because they have not changed.
* Now filters (literal) duplicate records from the ResourceIndex, in case the same capture (url + date) appears twice, or in two CDX files.
* UrlCanonicalizer is now pluggable, current functionality is now implemented in AggressiveUrlCanonicalizer. Added IdentityUrlCanonicalizer, which performs no canonicalization.
* bin-search command line tool now outputs a single stream of sorted results from multiple files, instead of returning matches from each file sequentially.
* Extracted several replay features into separate jspInserts that can now be mixed and matched.
* Now handles most text/css URL rewriting, both inside HTML pages, and in externally linked .css files.
* Externalized comment embedded inside replayed HTML pages into jspInsert: ArchiveComment.jsp.
* Non-javascript Archival URL replay mode, where all URL rewriting occurs on the server. This includes a non-javascript Timeline jspInsert.
* Added two-month timeline partition.
* Root page of webapp now lists access points, when users make a request that does not specify one. Also, now access point "slash-pages" are available "without the slash".

### Bug Fixes
* Now rewrite Location and Content-Base HTTP headers in non-HTML Archival URL replayed documents.
* Now rewrites all background attributes found in returned pages (archival URL mode only) instead of just on BODY tags.
* Now rewrites src attributes on INPUT tags.
* Command line tools now allow whitespace arguments, important for tools accepting delimiter arguments.
* Replay URLs in query results now include non-standard ports, if needed.
* Timezone is now explicitly set to GMT/UTC, fixing a Calendar result partitioning problem.
* Uncaught character-encoding exceptions now handled, plus slightly improved detection of correct character encoding by removing internal whitespace in declared encoding names.
* Archival URL parsing of query end-date now assumes latest possible date given a partial end-date, instead of earliest possible date.
* Re-implemented lost "closest" indicator for XML results.
* Now supports multiple auto index threads, one per ResourceStore, and also multiple auto index merge threads, one per BDB ResourceIndex.
* Fixed hard-coded maximum year issue.
* Re-implemented NotInArchive logging, which was lost in 1.0.0.
