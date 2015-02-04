## System Requirements
- **Java:** [Oracle Java version 1.6](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or higher. 
- **Tomcat:** [Apache Tomcat version 6.0](http://tomcat.apache.org/download-60.cgi) or higher.

Please note that the recommended operating environment for OpenWayback is UNIX/Linux and this is assumed in this documentation.

## Download OpenWayback
OpenWayback is packaged as a web application file (.war file). You can choose to download the binary distribution or build from source.

### Downloading the Binary Distribution
All OpenWayback releases are hosted on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.netpreserve.openwayback%22). You can find the latest binary release [here](http://search.maven.org/#browse%7C951206516), currently [openwayback-2.0.0](http://search.maven.org/remotecontent?filepath=org/netpreserve/openwayback/openwayback-dist/2.0.0/openwayback-dist-2.0.0-2.0.0.tar.gz).

Extract the .tar.gz file containing the webapp (.war) file:
`tar -xzvf <filename>.tar.gz`

This will produce a folder named 'openwayback' containing two (2) folders (bin and lib) and the web application file: 'openwayback-(version).war'. 

### Building From Source
You can also build OpenWayback from the source yourself. Just follow these three steps:

#### 1. Get the Source

Option 1 is to use [Git](http://git-scm.com/) to clone the repository. For this Git must be installed on the machine you are using.

Change to a directory where you'd like to download the source and run:

```bash
git clone https://github.com/iipc/openwayback.git
```

Option 2 is to download the source as a [ZIP file](https://github.com/iipc/openwayback/archive/master.zip). 

This way you only download the current main branch of the code.

Change to a directory where you'd like to download the source and run:

```bash
wget https://github.com/iipc/openwayback/archive/master.zip;
mv master.zip openwayback.zip;
unzip openwayback.zip;
mv openwayback-master openwayback;
```

In either case you'll end up with a directory named `openwayback` that contains the source for OpenWayback. 

#### 2. Build OpenWayback
For this step you'll need [Apache Maven 2](http://maven.apache.org/) or higher to be able build OpenWayback .

Change into the `openwayback` directory created in step 1 and run:

```bash
mvn package
```

This will build OpenWayback, running all unit tests along the way. 

To skip unit test add `-Dmaven.test.skip=true` to the command. This is mostly useful on Windows machines as some unit tests can fail on Windows machines.

OpenWayback consists of several sub-modules, all of which are built when this method is invoked in the root of the project. You can also build the individual modules by changing into those directories, but this may cause Maven to fetch other OpenWayback modules from online repositories, rather than building on the downloaded code.

#### 3. Locate the Build Artifacts
Once you've built OpenWayback as described above, you can find the full binary distribution under `openwayback/dist/target/`. You can then untar it and use in the same way that you'd use a pre-built distribution downloaded from the Internet and discussed above.

If you just need the WAR file (and not the various command line tools, such as the `cdx-indexer`) you can get that directly from `openwayback/wayback-webapp/target/`. It is the same file as the one added to the distribution tarball.


## Installation
Apache Tomcat is required to run OpenWayback. Please refer to the README file in your Tomcat distribution for instructions. The instructions below assume that Tomcat is installed under the directory $CATALINA_HOME.

You must rename the .war web application file to **ROOT.war** before deploying it to Tomcat.

Please follow these steps:

1. Locate the .war file you built or downloaded. Rename it to **ROOT.war**.
2. Place the ROOT.war file in the `webapps` folder of Tomcat, usually `$CATALINA_HOME/webapps/`.
3. Wait for Tomcat to unpack the .war file.
4. Customise configuration file `wayback.xml` and possibly other XML configuration files. See [How to configure](How-to-configure.html) for details.
5. Restart Tomcat.

OpenWayback can also be deployed in a non-ROOT context. Please see [Deploying OpenWayback in non-ROOT](Deploying-OpenWayback-in-non-ROOT-Context.html)  for details.
