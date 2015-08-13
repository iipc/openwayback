**OpenWayback is enabled and supported by the International Internet Preservation Consortium (IIPC) and the member institutions with which the contributors of the project are affiliated.**

A detailed list of changes (per release) can be found in the [Release Notes](release_notes.html).

## Releases

### OpenWayback 2.2.0 Release - 3 June 2015

OpenWayback 2.2.0 release is a minor release. It includes bug fixes and some modest new features. See [release notes](release_notes.html) for full details.

[https://github.com/iipc/openwayback/tree/openwayback-2.2.0](https://github.com/iipc/openwayback/tree/openwayback-2.2.0)

### OpenWayback 2.1.0 Release - 12 February 2015

OpenWayback 2.1.0 release is a minor releases. It includes bug fixes and some modest new features. See [release notes](release_notes.html) for full details.

[https://github.com/iipc/openwayback/tree/openwayback-2.1.0](https://github.com/iipc/openwayback/tree/openwayback-2.1.0)
 
### OpenWayback 2.0.0 Release - 12th September 2014

OpenWayback 2.0.0 release is a culmination of the two previous beta releases. It includes bug fixes and significant improvement of documentation. Existing documentation has been identified, reorganised and updated. A set of essential documentation is included in the release.

[https://github.com/iipc/openwayback/tree/openwayback-2.0.0](https://github.com/iipc/openwayback/tree/openwayback-2.0.0)

### OpenWayback 2.0.0 BETA 2 Release - 14th May 2014
OpenWayback 2.0.0 BETA 2 release includes bug fixes and a number of enhancements, including better support for WARCs and integration of JWAT ResourceStore. A consistent naming scheme has been applied to the artifactId, the github project name, and the jar name. In addition, documentation has been improved, manual testing experience documented and a sample overlay provided, allowing easier customisations of the standard wayback-webapp.

[https://github.com/iipc/openwayback/tree/openwayback-2.0.0.BETA.2](https://github.com/iipc/openwayback/tree/openwayback-2.0.0.BETA.2)

### OpenWayback 2.0.0.BETA 1 Release - 10th January 2014
OpenWayback 2.0.0 BETA 1 release mainly reflects the move of ownership of the lead repository from the Internet Archive to the IIPC. This includes a set of changes to the IA codebase to make it more independent of their build server and some non-public artefacts. Moreover, it includes updates from the internetarchive/wayback fork, corresponding to the move of critical code from internetarchive/heritrix3 to iipc/iipc-web-commons.

[https://github.com/iipc/openwayback/tree/openwayback-2.0.0.BETA.1](https://github.com/iipc/openwayback/tree/openwayback-2.0.0.BETA.1)

**_The following releases of the Open Source Wayback Machine (OSWM) were made by the Internet Archive, who led its development since 2005. The on-going development of Wayback was handed over to the International Internet Preservation Consortium (IIPC) in October 2013, when the OpenWayback project was launched. For more details please see [https://github.com/iipc/openwayback/wiki/General-Overview](https://github.com/iipc/openwayback/wiki/General-Overview)._** 

### New Release - 1.8.0, 18/12/2013
Release notes not available. Among other features, the [wayback-cdx-server](https://github.com/iipc/openwayback/tree/master/wayback-cdx-server) was introduced, which is a standalone HTTP servlet that serves the index that the wayback machine uses to lookup captures. 

### New Release - 1.7.0, 09/02/2013
Release notes not available.

### New Release - 1.6.0, 1/3/2011
The 1.6.0 release is now available, with improved server-side rewriting of HTML, CSS, Javascript, and SWF content. This version includes other new features and bug fixes, which are detailed on the [Release Notes](release_notes.html) page. Upgrading to this version will require changes to Wayback Spring XML configuration.

### Maintenance Release - 1.4.2, 7/17/2009
Release 1.4.2 fixes several problems discovered in the 1.4.1 release. Please see the [Release Notes](release_notes.html) for a detailed list of changes.

### Maintenance Release - 1.4.1, 11/10/2008
Release 1.4.1 fixes several problems discovered in the 1.4.0 release, and most notably disables by default the AnchorDate and AnchorWindow features which generated some confusion. Please see the [Release Notes](release_notes.html) for a detailed list of changes.

### New Release - 1.4.0, 8/20/2008
Release 1.4.0 has several new features, as well as several bug-fixes. This version includes substantial code changes and refactoring to consolidate and expand current functionality as well as enable several future planned features. Notable new features include the ability to insert content in replayed HTML pages within Proxy and DomainPrefix replay modes, the ability to scan multiple local directories recursively for ARC/WARC files, and a feature within Archival URL replay mode to prevent time drift during a replay session.

Please see the [Release Notes](release_notes.html) for specific features and bug fixes.

### New Release - 1.2.0, 1/30/2008
Release 1.2.0 has several new features, as well as several bug-fixes. Wayback now supports compressed and uncompressed ARC and WARC formats. Previously there was only support for compressed ARC files. This version also includes a new Archival URL replay mechanism, where all URL rewriting occurs on the server, obviating the need for client-side Javascript, and preventing some request leakage. This version also includes the capability to replace the default URL canonicalization scheme(currently there is still only one implementation available, but the groundwork for using different schemes is now in place.) This version also includes support for de-duplicated WARC records.
Please see the [Release Notes](release_notes.html) for specific features and bug fixes.

### New Release - 1.0.0, 10/12/2007
Release 1.0.0 has several significant changes, most notably a completely new configuration mechanism using Spring IOC. This new configuration system introduces some deployment concepts:

* **WaybackCollections** define a set of documents via the previously existing ResourceStore and ResourceIndex implementations.
* **AccessPoints** define a method by which users can access and interact with a WaybackCollection. A single WaybackCollection may be exposed to users through several AccessPoints simultaneously. Each AccessPoint specifies an access URL, a Query interface, a Replay interface, and several optional access restrictions, including limiting who can connect to the AccessPoint, and which documents in the WaybackCollection are available through the AccessPoint.

This new configuration frameworks allows hosting of hundreds of individual collections within a single wayback installation, each with potentially multiple AccessPoints.

This version also includes a major refactoring of the Replay User Interface framework, simplifying extension and the creation of novel replay modes. Specifically, one or more external .jsp files can be used to generate additional HTML content within replayed HTML pages. The Timeline Replay mode has been completely replaced by one of these external .jsp files, which inserts the Timeline banner inside replayed HTML pages.
This version includes a very experimental new Replay mode, domain-prefix replay mode, which performs all markup and recontextualization of replayed HTML documents on the server-side, eliminating the need for client-side Javascript execution. Please ask on the discussion list for assistance in using this Replay mode.
Lastly, this version has some internal improvements which should reduce memory consumption, and the software is now built using maven2.

### New Release - 0.8.0, 01/11/2007
Release 0.8.0 offers several new features, most notably a CDX format flat file ResourceIndex implementation, improved character set detection, and many smaller improvements, bug-fixes, and optimizations.

**Major Features:**

* Added Sorted CDX flat file ResourceIndex implementation, allowing for much larger data sets.
* Improved character set detection so pages are not mangled when server side modification occurs.
* Several new command-line tools, for generating and updating each ResourceIndex type.
* Bug-fixes to allow integration with NutchWax full-text searching.

### New Release - 0.6.0, 07/14/2006
Release 0.6.0 offers:

* Timeline Mode - comparable with WERA user interface.
* Manual Exclusions - allows for blocking sites and paths from the index for specific ranges of time.

### New Release - 0.4.0, 03/28/2006
Release 0.4.0 offers many new features and improvements, including:

* Distributed ARC storage.
* Improved Javascript and document rewriting for Archival URL replay mode.
* Several new ResourceIndex implementations: Remote BDB, NutchWax.
* live web robots.txt caching and retroactive compliance.
* "Classic" Wayback Machine query User Interface.

### First Release - 0.2.0, 12/09/2005
First public release of the open source wayback.