## Wayback Sample Overlay

This project presents an example `mvn` overlay which overlays the the standard wayback-webapp
and allows for easier customizations. The overlay is added on top of the webapp.

This example contains the following files key config files [wayback.xml](src/main/webapp/WEB-INF/wayback.xml)
and [CDXServerCollection.xml](src/main/webapp/WEB-INF/CDXServerCollection.xml)

These files are 'overlayed' on top of the standard webapp build, allowing for customization.

This project can be further modified to create custom setups for Wayback which change various components.



### Latest Configuration

This example presents a 'new'-style Wayback configuration based on the cdx server.

For demo purposes, a single warc and single cdx are included in the [samples](samples) directory, containing
one capture of `http://example.com`

This configuration also includes:

* Properties, such as port and path, configured in `pom.xml` which are accessible in the spring config

* Maven plugins for jetty and tomcat

* Embedded CDX Server


### Installing

You can run `mvn install` in the `wayback-sample-overlay` directory to install.

This will build an installable .war file, `./target/wayback-sample-overlay-VERSION.war` which can be deployed
to a servlet container, such as Apache Tomcat


### Testing Locally

You can also run:

* `mvn jetty:run-war` to start an instance of Jetty for testing

* `mvn tomcat7:run-war` to start an instance of Tomcat 7 for testing


Both commands will build and execute the .war file on the command line.

Wayback will startup on port 8080 (setting can be configured in pom.xml)
and is configured to read the sample cdx and warc. The cdx contains one entry for `http://example.com`

If successful, the following paths will be accessible:

* http://localhost:8080/wayback/*/http://example.com -- Calendar Page

* http://localhost:8080/wayback/20140206032125/http://example.com -- Capture Replay

* http://localhost:8080/cdx?url=example.com -- CDX Query


### Maven Config Details

The [pom.xml](pom.xml) contains the following properties written to a wayback.properties and read by the spring config.

```
    <wayback.port>8080</wayback.port>
    <wayback.cdxPath>/cdx/</wayback.cdxPath>
    <wayback.path>/wayback/</wayback.path>
```

These configs determine the port and path for this sample collection.


### Spring Config Details

This sample is configured using an embedded CDX Server to read the cdx data.

The following are excerpts from [CDXServerCollection.xml](src/main/webapp/WEB-INF/CDXServerCollection.xml)


The cdx are listed as follows:

```xml
  <bean name="cdx" class="org.archive.format.cdx.MultiCDXInputSource">
    <property name="cdxUris">
      <list>
        <value>./sample/cdx/example.cdx</value>
        <!-- include other cdxs here ... -->
      </list>
    </property>
  </bean>
```


The warc reading is configured as follows, allowing wayback to find the warc
in `./sample/warcs/`


```xml
  <bean id="resourceStore" class="org.archive.wayback.resourcestore.JWATFlexResourceStore">
    <property name="sources">
      <list>
       <bean class="org.archive.wayback.resourcestore.FlexResourceStore$PrefixLookup">
          <property name="prefix" value="./sample/warcs/"/>
        </bean>
      </list>
    </property>
    <property name="blockLoader">
      <bean class="org.archive.format.gzip.zipnum.ZipNumBlockLoader">
      </bean>
    </property>
  </bean>
```

