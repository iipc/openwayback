### Wayback Capture Tools Overview

This project contains tools that allow wayback to save and record archive data as a 'fallback', in response to a request for a missing capture.

The tools in this project:

* Retrieve content from the live web (using Apache HttpClient 3.x)

* Optionally dedup content against the current cdx (if a cdx server is provided)

* Write response/revisit and request records to a warc file using the JWAT library warc writer.


The `InstaLiveWeb` access point is the controller for these operations, and maintains an instance of
`InstaLiveWebWarcWriter` which handles the writing.
The system is designed to write to a single warc at time. Data from remote server is buffered locally
(either to disk or in memory), and then synchronously written to the warc.

When warc reaches `maxFileSize`, a new warc is created.

The writer holds a `FileLock` on the warc until it is done.


### Requirements

To use these tools,  Wayback configuration must be:

* Configured with a `cdxServer` to handle on the fly deduplication

* Provide an implementation of `InstaPersistCache`


### Persist Cache

The `InstaPersistCache` is a CDX input source that can also write cdx data:

```java
public interface InstaPersistCache extends CDXInputSource {

	public boolean saveResult(CaptureSearchResult result);
}
```

It needs to be ablt to store and retrieve cdxs given a cdx key. (Range queries are not required).



### Possible Usage

Below is a sketch of a possible configuration:



1. First, assuming the following properties are defined


```
wayback.path=/wayback/
wayback.recordPrefix=/record/
wayback.recordEmbedPrefix=/record/_embed/
```


2. Add `InstaLiveWebWarcWriter` and `InstaLiveWeb` access point:

   Note the references to `cdxServer` and `instaCache` which are not included in this example.


```xml

  <bean name="livewebWarcWriter" class="org.archive.wayback.instantliveweb.InstaLiveWebWarcWriter">
    <property name="nameVersion" value="LiveWeb Warc Writer 1.0"/>
    <property name="maxFileSize" value="524288000"/>
    <property name="maxResponseSize" value="209715200"/>
    <property name="warcOutDir" value="${wayback.livewebWarcs}"/>
    <property name="warcPrefix" value="live"/>
    <property name="cdxDedupServer" ref="cdxServer"/>
    <property name="isPartOf" value="liveweb"/>
  </bean>

  <bean name="liveWebAccessPoint" class="org.archive.wayback.instantliveweb.InstaLiveWeb">
    <property name="accessPointPath" value="${wayback.recordPrefix}"/>
    <property name="internalPort" value="${wayback.port}"/>   
    
    <property name="recordUserAgent" value="OpenWayback User Agent"/>
    <property name="inner" ref="waybackAccessPoint" />
    
    <property name="persistCache" ref="instaCache"/>
    <property name="warcWriter" ref="livewebWarcWriter"/>
    
    <property name="uriConverter">
      <bean class="org.archive.wayback.instantliveweb.InstaArchiveUriConverter">
        <property name="recordPrefix" value="${wayback.recordEmbedPrefix}"/>
      </bean>
    </property>           
 </bean>
```



3. Replace the `ServerRelativeArchivalRedirect` post-processor with:

```xml
  <bean name="+" class="org.archive.wayback.instantliveweb.InstaRelativeRedirect">
    <property name="matchPort" value="${wayback.port}" />
    <property name="useCollection" value="true" />
    <property name="replayPrefix" value="${wayback.path}" />
    <property name="record" value="${wayback.recordPrefix}"/>
    <property name="recordEmbed" value="${wayback.recordEmbedPrefix}"/>
  </bean>
```

4. Add reference to the main wayback access point, assumed to be `waybackAccessPoint`:

```xml
    <property name="liveweb" ref="liveWebAccessPoint"/>
```


5. Declare an instance of `InstaPersistCache`. 
   This package includes `InstaRedisCache`, an implementation which uses Redis key store for this purprose.

