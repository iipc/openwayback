<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output doctype-public="-//SPRING//DTD BEAN//EN" doctype-system="http://www.springframework.org/dtd/spring-beans.dtd" indent="yes"/>

<xsl:param name="source-file-names" />
<xsl:variable name="names-sequence" select="tokenize($source-file-names,'\|')" />
<xsl:variable name="cfg-files" select="document($names-sequence)" />

<xsl:template match="/">

<beans>
<xsl:for-each select="$cfg-files/collections/collection">

  <!-- Wayback Collection (Common)-->
  <bean name="{path}-accessPoint" parent="waybackAccessPoint">
    <property name="collection" ref="{collBean}" />
    
    <property name="uriConverter">
      <bean class="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter">
        <property name="replayURIPrefix" value="/{path}/"/>
      </bean>
    </property>
    
    <property name="configs">
      <props>
        <prop key="graphJspPrefix">/<xsl:value-of select="path"/>/</prop>
      </props>
    </property>
    
    <xsl:if test="./includes">
      <property name="fileIncludePrefixes">
      <list>
          <xsl:for-each select="./includes/include">
          	<value><xsl:value-of select="./text()"/></value>
    	  </xsl:for-each>  
      </list>
      </property>
    </xsl:if>
        
    <property name="accessPointPath" value="/{path}/"/>
    <property name="replayPrefix" value="/{path}/"/>
    <property name="queryPrefix" value="/{path}/"/>
  </bean>
  
</xsl:for-each>  
</beans>

</xsl:template>


</xsl:stylesheet>
