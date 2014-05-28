package org.archive.wayback.archivalurl;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.htmlparser.nodes.TagNode;

/**
 * The {@code AttributeRewriter} provides service for rewriting attribute values
 * in HTML document.
 * {@link FastArchivalUrlReplayParseEventHandler} delegates rewrite of URL-bearing
 * attributes to an object implementing this interface.
 * @see FastArchivalUrlReplayParseEventHandler
 */
public interface AttributeRewriter {
	/**
	 * Rewrite attributes of HTML tag {@code tag}.
	 * This method checks all attributes of {@code tag} it concerned
	 * with, and modify their value in-place. {@code context} provides
	 * an interface for rewriting URL for replay, etc.
	 * @param context provides access to various context information
	 * @param tag tag whose attributes are to be rewritten.
	 */
	public void rewrite(ReplayParseContext context, TagNode tag);
}