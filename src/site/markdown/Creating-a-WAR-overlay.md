**Note:** The following assumes a degree of familiarity with the [Maven](http://maven.apache.org/) build management tool.

## What is a WAR overlay?

In fact, what is a WAR file? 

A [WAR file](https://en.wikipedia.org/wiki/WAR_%28file_format%29) (Web application ARchive) is very much like a Java JAR file, in that it is a ZIP file with a known internal structure.

It contains all the files needed to launch a Java based website in a servlet container/web server (like [Apache Tomcat](https://tomcat.apache.org/)).

An overlay of a WAR file, is simply a Maven project that uses another project's WAR output as a dependency, rather than a project's JAR. When the overlay project is built, the underlying project's WAR file is exploded and files in the overlay project added to it. If an overlay project has a file with the same path and name as a file in the underlying WAR it will replace it.

Thus the overlay project is literally that, it is a set of files that are laid over the original WAR. Some files may be new, others will replace what was already in the WAR file.

## Why use WAR overlays when customizing OpenWayback?

The files needed to customize OpenWayback are all stored in the WAR deployment. Most notably there is the `WEB-INF/wayback.xml` file that has almost all configuration options and `WEB-INF/classes/WaybackUI.properties` that contains localization strings.

These files can be edited after deployment to a servlet container, assuming it is set to explode WAR files (Tomcat does this by default), but there is a danger that each time an update is deployed, the existing configuration will be overwritten and lost.

A naive way to avoid this, would be to have your own institutional fork of OpenWayback that contains your configurations. The build of your fork is then pushed to production machines.

This is better, as it lets you manage configurations easily in version control software and also enables much more extensive customizations as you can tinker with any aspect of OpenWayback.

The downside is that it can be hard to keep this in sync with changes happening in OpenWayback. It can also be difficult to see exactly where you've made modifications to OpenWayback.

The WAR overlay eases that problem. Instead of having all of OpenWayback to deal with, your overlay project will only have the code that is uniquely relevant to your deployment.

## How does it work?

Simply create a new [Maven webapp project](https://maven.apache.org/guides/mini/guide-webapp.html) and add a dependency on the OpenWayback WAR like so:

```xml
<dependency>
	<groupId>org.netpreserve.openwayback</groupId>
	<artifactId>openwayback-core</artifactId>
	<version>2.0.0</version>
</dependency>
<dependency>
	<groupId>org.netpreserve.openwayback</groupId>
	<artifactId>openwayback-webapp</artifactId>
	<version>2.0.0</version>
	<type>war</type>
	<scope>runtime</scope>
</dependency>
```

The first dependency is on the core libraries in Wayback and the second is the actual Wayback web application WAR file.
Note that versions should specify the same release of OpenWayback you wish to deploy.

Then in the POM's plugin section, add:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-war-plugin</artifactId>
    <version>2.1.1</version>
    <configuration>
        <overlays>
            <overlay>
                <groupId>org.netpreserve.openwayback</groupId>
                 <artifactId>openwayback-webapp</artifactId>
            </overlay>
        </overlays>
    </configuration>
</plugin>
```
You can now replace files in OpenWayback by adding them in the same place in your project. E.g. the `wayback.xml` file is located under `src/main/webapp/WEB-INF/wayback.xml` and the `WaybackUI.properties` file can be found under `src/main/resources/WaybackUI.properties`.

Any file in the `wayback-webapp` sub-project can thus be replaced. 

You can also add your own JSP pages and even Java classes. Doing so will, however, require modifying the `wayback.xml` more extensively to wire the new functionality in. Basically, any aspect of the web application can be overridden.

Keep in mind that you can not overlay classes from the wayback-core project. In some cases, you can however provide replacements and wire them in via the `wayback.xml` file.

Running:

```bash
$ mvn package
```

Should then generate a WAR file that contains your customizations and is ready for deployment. 

Note that this only generates the WAR file for deployment in Tomcat. If you are using any of the command line tools provided with OpenWayback (e.g. to build CDXs) you'll still need the regular build of OpenWayback to access them.

## Sample overlay project

An example of doing this is available as a sample project here on Github: https://github.com/iipc/openwayback-sample-overlay.

## Working in Eclipse

[Eclipse](http://www.eclipse.org/) is the IDE most commonly used when working on OpenWayback. You are free to use any Java IDE however. 

The most recent (4.3.2) versions of Eclipse come with full support for Maven and Git. If you download the [Eclipse IDE for Java EE Developers](http://www.eclipse.org/downloads/) it will support the webapp nature of the project, including how to deploy WAR overlay projects to servers. Older versions of Eclipse may require additional plugins to fully support WAR overlays.

## Having different configurations in development and production

You may wish to have different configuration set in development, testing and production. This is a bit difficult when all the configuration needs to be baked into the WAR file.

It is possible to move some of this configuration outside the WAR file, by leveraging the fact that the `wayback.xml` file is in fact a [Spring](http://projects.spring.io/spring-framework/) configuration file. Notably, by using `org.springframework.beans.factory.config.PropertyPlaceholderConfigurer`.

Insert this near the top of your `wayback.xml`:

```xml
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
	<property name="location" value="file:${user.home}/wayback.properties" />
</bean>
```

This will cause OpenWayback to load all key/value pairs from the file specified. By using `${user.home}` we ensure that this location is platform agnostic.

Place inside this file any deployment specific properties, like port number, location of CDX files etc.

Then, later in the `wayback.xml` you can refer to any value in this properties file by its key, like thus: 

```xml
<property name="resourceStore">
	<bean class="org.archive.wayback.resourcestore.LocationDBResourceStore">
		<property name="db">
			<bean class="org.archive.wayback.resourcestore.locationdb.FlatFileResourceFileLocationDB">
				<property name="path" value="${wayback.archivespath}" />
			</bean>
		</property>
	</bean>
</property>
```

Here the location of the resource store is given by the key `wayback.archivespath`. 

It is recommended that you include a default properties file in your project. This serves as a template when deploying in a new location without any existing configuration.