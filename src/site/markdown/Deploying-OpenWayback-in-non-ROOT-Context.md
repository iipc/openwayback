OpenWayback by default assumes deployment as ROOT context (ROOT.war) with an AccessPoint called wayback. The default URL for accessing OpenWayback is: `http://localhost:8080/wayback/`.

OpenWayback can also be installed in a non-ROOT context (e.g. mycontext). For this `wayback.xml` needs to be configured to accordingly. The default AccessPoint URL would then be `http://localhost:8080/mycontext/wayback/`.

### Configuring wayback.xml for deployment in a non-ROOT context

Edit `wayback.xml` as follows to add `wayback.url.context` and change the `wayback.url.prefix` to include `mycontext`:

```xml
  <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <property name="properties">
      <value>
        ...
      wayback.url.scheme=http
      wayback.url.host=localhost
      wayback.url.port=8080
      wayback.url.context=mycontext
      wayback.url.prefix=${wayback.url.scheme}://${wayback.url.host}:${wayback.url.port}/${wayback.url.context}
      </value>
    </property>
  </bean>
```

Then in the AccessPoint bean in `wayback.xml`, change `accessPointPath`, `replayPrefix`, `queryPrefix`, `staticPrefix`, and `replayURIPrefix` as follows:

```xml
  <bean name="standardaccesspoint" class="org.archive.wayback.webapp.AccessPoint">
    <property name="accessPointPath" value="/wayback/"/>
    ...
    <property name="replayPrefix" value="/${wayback.url.context}/wayback/" />
    <property name="queryPrefix" value="/${wayback.url.context}/wayback/" />
    <property name="staticPrefix" value="/${wayback.url.context}/wayback/" />
    ...
    <property name="replayURIPrefix" value="/${wayback.url.context}/wayback/"/>
    ...
```
Restart Tomcat.