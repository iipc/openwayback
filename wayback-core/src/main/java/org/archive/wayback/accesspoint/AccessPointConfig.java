package org.archive.wayback.accesspoint;

import java.util.List;
import java.util.Properties;

import org.archive.wayback.RequestParser;
import org.archive.wayback.webapp.WaybackCollection;
import org.springframework.beans.factory.BeanNameAware;

public class AccessPointConfig implements BeanNameAware {
	
	private Properties configs = null;
	private List<String> fileIncludePrefixes = null;
	private List<String> fileExcludePrefixes = null;
	private WaybackCollection collection = null;
	private RequestParser requestParser = null;
	private String beanName;
	
	public Properties getConfigs() {
		return configs;
	}
	public void setConfigs(Properties configs) {
		this.configs = configs;
	}
	public List<String> getFileIncludePrefixes() {
		return fileIncludePrefixes;
	}
	public void setFileIncludePrefixes(List<String> fileIncludePrefixes) {
		this.fileIncludePrefixes = fileIncludePrefixes;
	}
	public List<String> getFileExcludePrefixes() {
		return fileExcludePrefixes;
	}
	public void setFileExcludePrefixes(List<String> fileExcludePrefixes) {
		this.fileExcludePrefixes = fileExcludePrefixes;
	}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public String getBeanName() {
		return this.beanName;
	}
	public WaybackCollection getCollection() {
		return collection;
	}
	public void setCollection(WaybackCollection collection) {
		this.collection = collection;
	}
	// Ability to override requestParser per config
	public RequestParser getRequestParser() {
		return requestParser;
	}
	public void setRequestParser(RequestParser requestParser) {
		this.requestParser = requestParser;
	}
	
}
