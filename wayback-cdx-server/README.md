# Wayback CDX Server API 1.0 - BETA #


##### Table of Contents

#### [Intro and Usage](#intro-and-usage)

* [Basic usage](#basic-usage)

* [Url Match Scope](#url-match-scope)

* [Output Format (JSON)](#output-format-json)

* [Filtering](#filtering)

* [Query Result Limits](#query-result-limits)

* [Resumption Key](#resumption)


#### [Advanced Usage](#advanced-usage) 

* [Paging API](#paging-api)

* [Access Control](#access-control)



## Intro and Usage ##

The `wayback-cdx-server` is a standalone HTTP servlet that serves the index that the `wayback` machine uses to lookup captures.

The index format is known as 'cdx' and contains various fields representing the capture, usually
sorted by url and date.
http://archive.org/web/researcher/cdx_file_format.php

The server responds to GET queries and returns either the plain text CDX data, or optionally a JSON array of the CDX.

The CDX server is deployed as part of web.archive.org Wayback Machine and the usage below reference this deployment.

However, the cdx server is freely available with the rest of the open-source wayback machine software in this repository.

Further documentation will focus on configuration and deployment in other environments.

Please contant us at wwm@archive.org for additional questions.


### Basic Usage ###

The most simple query and the only required param for the CDX server is the **url** param

 * http://web.archive.org/cdx/search/cdx?url=archive.org

The above query will return a portion of the index, one per row, for each 'capture' of the url "archive.org"
that is available in the archive.

The columns of each line are the fields of the cdx.
At this time, the following cdx fields are publicly available:

  `["urlkey","timestamp","original","mimetype","statuscode","digest","length"]`


The the **url=** value should be [url encoded](http://en.wikipedia.org/wiki/Percent-encoding) if the url itself contains a query.

All other params are optional and are explained below.


For doing large/bulk queries, the use of the [Paging API](#paging-api) is recommended.


### Url Match Scope ###

The default behavior is to return matches for an exact url. However, the cdx server can also return results matching a certain
prefix, a certain host or all subdomains by using the **matchType=** param.

For example, if given the url: *archive.org/about/* and:
 
 * **matchType=exact** (default if omitted) will return results matching exactly *archive.org/about/*

 * **matchType=prefix** will return results for all results under the path *archive.org/about/*

   http://web.archive.org/cdx/search/cdx?url=archive.org/about/&matchType=prefix&limit=1000

 * **matchType=host** will return results from host archive.org

   http://web.archive.org/cdx/search/cdx?url=archive.org/about/&matchType=host&limit=1000

 * **matchType=domain** will return results from host archive.org and all subhosts *.archive.org

   http://web.archive.org/cdx/search/cdx?url=archive.org/about/&matchType=domain&limit=1000


The matchType may also be set implicitly by using wildcard '*' at end or beginning of the url:

 * If url is ends in '/\*', eg **url=archive.org/\*** the query is equivalent to **url=archive.org/&matchType=prefix**
 * if url starts with '\*.', eg **url=\*.archive.org/** the query is equivalent to **url=archive.org/&matchType=domain**

(Note: The *domain* mode is only available if the CDX is in SURT-order format.)


### Output Format (JSON) ##

* Output: **output=json** can be added to return results as JSON array. The JSON output currently also includes a first line which indicates the cdx format. 
  
  Ex: http://web.archive.org/cdx/search/cdx?url=archive.org&output=json&limit=3
```
[["urlkey","timestamp","original","mimetype","statuscode","digest","length"],
 ["org,archive)/", "19970126045828", "http://www.archive.org:80/", "text/html", "200", "Q4YULN754FHV2U6Q5JUT6Q2P57WEWNNY", "1415"],
 ["org,archive)/", "19971011050034", "http://www.archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1402"],
 ["org,archive)/", "19971211122953", "http://www.archive.org:80/", "text/html", "200", "XAHDNHZ5P3GSSSNJ3DMEOJF7BMCCPZR3", "1405"]]
```

* By default, CDX server returns gzip encoded data for all queries. To turn this off, add the **gzip=false** param


### Filtering ###

* Date Range: Results may be filtered by timestamp using **from=** and **to=** params.
  The ranges are inclusive and are specified in the same 1 to 14 digit format used for `wayback` captures: *yyyyMMddhhmmss*
  
  Ex: http://web.archive.org/cdx/search/cdx?url=archive.org&from=2010&to=2011


* Regex filtering: It is possible to filter on a specific field or the entire CDX line (which is space delimited).
  Filtering by specific field is often simpler.
  Any number of filter params of the following form may be specified: **filter=**[!]*field*:*regex* may be specified.

  * *field* is one of the named cdx fields (listed in the JSON query) or an index of the field. It is often useful to filter by
    *mimetype* or *statuscode*

  * Optional: *!* before the query inverts the match, that is, will return results that do NOT match the regex.

  * *regex* is any standard Java regex pattern (http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html)


* Ex: Query for 2 capture results with a non-200 status code:
    
  http://web.archive.org/cdx/search/cdx?url=archive.org&output=json&limit=2&filter=!statuscode:200


* Ex: Query for 10 capture results with a non-200 status code and non text/html mime type matching a specific digest:
    
  http://web.archive.org/cdx/search/cdx?url=archive.org&output=json&limit=10&filter=!statuscode:200&filter=!mimetype:text/html&filter=digest:2WAXX5NUWNNCS2BDKCO5OVDQBJVNKIVV


### Query Result Limits ###

  As the CDX server may return millions or billions of record, it is often necessary to set limits on a single query for practical reasons.
  The CDX server provides several mechanisms.

  * The CDX server config provides a setting for absolute maximum length returned from a single query (currently set to 150000 by default).
 
  * The **limit=** *N* param, used is a simple way to limit the number of matches returned in a query to N, where N>0.

  * The **offset=** *M* param can be used in conjunction with limit to 'skip' the first M records. This allows for a simple way to scroll through the results.

However, the offset/limit model does not scale well to large querties since the CDX server must read and skip through the number of results specified by
**offset**, so the CDX server begins reading at the beginning every time.


### Resumption Key ###

There is also a new method that allows for the CDX server to specify 'resumption key' that can be used to continue the query from the previous end.
This allows breaking up a large query into smaller queries more efficiently.
This can be achieved by using **showResumeKey=** and **resumeKey=** params

  * To show the resumption key add **showResumeKey=true** param. When set, the resume key will be printed only if the query has more results that have not be printed due to **limit=** (or max query limit) number of results reached.

  * After the end of the query, the *<resumption key>* will be printed on a seperate line or seperate JSON query.

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

  * In a subsequent query, adding **resumeKey=** *<resumption key>* will resume the search from the next result:
    No other params from the original query (such as *from=* or *url=*) need to be altered
    To continue from the previous example, the subsequent query would be:

    Ex: http://web.archive.org/cdx/search/cdx?url=archive.org&limit=5&showResumeKey=true&resumeKey=org%2Carchive%29%2F+19980109140106%21


## Advanced Usage

### Paging API ###

The above resume key allows for sequential querying of CDX data.
However, in some cases where very large querying is needed (for example domain query), it may be useful to perform queries
in parallel and also estimate the total size of the query.

`wayback` and `cdx-server` support a secondary loading from a 'zipnum' CDX index.
Such an index contains CDX lines store in concatenated GZIP blocks (usually 3000 lines each) and a secondary index
which provides binary search to the 'zipnum' blocks.
By using the secondary index, it is possible to estimate the total size of a query and also break up the query in size.
Using the zipnum format or other secondary index is needed to support paging API.


  * To use paging, simply add the **page=i** param to the query to return the i-th page. If the paging API is not supported, cdx server will return a 400.

  * Pages are numbered from 0 to *num pages - 1*. If *i<0*, pages are not used. If *i>=num pages*, no results are returned.

    Ex: First page: http://web.archive.org/cdx/search/cdx?url=archive.org&page=0

    Ex: Next Page: http://web.archive.org/cdx/search/cdx?url=archive.org&page=1


  * To determine the number of pages, add the **showNumPages=true** param. This is a special query that will return a single number indicating the number of pages

    Ex: http://web.archive.org/cdx/search/cdx?url=archive.org&showNumPages=true
  
  * Page size (number of results per page) is fixed and is set by cdx server, and may be similar to max query limit in non-paged mode.

  * If there is only one page, adding the **page=0** param will return the same results as without setting a page.

  * It is also possible to have the cdx server return the raw secondary index, by specifying **showPagedIndex=true**. This query returns the secondary index instead of the cdx results and may be subject to access restrictions.

  * All other params, including the resumeKey= should work in conjunction with the paging.



### Access Control ###

The cdx server is designed to improve access to archival data.

However, in certain cases restrictions are necessary.

As such, The cdx server supports granting access via a custom auth cookie as part of the server configuration.

Currently there are two possible restrictions:

* Access to certain urls which are considered private. When restricted, only public urls are included in query results and access to secondary index is restricted.

* Access to certain fields, such as filename in the CDX. When restricted, the cdx results contain only public fields.


To allow access, the cookie must be explicitly set on the client, eg:

```
curl -H "Cookie: cdx-server-auth-cookie=secret-value http://mycdxserver/search/cdx?url=..."
```

The name of *cdx-server-auth-cookie* and *secret-value* can be configured in the CDX server config.


## CDX Server Configuration ##

TODO




