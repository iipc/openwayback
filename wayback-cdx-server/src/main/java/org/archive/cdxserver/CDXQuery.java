package org.archive.cdxserver;

import javax.servlet.http.HttpServletRequest;

import org.archive.url.UrlSurtRangeComputer;
import org.archive.url.UrlSurtRangeComputer.MatchType;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;

/**
 * container for CDX query parameters.
 * @see CDXServer
 *
 */
public class CDXQuery {

    public static final String EMPTY_STRING = "";

    public enum SortType {
        regular, reverse, closest,
    };

    String url;

    MatchType matchType = null;

    String matchTypeStr = null;

    String from = EMPTY_STRING;
    String to = EMPTY_STRING;
    String closest = EMPTY_STRING;

    SortType sort = null;

    int collapseTime = -1;

    Boolean gzip = null;
    String output = EMPTY_STRING;

    String[] filter = null;
    String[] collapse = null;

    boolean showDupeCount = false;
    boolean resolveRevisits = false;
    boolean showGroupCount = false;
    boolean lastSkipTimestamp = false;
    boolean showUniqCount = false;

    int offset = 0;
    int limit = 0;
    boolean last = false;
    Boolean fastLatest = null;
    String fl = EMPTY_STRING;

    int page = -1;
    int pageSize = 0;

    boolean showNumPages = false;
    boolean showPagedIndex = false;

    String resumeKey = EMPTY_STRING;
    boolean showResumeKey = false;

    public CDXQuery() {

    }

    public CDXQuery(String url) {
        this.url = url;
    }

    public CDXQuery(HttpServletRequest request) {
        try {
            fill(request);
        } catch (ServletRequestBindingException e) {
            // Ignore
        }
    }

    protected <E extends Enum<E>> E getEnumValue(HttpServletRequest request,
            String name, Class<E> eclass, E defaultValue) {
        String enumStr = ServletRequestUtils.getStringParameter(request, name,
                null);

        E enumVal = defaultValue;

        if (enumStr != null) {
            try {
                enumVal = Enum.valueOf(eclass, enumStr);
            } catch (IllegalArgumentException ill) {

            }
        }

        return enumVal;
    }

    public void fill(HttpServletRequest request)
            throws ServletRequestBindingException {
        if (this.url == null) {
            url = ServletRequestUtils
                    .getRequiredStringParameter(request, "url");
        }

        matchType = getEnumValue(request, "matchType",
                UrlSurtRangeComputer.MatchType.class, null);

        from = ServletRequestUtils.getStringParameter(request, "from", from);
        to = ServletRequestUtils.getStringParameter(request, "to", "");
        closest = ServletRequestUtils
                .getStringParameter(request, "closest", "");

        sort = getEnumValue(request, "sort", SortType.class, SortType.regular);
        collapseTime = ServletRequestUtils.getIntParameter(request,
                "collapseTime", 0);

        gzip = ServletRequestUtils.getBooleanParameter(request, "gzip");

        output = ServletRequestUtils.getStringParameter(request, "output",
                output);

        filter = ServletRequestUtils.getStringParameters(request, "filter");
        collapse = ServletRequestUtils.getStringParameters(request, "collapse");

        showDupeCount = ServletRequestUtils.getBooleanParameter(request,
                "showDupeCount", false);
        resolveRevisits = ServletRequestUtils.getBooleanParameter(request,
                "resolveRevisits", false);
        showGroupCount = ServletRequestUtils.getBooleanParameter(request,
                "showGroupCount", false);
        lastSkipTimestamp = ServletRequestUtils.getBooleanParameter(request,
                "lastSkipTimestamp", false);
        showUniqCount = ServletRequestUtils.getBooleanParameter(request,
                "showUniqCount", false);

        offset = ServletRequestUtils.getIntParameter(request, "offset", 0);
        limit = ServletRequestUtils.getIntParameter(request, "limit", 0);
        last = ServletRequestUtils.getBooleanParameter(request, "last", false);
        fastLatest = ServletRequestUtils.getBooleanParameter(request,
                "fastLatest");
        fl = ServletRequestUtils.getStringParameter(request, "fl", "");

        page = ServletRequestUtils.getIntParameter(request, "page", -1);
        pageSize = ServletRequestUtils.getIntParameter(request, "pageSize", 0);

        showNumPages = ServletRequestUtils.getBooleanParameter(request,
                "showNumPages", false);
        showPagedIndex = ServletRequestUtils.getBooleanParameter(request,
                "showPagedIndex", false);

        resumeKey = ServletRequestUtils.getStringParameter(request,
                "resumeKey", "");
        showResumeKey = ServletRequestUtils.getBooleanParameter(request,
                "showResumeKey", false);
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

    public SortType getSort() {
        return sort;
    }

    public void setSort(SortType sort) {
        this.sort = sort;
    }

    public boolean isReverse() {
        return this.sort == SortType.reverse;
    }

    public boolean isSortClosest() {
        return this.sort == SortType.closest;
    }

    public Boolean isGzip() {
        return gzip;
    }

    public void setGzip(Boolean gzip) {
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

    public boolean isShowGroupCount() {
        return showGroupCount;
    }

    public void setShowGroupCount(boolean showGroupCount) {
        this.showGroupCount = showGroupCount;
    }

    public boolean isLastSkipTimestamp() {
        return lastSkipTimestamp;
    }

    public void setLastSkipTimestamp(boolean lastSkipTimestamp) {
        this.lastSkipTimestamp = lastSkipTimestamp;
    }

    public boolean isShowUniqCount() {
        return showUniqCount;
    }

    public void setShowUniqCount(boolean showUniqCount) {
        this.showUniqCount = showUniqCount;
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

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public Boolean getFastLatest() {
        return fastLatest;
    }

    public void setFastLatest(Boolean fastLatest) {
        this.fastLatest = fastLatest;
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
