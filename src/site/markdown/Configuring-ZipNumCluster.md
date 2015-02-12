OpenWayback [[Advanced configuration]]

## Configuring ZipNumCluster

For information on ZipNum format http://aaron.blog.archive.org/2013/05/28/zipnum-and-cdx-cluster-merging/

Enable and edit CDXCollection.xml as follows:

```xml
    <property name="resourceIndex">
      <bean class="org.archive.wayback.resourceindex.LocalResourceIndex">
        <property name="canonicalizer" ref="waybackCanonicalizer" />
        <property name="source">
        <bean class="org.archive.wayback.resourceindex.ZipNumClusterSearchResultSource">
                <property name="cluster">
                        <bean class="org.archive.format.gzip.zipnum.ZipNumCluster">
                                <property name="summaryFile" value="/<PATH-TO-SUMMARYFILE>"/>
                                <property name="locFile" value="/<PATH-TO-LOCFILE>" />
                        </bean>
                </property>
                <property name="params">
                        <bean class="org.archive.format.gzip.zipnum.ZipNumParams"/>
                </property>
        </bean>
        </property>
        <property name="maxRecords" value="100000" />
        <property name="dedupeRecords" value="true" />    
      </bean>
    </property>
```
<br/>
**Summary file format**

Summary file consists of 4 columns separated by tab as follows:<br/>
1. The first line of each chunk <br/>
2. Chunk name (or shard name) <br/>
3. Offset: the starting byte-offset of the chunk <br/>
4. Length: the length of the chunk <br/><br/>

**Loc file format**

Loc file consists of 2 columns separated by tab as follows: <br/>
1. Chunk name (or shard name) <br/>
2. Chunk URL: e.g. `hdfs://url` or `http://url` <br/><br/>

For more information on how to generate summary file using hadoop, please see link at the top.