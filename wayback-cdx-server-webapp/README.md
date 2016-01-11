# Wayback CDX Server API - BETA #

##### Changelist

* 2013-08-07 -- Add this changelist! Page size is now adjustable [Pagination API](#pagination-api)

* 2013-08-07 -- Added support for [Counters](#counters) and [Field Order](#field-order).

* 2013-08-03 -- Added support for [Collapsing](#collapsing)


##### Table of Contents

#### [Intro and Usage](#intro-and-usage)

* [Changelist](#changelist)

* [Basic usage](#basic-usage)

* [Url Match Scope](#url-match-scope)

* [Output Format (JSON)](#output-format-json)

* [Field Order](#field-order)

* [Filtering](#filtering)

* [Collapsing](#collapsing)

* [Query Result Limits](#query-result-limits)

#### [Advanced Usage](#advanced-usage) 

* [Closest Timestamp Match](#closest-timestamp-match)

* [Resumption Key](#resumption)

* [Resolve Revisits](#resolve-revisits)
  
* [Counters](#counters)

  * [Duplicate Counter](#duplicate-counter)
  
  * [Skip Counter](#skip-counter)

* [Pagination API](#pagination-api)

* [Access Control](#access-control)



## Intro and Usage ##

The `wayback-cdx-server` is a standalone HTTP servlet that serves the index that the `wayback` machine uses to lookup captures.

The index format is known as 'cdx' and contains various fields representing the capture, usually sorted by url and date.
https://archive.org/web/researcher/cdx_file_format.php

The server responds to GET queries and returns either the plain text CDX data, or optionally a JSON array of the CDX.

The CDX server is deployed as part of the Wayback Machine at https://web.archive.org and the examples below reference this deployment.

However, the CDX server software is freely available with the rest of the open-source Wayback Machine software in this repository.

Further documentation will focus on configuration and deployment in other environments.

Please contant us at wwm@archive.org for additional questions.


### Basic Usage ###

The simplest query uses only the single required parameter for the CDX server, **url**:

 * http://web.archive.org/cdx/search/cdx?url=archive.org

This will return index entries, one per row, describing each 'capture' of the url "archive.org" that is available in the archive.

The columns of each line are the fields of the cdx.
At this time, the following cdx fields are publicly available:

  `["urlkey","timestamp","original","mimetype","statuscode","digest","length"]`
  
It is possible to customize the [Field Order](#field-order) as well.

The **url=** value should be [url encoded](http://en.wikipedia.org/wiki/Percent-encoding) if the url itself contains a query.

All the other params are optional and are explained below.

For doing large/bulk queries, the use of the [Pagination API](#pagination-api) is recommended.


### Url Match Scope ###

The default behavior is to return exact url matches. However, the cdx server can also return results matching a certain
prefix, a certain host or all subdomains by using the **matchType=** param.

For example, if given the url: *archive.org/about/* and:
 
 * **matchType=exact** (the default), it will return results matching exactly *archive.org/about/*

 * **matchType=prefix** will make it return all the results whose paths start with *archive.org/about/*

  For example: http://web.archive.org/cdx/search/cdx?url=archive.org/about/&matchType=prefix&limit=1000

 * **matchType=host** will make it return all the results where the host part exactly matches, ignoring the path

   For example: http://web.archive.org/cdx/search/cdx?url=archive.org/about/&matchType=host&limit=1000 will match the host *archive.org* exactly, and ignore the */about/* path.

 * **matchType=domain** will make it return all the results where the host part matches, including subdomains

   For example: http://web.archive.org/cdx/search/cdx?url=archive.org/about/&matchType=domain&limit=1000 will match any subdomains of *archive.org*, like *web.archive.org*.

The matchType may also be set implicitly by using a wildcard '*' at the beginning or end of the url:

 * If the url ends with '/\*', eg **url=archive.org/\*** the query is equivalent to **url=archive.org/&matchType=prefix**
 * If the url starts with '\*.', eg **url=\*.archive.org/** the query is equivalent to **url=archive.org/&matchType=domain**

(Note: The *domain* mode is only available if the CDX is in SURT-order format.)


### Output Format (JSON) ##

* Output: **output=json** can be added to make the cdx server return the results as a JSON array. The JSON output currently also includes an initial line which lists the fields included, and their order. 
  
  For example: http://web.archive.org/cdx/search/cdx?url=archive.org&output=json&limit=3
```
[["urlkey","timestamp","original","mimetype","statuscode","digest","length"],
 ["org,archive)/", "19970126045828", "http://www.archive.org:80/", "text/html", "200", "Q4YULN754FHV2U6Q5JUT6Q2P57WEWNNY", "1415"],
 ["org,archive)/", "19971011050034", "http://www.archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1402"],
 ["org,archive)/", "19971211122953", "http://www.archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1405"]]
```

* By default, the CDX server returns gzip encoded data for all queries. To turn this off, add the **gzip=false** param.

### Field Order ###

It is possible to customize which fields are returned from the cdx server (and their order) by using the **fl=** param.
Simply pass in a comma seperated list of fields and only those fields will be returned:

  For example: http://web.archive.org/cdx/search/cdx?url=archive.org&fl=timestamp,mimetype&output=json returns only the timestamp and mimetype fields with the header `["timestamp","mimetype"]`.

* If the **fl=** param is omitted, all the available fields are returned.


### Filtering ###

* Date Range: Results may be filtered by timestamp using **from=** and **to=** params.
  The ranges are inclusive and are specified in the same 1 to 14 digit format used for `wayback` captures: *yyyyMMddhhmmss*
  
  For example: http://web.archive.org/cdx/search/cdx?url=archive.org&from=2010&to=2011


* Regex filtering: It is possible to filter on a specific field or the entire CDX line (which is space delimited).
  Filtering by a specific field is often simpler.
  Any number of filter params of the following form may be specified: **filter=**[!]*field*:*regex*

  * *field* is one of the named cdx fields (listed in the JSON query) or an index of the field. It is often useful to filter by
    *mimetype* or *statuscode*

  * Optional: a *!* before the query inverts the match, that is, will return results that do NOT match the regex.

  * *regex* is any standard Java regex pattern (http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html)

For example: http://web.archive.org/cdx/search/cdx?url=archive.org&output=json&limit=2&filter=!statuscode:200 will return 2 capture results with non-200 status codes.

 For example: http://web.archive.org/cdx/search/cdx?url=archive.org&output=json&limit=10&filter=!statuscode:200&filter=!mimetype:text/html&filter=digest:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV will return 10 capture results with non-200 status codes and mime types that are not *text/html* but which match a specific content digest

### Collapsing ###

A new form of filtering is the option to 'collapse' results based on a field, or a substring of a field.
Collapsing is done on adjacent cdx lines, causing all matching captures after the first one to be filtered out.
This is useful for simplifying queries whose results are 'too dense' or when looking for unique captures.

To use collapsing, add one or more params of the form **collapse=field** or **collapse=field:N** where N is the first N characters of *field* to test.

For example: http://web.archive.org/cdx/search/cdx?url=google.com&collapse=timestamp:10 will only show at most 1 capture per hour (comparing the first 10 digits of the timestamp field). Given two captures whose timestamps are 20130226010000 and 20130226010800, since the first 10 digits (*2013022601*) match, the second capture will be filtered out.

  The calendar page at web.archive.org uses this filter by default: http://web.archive.org/web/*/archive.org

  For example: http://web.archive.org/cdx/search/cdx?url=archive.org&collapse=digest will only show unique captures (captures with different content digests). Note that only adjacent cdx lines are collapsed, duplicate digests elsewhere in the result set are not affected.

 For example: http://web.archive.org/cdx/search/cdx?url=archive.org&collapse=urlkey&matchType=prefix will only show unique urls from a prefix query (filtering out captures except for the first capture of a given url). This is similar to the old prefix query in the Wayback Machine. (Note that this query may be slow at the moment.)


### Query Result Limits ###

  As the CDX server may return millions or billions of records, it is often necessary to set limits on a single query for practical reasons.
  The CDX server provides several mechanisms for this, including ability to return the last N as well as the first N results.

  * The CDX server config provides a setting for absolute maximum length returned from a single query (currently set to 150000 by default).
 
  * Queries can include the **limit=** *N* param to return the first N results. or the **limit=** *-N* param to return the last N results. The last-N query may be slow as it still reads all the results, and only avoids the cost of returning all but the last N to the client.

    For example: http://web.archive.org/cdx/search/cdx?url=archive.org&limit=-1 will return the last result.

  * *Advanced Option:* **fastLatest=true** may be set to return *some number* of latest results for an exact match and is faster than the standard last results search. The number of results is at least 1 so **limit=-1** implies this setting. The number of results may be greater >1 when a secondary index format (such as ZipNum) is used, but is not guaranteed to return any more than 1 result. Combining this setting with **limit=** will ensure that *no more* than N last results.

 For example: http://web.archive.org/cdx/search/cdx?url=archive.org&fastLatest=true&limit=-5 will return up to 5 of the latest (by date) results, more quickly than with just **limit=-5**.

  * The **offset=** *M* param can be used in conjunction with the **limit=** param to 'skip' the first M records. This allows for a simple way to scroll through the results.

    However, the offset/limit model does not scale well to large querties since the CDX server must read and skip through the number of results specified by **offset**, reqiring it to begin reading at the beginning every time.


## Advanced Usage

The following features are for more specific/advanced usage of the CDX server.


### Resumption Key ###

There is a new method that allows for the CDX server to specify a 'resumption key' that can be used to continue a query from where it previously ended.
This allows a large query to be broken up into smaller queries more efficiently.

  * To show the resumption key add the **showResumeKey=true** param. When set, the resumption key will be included only if the query has more results that were excluded due to a **limit=** param (or the server-side max query limit).

  * At the end of the results, after a blank row, the *<resumption key>* will be output on a seperate line or in a seperate JSON array.

  * Plain text example: http://web.archive.org/cdx/search/cdx?url=archive.org&limit=5&showResumeKey=true
    
```
org,archive)/ 19970126045828 http://www.archive.org:80/ text/html 200 Q4YULN754FHV2U6Q5JUT6Q2P57WEWNNY 1415
org,archive)/ 19971011050034 http://www.archive.org:80/ text/html 200 XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3 1402
org,archive)/ 19971211122953 http://www.archive.org:80/ text/html 200 XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3 1405
org,archive)/ 19971211122953 http://www.archive.org:80/ text/html 200 XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3 1405
org,archive)/ 19980109140106 http://archive.org:80/ text/html 200 XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3 1402
   
org%2Carchive%29%2F+19980109140106%21
```

  * JSON example: http://web.archive.org/cdx/search/cdx?url=archive.org&limit=5&showResumeKey=true&output=json

```
[["urlkey","timestamp","original","mimetype","statuscode","digest","length"],
 ["org,archive)/", "19970126045828", "http://www.archive.org:80/", "text/html", "200", "Q4YULN754FHV2U6Q5JUT6Q2P57WEWNNY", "1415"],
 ["org,archive)/", "19971011050034", "http://www.archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1402"],
 ["org,archive)/", "19971211122953", "http://www.archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1405"],
 ["org,archive)/", "19971211122953", "http://www.archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1405"],
 ["org,archive)/", "19980109140106", "http://archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1402"],
 [],
 ["org%2Carchive%29%2F+19980109140106%21"]]
```

  * In a subsequent query, adding the **resumeKey=** *<resumption key>* param will resume the search from the next result:
    No other params from the original query (such as *from=* or *url=*) need to be altered.
    To continue from the previous example, the subsequent query would be:

    Ex: http://web.archive.org/cdx/search/cdx?url=archive.org&limit=5&showResumeKey=true&resumeKey=org%2Carchive%29%2F+19980109140106%21

### Counters ###

There is some work on custom counters to enchance the aggregation capabilities of the CDX server.
These features are brand new and should be considered experimental.

#### Duplicate Counter ####

While collapsing allows for filtering out adjacent results that are duplicates, it is also possible to track duplicates throughout the cdx using a special new extension.
By adding the **showDupeCount=true** param a new `dupecount` column will be added to the results.

* The duplicates are determined by counting rows with the same `digest` field.

* The `warc/revisit` mimetype in duplicates > 0 will automatically be resolved to the mimetype of the original, if found.

* Using **showDupeCount=true** will only show unique captures: http://web.archive.org/cdx/search/cdx?url=archive.org&showDupeCount=true&output=json&limit=50


#### Skip Counter ####

It is possible to track how many CDX lines were skipped due to [Filtering](#filtering) and [Collapsing](#collapsing)
by turning on the special `skipcount` column with **showSkipCount=true**. 
An optional `endtimestamp` column can also be shown, containing the timestamp of the most recent capture, by adding **lastSkipTimestamp=true**

* Ex: Collapse results by year and print number of additional captures skipped and timestamp of last capture:

  http://web.archive.org/cdx/search/cdx?url=archive.org&collapse=timestamp:4&output=json&showSkipCount=true&lastSkipTimestamp=true


### Pagination API ###

The resumption key described above allows for the sequential querying of CDX data.
However, in some cases where very large result sets are needed (for example with a **matchType=domain** query), it may be useful to perform queries in parallel and also to estimate the total size of the query without running it in full.

The `wayback` and `cdx-server` software support secondary loading from a 'zipnum' CDX index.
Such an index contains CDX lines stored in concatenated GZIP blocks (usually 3000 lines each) and a secondary index
which provides a binary search to the 'zipnum' blocks.
By using the secondary index, it is possible to estimate the total size of a query and also to break up the query.
Using the zipnum format or another secondary index is needed to support pagination.

However, pagination can only work on a single index at a time. Merging input from multiple sources (plain cdx or zipnum)
is not possible with pagination. As such, the results from the paginated version may be slightly less up-to-date than
the default non-paginated query.

  * To use pagination, simply add the **page=i** param to the query to return the i-th page. If pagination is not supported, the cdx server will return a 400 HTTP status code.

  * Pages are numbered from 0 to *num pages - 1*. Providing a negative page number disables pagination for the query. If *i>=num pages*, no results are returned.

    Ex: First page: http://web.archive.org/cdx/search/cdx?url=archive.org&page=0

    Ex: Next Page: http://web.archive.org/cdx/search/cdx?url=archive.org&page=1


  * To determine the number of pages, add the **showNumPages=true** param. This is a special query that will return a single number indicating the number of pages.

    Ex: http://web.archive.org/cdx/search/cdx?url=archive.org&showNumPages=true
  
  * Page size (number of results per page) is configured to an optimal value on the cdx server, and may be similar to max query limit in non-paged mode. The CDX server on archive.org has a page size of 50 currently.
  
  * It is possible to adjust the page size to a smaller value than the default by setting the **pageSize=P** where 1 <= P <= default page size.
  
    Ex: Get # of pages with smallest page size: http://web.archive.org/cdx/search/cdx?url=archive.org&showNumPages=true&pageSize=1
    
    Ex: Get first page with smallest page size: http://web.archive.org/cdx/search/cdx?url=archive.org&page=0&pageSize=1
    

  * If there is only one page, adding the **page=0** param will return the same results as without setting a page.

  * It is also possible to have the cdx server return the raw secondary index, by specifying the **showPagedIndex=true** param. This query returns the secondary index instead of the cdx results and may be subject to access restrictions.

  * All other params, including **resumeKey=**, should work in conjunction with pagination.



### Access Control ###

The cdx server is designed to improve access to archived data by a broad audience, but it may be necessary to restrict certain parts of the cdx data.

The cdx server provides a way to grant permissions to access restricted data via an API key that is passed in as a cookie.

Currently two restrictions/permission types are supported:

* Access to certain urls which are considered private. When restricted, only public urls are included in query results and access to the secondary index is restricted.

* Access to certain fields, such as the filename in the CDX. When restricted, the cdx results contain only public fields.


To allow access, the API key cookie must be explicitly set on the client, eg:

```
curl -H "Cookie: cdx-auth-token=API-Key-Secret http://mycdxserver/search/cdx?url=..."
```

The *API-Key-Secret* can be set in the cdx server configuration.


## CDX Server Configuration ##


TODO

Start by editing the wayback-cdx-server-servlet.xml file in the WEB-INF Directory. Just put some valid CDX-Files in the cdxUris-List (Files must end with cdx or cdx.gz!) 


