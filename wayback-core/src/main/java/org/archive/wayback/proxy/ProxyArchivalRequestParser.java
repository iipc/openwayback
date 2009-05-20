package org.archive.wayback.proxy;

import java.util.List;

import org.archive.wayback.RequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDateRangeQueryRequestParser;
import org.archive.wayback.requestparser.FormRequestParser;
import org.archive.wayback.requestparser.OpenSearchRequestParser;

public class ProxyArchivalRequestParser extends ProxyRequestParser {
	private ProxyReplayRequestParser prrp = new ProxyReplayRequestParser(this);
	protected RequestParser[] getRequestParsers() {
		prrp.init();
		RequestParser[] theParsers = {
				prrp,
				new PathDatePrefixQueryRequestParser(this),
				new PathDateRangeQueryRequestParser(this),
				new PathPrefixDatePrefixQueryRequestParser(this),
				new PathPrefixDateRangeQueryRequestParser(this),
				new OpenSearchRequestParser(this),
				new FormRequestParser(this) 
				};
		return theParsers;
	}
	public List<String> getLocalhostNames() {
		return prrp.getLocalhostNames();
	}
	public void setLocalhostNames(List<String> localhostNames) {
		prrp.setLocalhostNames(localhostNames);
	}
}
