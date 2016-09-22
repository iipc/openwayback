# OpenWayback Resource Resolver

The Resource Resolver is the first step in modularizing OpenWayback. It serves the purpose of turning a request for an
Uri, date and possibly other parameters into a information record which is used to get the requested resource.

As it is a Web Archive Resource Resolver it is called **WARR** for short.

## Prerequisites

WARR requires Java 8.

## Installation

As this service is part of the 3.0.0 release of OpenWayback which is not released yet, you can download a snapshot
release from here:
https://oss.sonatype.org/content/repositories/snapshots/org/netpreserve/openwayback/openwayback-resource-resolver/3.0.0-SNAPSHOT/

Find the latest file ending in `.tar.gz`

Unpack it with: `tar zxvf <filename>`

You should now have a directory with a name like `openwayback-resource-resolver-3.0.0-SNAPSHOT`.

Now put some cdx files in `openwayback-resource-resolver-3.0.0-SNAPSHOT/cdx/` and start the Resource Resolver with:  
`openwayback-resource-resolver-3.0.0-SNAPSHOT/bin/warr`

Both traditional [CDX](http://iipc.github.io/warc-specifications/specifications/cdx-format/cdx-2015/) files and  
the new [CDXJ](http://iipc.github.io/warc-specifications/specifications/cdx-format/openwayback-cdxj/) format  
is supported, but in the former case the url key must be SURT encoded.

## Usage

By default WARR listen on port 8080 on all interfaces. This can be changed by editing  
`openwayback-resource-resolver-3.0.0-SNAPSHOT/config/application.conf`.

Some options are common for the endpoints:


### Http request headers

* **Accept**: Content type for the response can be one of:
    * **application/vnd.org.netpreserve.cdxj** Return result in cdxj format
    * **application/vnd.org.netpreserve.cdx** Return result in the legacy CDX format
    * **text/html** Used primarly for testing WARR through a browser

* **Accept-Encoding**: Compression is supported if requested with `Accept-encoding` header

### Http response headers

* **X-Archive-Server**: For every response this header tells the version of the Resource Resolver.

### Resolve a resource

Format: `http://localhost:8080/resource/<Url-encoded Uri>/<time stamp>`

The Uri to the resource you want to resolve must be url-encoded to be a legal path segment. The time stamp can be in either 
WARC-format (e.g. 2016-02-05T14:42:00Z) or Heritrix format (e.g. 20160205144200).

The returned resource is the one closest in time to the time stamp.


#### Query parameters

##### Record Type

You can restrict what kind of records to return with the query parameter **recordType**. The value is a comma separated  
list of desired record types supported by your index. Default is `recordType=response,revisit`

#### Limit

The limit parameter sets the maximum number of records returned. The default is `1` to return exactly the best  
resolution. If set higher, the returned list of resources will be sorted by distance in time to the requsted time stamp.


### List resources

Format: `http://localhost:8080/resourcelist/<Url-encoded Uri>`

Lists all records matching the Uri. The Uri must be url-encoded to be a legal path segment.

#### Query parameters

#### Date

Limit the results to match a date/time range with `date=<date range expression>`.

The date/time range expression consists of one or two dates separated by `,` or `;`. The dates can be in WARC or  
Heritrix format. An expression starting or ending with `,` or `;` is open ended. If there is only one date and no  
separators, it is treated as a range of all dates that can fit into the submitted date.

Examples:

* `date=2007-04` - a range starting at 2007-04-01 (inclusive) and ending on 2007-05-01 (exclusive).
* `date=2007-04-03T14` - a range starting at 2007-04-03T14 (inclusive) and ending on 2007-04-03T15 (exclusive).
* `date=2007-04;2007-05` - a range starting at 2007-04-01 (inclusive) and ending on 2007-05-01 (exclusive).
* `date=2007-04;` - a range starting at 2007-04-01 (inclusive) with no end.
* `date=;2007-04` - a range consisting of every date before 2007-04-01 (exclusive).



#### Record type

You can restrict what kind of records to return with the query parameter **recordType**. The value is a comma separated  
list of desired record types supported by your index. Default is `recordType=response,revisit`

#### Uri Match Type

The default behavior is to return exact uri matches. However, WARR can also return results matching a path  
prefix or all subdomains by using the **matchType=** parameter.

If given the uri: *netpreserve.org/about/*:

* **matchType=exact** (the default), will return results matching exactly *netpreserve.org/about/*

* **matchType=path** will return all the results whose paths start with *netpreserve.org/about/*

    Example: `http://localhost:8080/resourcelist/netpreserve.org%2Fabout?matchType=path`

* **matchType=host** will return all the results where the host part matches, including subdomains. The path is not  
    taken into account for this match type.

    Example: `http://localhost:8080/resourcelist/netpreserve.org%2Fabout?matchType=host`
    will match any subdomains of *netpreserve.org*, like *web.netpreserve.org*.

#### Sort

By default the result list is sorted in ascending lexical order. By setting **sort=desc**, the order is reversed.

#### Limit

The limit parameter sets the maximum number of records returned. The default is set in `application.conf`.
