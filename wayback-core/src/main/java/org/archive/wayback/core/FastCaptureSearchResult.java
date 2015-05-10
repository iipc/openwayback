package org.archive.wayback.core;

import java.util.Date;

import org.archive.wayback.util.url.UrlOperations;

public class FastCaptureSearchResult extends CaptureSearchResult {
	
	public final static String EMPTY_VALUE = "-";

	protected String urlKey;
	protected String captureTimestamp;
	protected String originalUrl;
	protected String mimeType;
	protected String httpCode;
	protected String digest;
	protected String redirectUrl;
	protected String file;
	protected String robotFlags;
	protected String oraclePolicy;
	
	protected CaptureSearchResult revisitPayload = null;

	private boolean duplicateDigest = false;
	private boolean closest = false;
	
	public FastCaptureSearchResult()
	{
		// Don't autocreate the hashmap in SearchResult here
		super(false);
	}
	
	public String getUrlKey() {
		return urlKey;
	}
	
	public void setUrlKey(String urlKey) {
		this.urlKey = urlKey;
	}
	
	public String getCaptureTimestamp() {
		return captureTimestamp;
	}
	
	public void setCaptureTimestamp(String captureTimestamp) {
		this.captureTimestamp = captureTimestamp;
	}
	
	public String getOriginalUrl() {
		return originalUrl;
	}
	
	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getHttpCode() {
		return httpCode;
	}
	
	public void setHttpCode(String httpCode) {
		this.httpCode = httpCode;
	}
	
	public String getDigest() {
		return digest;
	}
	
	public void setDigest(String digest) {
		this.digest = digest;
	}
	
	public String getRedirectUrl() {
		return redirectUrl;
	}
	
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getRobotFlags() {
		return robotFlags;
	}
	
	public void setRobotFlags(String robotFlags) {
		this.robotFlags = robotFlags;
	}
	
	public long getOffset() {
		return cachedOffset;
	}
	
	public void setOffset(long offset) {
		cachedOffset = offset;
	}
	
	public long getCompressedLength() {
		return cachedCompressedLength;
	}
	
	public void setCompressedLength(long offset) {
		cachedCompressedLength = offset;
	}
	
	public String getOraclePolicy() {
		return oraclePolicy;
	}
	public void setOraclePolicy(String policy) {
		oraclePolicy = policy;
	}

	@Override
	public void flagDuplicateDigest() {
		duplicateDigest = true;
	}

	@Override
	public boolean isRevisitDigest() {
		return duplicateDigest;
	}

	@Override
	public void flagDuplicateDigest(CaptureSearchResult payload) {
		duplicateDigest = true;
		revisitPayload = payload;
		httpCode = payload.getHttpCode();
	}
	
	public CaptureSearchResult getDuplicatePayload()
	{
		return revisitPayload;
	}

	@Override
	public String getDuplicatePayloadFile() {
		return (revisitPayload != null) ? revisitPayload.getFile() : null;
	}

	@Override
	public Long getDuplicatePayloadOffset() {
		return (revisitPayload != null) ? revisitPayload.getOffset() : null;
	}

	@Override
	public long getDuplicatePayloadCompressedLength() {
		return (revisitPayload != null) ? revisitPayload.getCompressedLength() : -1;
	}

	@Override
	public Date getDuplicateDigestStoredDate() {
		return (revisitPayload != null) ? revisitPayload.getCaptureDate() : null;
	}

	@Override
	public String getDuplicateDigestStoredTimestamp() {
		return (revisitPayload != null) ? revisitPayload.getCaptureTimestamp() : null;
	}

	@Override
	public String getOriginalHost() {
		return UrlOperations.urlToHost(getOriginalUrl());
	}

	@Override
	public void setOriginalHost(String originalHost) {

	}

	@Override
	public Date getCaptureDate() {
		// TODO Auto-generated method stub
		return super.getCaptureDate();
	}

	@Override
	public void setCaptureDate(Date date) {
		// TODO Auto-generated method stub
		super.setCaptureDate(date);
	}

	@Override
	public boolean isClosest() {
		return closest;
	}

	@Override
	public void setClosest(boolean value) {
		this.closest  = value;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void flagDuplicateDigest(Date storedDate) {
		super.ensureMap();
		super.flagDuplicateDigest(storedDate);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void flagDuplicateDigest(String storedTS) {
		super.ensureMap();
		super.flagDuplicateDigest(storedTS);
	}

	@Override
	public void flagDuplicateHTTP(Date storedDate) {
		super.ensureMap();
		super.flagDuplicateHTTP(storedDate);
	}

	@Override
	public void flagDuplicateHTTP(String storedTS) {
		super.ensureMap();
		super.flagDuplicateHTTP(storedTS);
	}

	@Override
	public boolean isDuplicateHTTP() {
		super.ensureMap();
		return super.isDuplicateHTTP();
	}
}
