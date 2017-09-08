package org.archive.wayback.archivalurl;

import java.net.URISyntaxException;

import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.replay.ReplayContext;
import org.archive.wayback.replay.ReplayRewriteContext;
import org.archive.wayback.replay.ReplayURLTransformer;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPointAware;

public class ArchivalUrlReplayURLTransformer implements ReplayURLTransformer {

	private static final String MAILTO_PREFIX = "mailto:";
	public static final String ANCHOR_PREFIX = "#";

	public static boolean isProtocolRelative(String url) {
		if (url.startsWith("//")) return true;
		// for URLs in query parameters (do we really want this?)
		if (url.startsWith("%2F%2F")) return true;
		if (url.startsWith("%2f%2f")) return true;
		// <-- TODO: delete (see JS escape support in JSStringTransformer)
		if (url.startsWith("\\/\\/")) return true;
		if (url.startsWith("\\\\/\\\\/")) return true;
		if (url.startsWith("\\\\u00252F\\\\u00252F")) return true;
		// -->
		return false;
	}

	public static boolean isAbsoluteURL(String url) {
		if (url.startsWith("http")) {
			String s = url.substring(4);
			if (s.startsWith("s"))
				s = s.substring(1);
			if (s.startsWith(":"))
				return isProtocolRelative(s.substring(1));
			if (s.startsWith("%3A") || s.startsWith("%3a"))
				return isProtocolRelative(s.substring(3));	
			// <-- TODO: delete (see JS escape support in JSStringTransformer)
			if (s.startsWith("\\\\u00253A"))
				return isProtocolRelative(s.substring(9));
			// -->
		}
		return false;
	}

	public ArchivalUrlReplayURLTransformer() {
		super();
	}

	@Override
	public String transform(ReplayContext replayContext, String url, String flags) {
		// if we get an empty string, just return it:
		if (url.length() == 0) {
			return url;
		}
	
		if (url.startsWith(UrlOperations.JAVASCRIPT_PREFIX) ||
				url.startsWith(MAILTO_PREFIX) ||
				url.startsWith(UrlOperations.DATA_PREFIX) ||
				url.startsWith(ANCHOR_PREFIX)) {
			return url;
		}
	
		// don't rewrite full and path-relative URLs. For
		// https://webarchive.jira.com/browse/ARI-3985.
		// Keep the style of URL (ARI-4033).
		String trimmedUrl = url.trim();
		URLStyle urlStyle;
		if (isAbsoluteURL(trimmedUrl))
			urlStyle = URLStyle.ABSOLUTE;
		else if (isProtocolRelative(trimmedUrl))
			urlStyle = URLStyle.PROTOCOL_RELATIVE;
		else if (trimmedUrl.startsWith("/"))
			urlStyle = URLStyle.SERVER_RELATIVE;
		else if (trimmedUrl.startsWith("../"))
			// if path-relative looking backward,
			// rewrite as server relative so we
			// don't inadvertently erase the
			// hostname from an archival url
			urlStyle = URLStyle.SERVER_RELATIVE;
		else
			return url;
	
		// first make url into absolute, taking BASE into account.
		// (this also removes escaping: ex. "https:\/\/" -> "https://")
		// (now url shall be escaping-free; See JSStringTransformer)
		String absurl = null;
		try {
			absurl = replayContext.resolve(url);
		} catch (URISyntaxException ex) {
			return url;
		}
		// if flags is a special value identifying HTTP header field
		// context, replace flags with request context flags. This way,
		// rewritten Location header field will inherit context flags
		// from the request.
		if (ReplayRewriteContext.HEADER_CONTEXT.equals(flags)) {
			flags = replayContext.getContextFlags();
		}
		// IMPORTANT: call makeReplayURI through replayContext.
		// This is to allow for decorating ReplayURIConverter with
		// custom per-request behavior. See AccessPoint#decorateURIConverter.
		return replayContext.makeReplayURI(absurl, flags, urlStyle);
	}

}