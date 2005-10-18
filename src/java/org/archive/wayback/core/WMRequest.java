package org.archive.wayback.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.net.UURI;

public class WMRequest {
	private final Pattern IMAGE_REGEX = Pattern
			.compile(".*\\.(jpg|jpeg|gif|png|bmp|tiff|tif)$");

	private String exactDateRequest = null;

	private Timestamp exactTimestamp = null;

	private Timestamp startTimestamp = null;

	private Timestamp endTimestamp = null;

	private String referrerUrl = null;

	private UURI requestURI = null;

	private UURI redirectURI = null;

	private boolean retrieval = false;

	private boolean query = false;

	private boolean pathQuery = false;

	public UURI getRedirectURI() {
		return redirectURI;
	}

	public void setRedirectURI(UURI redirectURI) {
		this.redirectURI = redirectURI;
	}

	public WMRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean isRetrieval() {
		return this.retrieval;
	}

	public boolean isQuery() {
		return this.query;
	}

	public boolean isPathQuery() {
		return this.pathQuery;
	}

	public Timestamp getExactTimestamp() {
		return exactTimestamp;
	}

	public void setExactTimestamp(Timestamp exactTimestamp) {
		this.exactTimestamp = exactTimestamp;
	}

	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(Timestamp endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public String getReferrerUrl() {
		return referrerUrl;
	}

	public void setReferrerUrl(String referrerUrl) {
		this.referrerUrl = referrerUrl;
	}

	public UURI getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(UURI requestURI) {
		this.requestURI = requestURI;
	}

	public Timestamp getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Timestamp startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	private void resetType() {
		this.retrieval = false;
		this.query = false;
		this.pathQuery = false;
	}

	public void setPathQuery() {
		resetType();
		this.pathQuery = true;
	}

	public void setQuery() {
		resetType();
		this.query = true;
	}

	public void setRetrieval() {
		resetType();
		this.retrieval = true;
	}

	public boolean isImageRetrieval() {
		String uri = requestURI.getEscapedURI();
		Matcher matcher = null;
		matcher = IMAGE_REGEX.matcher(uri);
		if (matcher != null && matcher.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getExactDateRequest() {
		return exactDateRequest;
	}

	public void setExactDateRequest(String exactDateRequest) {
		this.exactDateRequest = exactDateRequest;
	}

}
