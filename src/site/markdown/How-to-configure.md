# Configuring OpenWayback
This document outlines the core configuration element needed to get OpenWayback up and running, including building your CDX index.

For advanced options see [[Advanced configuration]] 

As described in [[How to install]] OpenWayback typically runs inside a [Tomcat](http://tomcat.apache.org/) web server deployed on a Linux or Unix system. The following documentation assumes this is the case and that the reader is generally familiar with both Tomcat and the [bash command shell](https://www.gnu.org/software/bash/) commonly found on Linux and Unix operating systems.

## The configuration file

OpenWayback uses [Spring XML configuration](http://docs.spring.io/spring/docs/3.0.x/reference/xsd-config.html) file. This file is called `wayback.xml` and can be found in the `WEB-INF` folder of the webapp (typically, this would be `$CATALINA_HOME/webapps/ROOT/WEB-INF/wayback.xml`.

The file contains multiple configuration options, with various parts commented out. 

## The bare essentials (or how to use the BDB indexing)

The default configuration that comes with OpenWayback uses a [Berkeley DB](https://en.wikipedia.org/wiki/Berkeley_DB) (BDB) database to store information about where to find your ARC and/or WARC files and an index of their content. It is also configured to automatically populate these indexes.

To get started with OpenWayback you only need to edit a few of the properties specified right near the top of the file:

```xml
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
  <property name="properties">
    <value>
      wayback.basedir=/tmp/openwayback
      
      wayback.archivedir.1=${wayback.basedir}/files1/
      wayback.archivedir.2=${wayback.basedir}/files2/
      
      wayback.url.scheme=http
      wayback.url.host=localhost
      wayback.url.port=8080
      wayback.url.prefix=${wayback.url.scheme}://${wayback.url.host}:${wayback.url.port}
    </value>
  </property>
</bean>
```

* **Directories** These are settings that direct the BDB indexing.
 * **wayback.basedir** should point at a directory where OpenWayback can store its internal state and keep temporary files.

 * **wayback.archivedir** points at two directories where OpenWayback can find ARC and/or WARC files. OpenWayback will search these two directories recursively. By default they are relative to the `basedir` but you may specify any fully qualified path. Do note, it may take OpenWayback a very long time to process a large collection of ARC/WARC files. For larger collections, you should read on about how to use CDX indexes. If you need to specify more than two directories, you will need to edit the `BDBCollection.xml` configuration file. 

* **Web access** These are more general settings telling OpenWayback how the webserver hosting it is configured.

 * **wayback.url.scheme** Whether access to the OpenWayback instance is via `http` or `https`. For `https` you will also need to configure the Tomcat web server accordingly. By default `http` is selected and this is usually the right choice.

 * **wayback.url.host** The host name used to access OpenWayback. Typically the host name of the server running OpenWayback. If you leave it at the default, `localhost`, OpenWayback will only behave normally if accessed from the machine hosting it.

 * **wayback.url.port** The port on which Tomcat is listening. By default this is `8080` which is also the default port for Tomcat.

 * **wayback.url.prefix** Assembles the scheme, host name and port into an URL prefix. You do not need to edit this setting unless you are [deploying OpenWayback in a non-ROOT context](Deploying-OpenWayback-in-non-ROOT-Context.html).

Setting these correctly is enough to get a small collection up and running in OpenWayback. When you restart Tomcat, OpenWayback will automatically index all the ARC and WARC files under `wayback.archivedir.1` and `wayback.archivedir.2` (this can take some time!). You can then view them in a browser at `http://localhost:8080/wayback` (or whatever you configured the scheme, host name and port to be).

This approach is only suitable for very small collections. For larger collections, we recommend the use of CDX indexes.

## CDX indexing (or OpenWayback at scale)

A [CDX file](https://archive.org/web/researcher/cdx_file_format.php) is a simple text file which contains a list of all the URLs in your collections, one URL per line.

A CDX *index* is a *sorted* CDX file.

### Building CDXs

OpenWayback ships with several command line tools in addition to the WAR file. If you untar the OpenWayback distribution into `$WAYBACK_HOME` these can all be found under `$WAYBACK_HOME/bin`.

To generate a CDX file of the contents of a single ARC or WARC you simple invoke:

```
$WAYBACK_HOME/bin/cdx-indexer <ARCHIVE-FILE> <CDX-FILE>
```

Here `$WAYBACK_HOME` refers to the directory created when you untarred the distribution tarball as discussed in the [installation instructions](How-to-install.html). 

This will generate a CDX called `CDX-FILE` for the contents of `ARCHIVE-FILE`.

The `cdx-indexer` does have a few option for configuring the exact nature of the CDX file. However, the default options are typically correct if you are using the `cdx-indexer` from the same OpenWayback distribution as the WAR web application comes from.

This generates an *unsorted* CDX file for one archive. You will need to either manually or (and this is recommended) via script generate a CDX file for each and every ARC and WARC file you have. This process is largely limited by I/O and running multiple `cdx-indexer` instances in parallel can be useful, especially if the CDXs are spread over many physical HDDs. The exact number of instances to use depends entirely on your hardware.

Then you need to merge and sort the resultant files to get a CDX index file. As you'll see, OpenWayback can handle multiple CDX index files. However, we do recommend merging them until each file is at least 10 GB assuming the filesystem allows large files. There is no limit to the size of CDX index files other than those imposed by the filesystem being used. 

To merge and sort CDX files, the [bash `sort`](http://ss64.com/bash/sort.html) command found on most *nix systems is usually used.

**IMPORTANT** If you are using the bash `sort` command, you **must** set the environment variable `LC_ALL=C`. This tells sort *how* to sort and ensures that it matches how OpenWayback expects CDX indexes to be sorted.

This is done by executing the following (or including it in your scripts):
```bash
export LC_ALL=C;
```

### Configuring Wayback to find CDXs

Once you've built your CDX files, sorted and merged them into a small number of CDX index files, you need to configure OpenWayback to use them.

In the `wayback.xml` configuration file, you'll find a block that looks like the following:

```xml
<import resource="BDBCollection.xml"/>
<!--
<import resource="CDXCollection.xml"/>
<import resource="RemoteCollection.xml"/>
<import resource="NutchCollection.xml"/>
-->
```

Start by removing or comment out the `BDBCollection.xml` line and uncommenting the `CDXCollection.xml` line.

E.g.

```xml
<import resource="CDXCollection.xml"/>
<!--
<import resource="BDBCollection.xml"/>
<import resource="RemoteCollection.xml"/>
<import resource="NutchCollection.xml"/>
-->
```

You then need to configure your access point to use the CDX collection. Further down the file you'll find:

```xml
    <property name="collection" ref="localbdbcollection" />
<!--
    <property name="collection" ref="localcdxcollection" />
-->
```

Simply comment out or remove the reference to `localbdbcollection` and uncomment the reference to `localcdxcollection`.

OpenWayback will now expect to find a single CDX index at `${wayback.basedir}/cdx-index/index.cdx`. If you only have one CDX index file you can simply leave it at that.

Alternatively, you can edit the `CDXCollection.xml` configuration file. There you will need to remove the simple `CDXIndex` resource index and enable the `CompositeSearchResultSource` that is commented out.

```xml
<bean class="org.archive.wayback.resourceindex.CompositeSearchResultSource">
  <property name="CDXSources">
    <list>
      <value>${wayback.basedir}/cdx-index/index-1.cdx</value>
      <value>${wayback.basedir}/cdx-index/index-2.cdx</value>
    </list>
  </property>
</bean>
```

As you'll note, you can specify multiple CDX indexes using it. The value line of the list can be repeated as often as is needed.

### Telling OpenWayback where to find your ARC and WARC files

CDX files only contain the name of the ARC and or WARC file that contains the URL. OpenWayback uses what are called `ResourceFileLocationDB` objects to resolve ARC and WARC filenames to actual locations. 

There are several different implementations of this available in OpenWayback (such as the `BDBResourceFileLocationDB` that is used by default). But when using a CDX index, we recommend that the `FlatFileResourceFileLocationDB` be used.

There is a default configuration for this provided near the top of the `wayback.xml` configuration file. Simply remove or comment out the `BDBResourceFileLocationDB` and then uncomment the following:

```xml
<bean id="resourcefilelocationdb" class="org.archive.wayback.resourcestore.locationdb.FlatFileResourceFileLocationDB">
  <property name="path" value="${wayback.basedir}/path-index.txt" />
</bean>
```

As you can see, it reuses the `wayback.basedir` setting and expect to find one file under that path, `path-index.txt`.

This file simply contains a list of all your ARC and WARC filenames (sorted) followed by a tab character and then the full path (including again the filename). This path can be either a filesystem path or an URL. You are allowed to mix filesystem paths and URLs in the same path-index file.

Do note that OpenWayback assumes that the ARC/WARC filenames are unique.

Example of how this file might look:

```
ARCNAME001.arc.gz	/data/ARCS01/ARCNAME001.arc.gz
ARCNAME002.arc.gz	/data/ARCS02/ARCNAME002.arc.gz
WARCNAME001.warc.gz	http://example.com/WARCS01/WARCNAME001.warc.gz
WARCNAME002.warc.gz	http://example.com/WARCS02/WARCNAME001.warc.gz
```

The first field should be as it is in the CDX index (9th or 10th field).

OpenWayback does not provide any special tools for generating this file. It should, however, be fairly straightforward to do so with *nix tools.

The following script is an example of how this might be accomplished assuming all ARC/WARC files share a root directory:

```bash
#!/bin/bash
# Find all ARC/WARC files
ARCHIVE_BASE_DIR=$1;
TARGET_FILE=$2;

tempfile="$TARGET_FILE.tmp";

unset a i
while IFS= read -r -d $'\0' file; do

  archive=$(basename $file);
  echo -e "$archive\t$file" >> $tempfile;

done < <(find $ARCHIVE_BASE_DIR -type f -regex ".*\.w?arc\.gz$" -print0)

# Now sort the file
export LC_ALL=C;
sort $tempfile > $TARGET_FILE;

rm $tempfile
```
