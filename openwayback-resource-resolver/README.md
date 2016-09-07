# OpenWayback Resource Resolver

The Resource Resolver is the first step in modularizing OpenWayback. It serves the purpose of turning a request for an
Uri, date and possibly other parameters into a information record which is used to get the requested resource.

As it is a Web Archive Resource Resolver it is called **WARR** for short.

## Installation

As this service is part of the 3.0.0 release of OpenWayback which is not released yet, you can download a snapshot
release from here:
https://oss.sonatype.org/content/repositories/snapshots/org/netpreserve/openwayback/openwayback-resource-resolver/3.0.0-SNAPSHOT/

Find the latest file ending in ```.tar.gz```

Unpack it with: ```tar zxvf <filename>```

You should now have a directory with a name like ```openwayback-resource-resolver-3.0.0-SNAPSHOT```.

Now put some cdx files in ```openwayback-resource-resolver-3.0.0-SNAPSHOT/cdx/``` and start the Resource Resolver with: ```openwayback-resource-resolver-3.0.0-SNAPSHOT/bin/warr```

## Usage

By default WARR listen on port 8080 on all interfaces. This can be changed by editing ```openwayback-resource-resolver-3.0.0-SNAPSHOT/config/application.conf```.

Some options are common for the endpoints:

* Content type for the response can be one of:
    * **text/html** Used primarly for testing WARR through a browser
    * **application/vnd.org.netpreserve.cdxj**
    * **application/vnd.org.netpreserve.cdx**

* Compression is supported if requested with ```Accept-encoding``` header

### Resolve a resource

```http://localhost:8080/resource/<Url encoded Uri>/<time stamp>```


#### Record Type


#### Limit


### List resources

```http://localhost:8080/resourcelist/<Url encoded Uri>```

#### Date


#### Record type


#### Uri Match Type

The default behavior is to return exact uri matches. However, WARR can also return results matching a path
prefix or all subdomains by using the **matchType=** param.

If given the uri: *netpreserve.org/about/*:
 
 * **matchType=exact** (the default), will return results matching exactly *netpreserve.org/about/*

 * **matchType=path** will return all the results whose paths start with *netpreserve.org/about/*

    Example: ```http://localhost:8080/resourcelist/netpreserve.org%2Fabout?matchType=path```

* **matchType=domain** will return all the results where the host part matches, including subdomains

    Example: ```http://localhost:8080/resourcelist/netpreserve.org%2Fabout?matchType=host```
    will match any subdomains of *netpreserve.org*, like *web.netpreserve.org*.


#### Limit
