# Advanced configuration

This page deals with advanced configuration options. Many of these require a considerable understanding of how OpenWayback operates under the hood. For the basics of getting OpenWayback up and running, see [[How to configure]].

## Configuring indexes

I.e. how OpenWayback goes from an URL being requested and to serving up either a list of matches or the actual content captured for that URL. The topics discussed in the basic configuration are mostly related to this. 

Additional topics on this are:

* [[Configuring ZipNumCluster]]

## Configuring access points

OpenWayback uses access points to handle incoming request. There can be multiple access points each with a different context (URL prefix) or even operating on different ports (Tomcat needs to be configured accordingly). 

By default only the standard `AccessPoint` handler is enabled. It performs the basic OpenWayback functionality that you are probably already familiar with. 

The behavior of the standard AccessPoint can however be modified by specifying renderers and parsers. 

Alternative and additional access point configurations are provided in OpenWayback for things like [Memento](http://mementoweb.org/) support, proxy mode and more. It is also possible to add your own request handler if you so choose. This is best done via a [WAR overlay](Creating-a-WAR-overlay.html).

For more see:

* [[AccessPoint overview]]
* [[Creating a WAR overlay]]

## Other configuration

* [[Deploying OpenWayback in non-ROOT Context]]

