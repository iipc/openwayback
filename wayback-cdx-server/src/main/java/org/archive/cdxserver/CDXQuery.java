package org.archive.cdxserver;

import javax.servlet.http.HttpServletRequest;

import org.archive.url.UrlSurtRangeComputer.MatchType;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;

public class CDXQuery {
	
	public static String EMPTY_STRING = "";

	
	String url;
	
	MatchType matchType = null;

	String matchTypeStr = null;

	String from = EMPTY_STRING;
	String to = EMPTY_STRING;
	String closest = EMPTY_STRING;
	int collapseTime = 0;

	boolean gzip = true;
	String output = EMPTY_STRING;

	String[] filter = null;
	String[] collapse = null;

	boolean showDupeCount =  false;
    boolean resolveRevisits = false;
	boolean showSkipCount = false;
	boolean lastSkipTimestamp = false;
	
	int offset = 0;
	int limit = 0;
	Boolean fastLatest = null;
	boolean reverse = false;
	String fl = EMPTY_STRING;
	
	int page = -1;
	int pageSize = 0;

	boolean showNumPages = false;
	boolean showPagedIndex = false;

	String resumeKey = EMPTY_STRING;
	boolean showResumeKey = false;

	public CDXQuery()
	{
		
	}
	
	public CDXQuery(String url)
	{
		this.url = url;
	}	
		
	public void fill(HttpServletRequest request) throws ServletRequestBindingException
	{
		url = ServletRequestUtils.getRequiredStringParameter(request, "url");

		String matchTypeStr = ServletRequestUtils.getStringParameter(request, "matchType", null);
		
		if (matchTypeStr != null) {
			matchType = MatchType.valueOf(matchTypeStr);
		}

		from = ServletRequestUtils.getStringParameter(request, "from", "");
		to = ServletRequestUtils.getStringParameter(request, "to", "");
		closest = ServletRequestUtils.getStringParameter(request, "closest", "");
		collapseTime = ServletRequestUtils.getIntParameter(request, "collapseTime", 0);

		gzip = ServletRequestUtils.getBooleanParameter(request, "gzip", true);
		output = ServletRequestUtils.getStringParameter(request, "output", "");

		filter = ServletRequestUtils.getStringParameters(request, "filter");
		collapse = ServletRequestUtils.getStringParameters(request, "collapse");
		
		showDupeCount = ServletRequestUtils.getBooleanParameter(request, "showDupeCount", false);
        resolveRevisits = ServletRequestUtils.getBooleanParameter(request, "resolveRevisits", false);
		showSkipCount = ServletRequestUtils.getBooleanParameter(request, "showSkipCount", false);
		lastSkipTimestamp = ServletRequestUtils.getBooleanParameter(request, "lastSkipTimestamp", false);
		
		offset = ServletRequestUtils.getIntParameter(request, "offset", 0);
		limit = ServletRequestUtils.getIntParameter(request, "limit", 0);
		fastLatest = ServletRequestUtils.getBooleanParameter(request, "fastLatest", false);
		reverse = ServletRequestUtils.getBooleanParameter(request, "reverse", false);
		fl = ServletRequestUtils.getStringParameter(request, "fl", "");
		
		page = ServletRequestUtils.getIntParameter(request, "page", -1);
		pageSize = ServletRequestUtils.getIntParameter(request, "pageSize", 0);

		showNumPages = ServletRequestUtils.getBooleanParameter(request, "showNumPages", false);
		showPagedIndex = ServletRequestUtils.getBooleanParameter(request, "showPagedIndex", false);

		resumeKey = ServletRequestUtils.getStringParameter(request, "resumeKey", "");
		showResumeKey = ServletRequestUtils.getBooleanParameter(request, "showResumeKey", false);		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	public String getMatchTypeStr() {
		return matchTypeStr;
	}

	public void setMatchTypeStr(String matchTypeStr) {
		this.matchTypeStr = matchTypeStr;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getClosest() {
		return closest;
	}

	public void setClosest(String closest) {
		this.closest = closest;
	}

	public boolean isGzip() {
		return gzip;
	}

	public void setGzip(boolean gzip) {
		this.gzip = gzip;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String[] getFilter() {
		return filter;
	}

	public void setFilter(String[] filter) {
		this.filter = filter;
	}

	public String[] getCollapse() {
		return collapse;
	}

	public void setCollapse(String[] collapse) {
		this.collapse = collapse;
	}

	public boolean isShowDupeCount() {
		return showDupeCount;
	}

	public void setShowDupeCount(boolean showDupeCount) {
		this.showDupeCount = showDupeCount;
	}

	public boolean isResolveRevisits() {
		return resolveRevisits;
	}

	public void setResolveRevisits(boolean resolveRevisits) {
		this.resolveRevisits = resolveRevisits;
	}

	public boolean isShowSkipCount() {
		return showSkipCount;
	}

	public void setShowSkipCount(boolean showSkipCount) {
		this.showSkipCount = showSkipCount;
	}

	public boolean isLastSkipTimestamp() {
		return lastSkipTimestamp;
	}

	public void setLastSkipTimestamp(boolean lastSkipTimestamp) {
		this.lastSkipTimestamp = lastSkipTimestamp;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public Boolean getFastLatest() {
		return fastLatest;
	}

	public void setFastLatest(Boolean fastLatest) {
		this.fastLatest = fastLatest;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public String getFl() {
		return fl;
	}

	public void setFl(String fl) {
		this.fl = fl;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isShowNumPages() {
		return showNumPages;
	}

	public void setShowNumPages(boolean showNumPages) {
		this.showNumPages = showNumPages;
	}

	public boolean isShowPagedIndex() {
		return showPagedIndex;
	}

	public void setShowPagedIndex(boolean showPagedIndex) {
		this.showPagedIndex = showPagedIndex;
	}

	public String getResumeKey() {
		return resumeKey;
	}

	public void setResumeKey(String resumeKey) {
		this.resumeKey = resumeKey;
	}

	public boolean isShowResumeKey() {
		return showResumeKey;
	}

	public void setShowResumeKey(boolean showResumeKey) {
		this.showResumeKey = showResumeKey;
	}

	public int getCollapseTime() {
		return collapseTime;
	}

	public void setCollapseTime(int collapseTime) {
		this.collapseTime = collapseTime;
	}
}
