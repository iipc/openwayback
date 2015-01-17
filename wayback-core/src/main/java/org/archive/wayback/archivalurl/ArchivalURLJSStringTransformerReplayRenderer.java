package org.archive.wayback.archivalurl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.replay.TextDocument;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.IdentityResultURIConverterFactory;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.webapp.AccessPoint;

/**
 * {@link TextReplayRenderer} that uses {@link StringTransformer} as an underlining
 * rewrite engine.
 * <p><code>transformer</code> can be any <code>StringTransformer</code> such as
 * <code>JSStringTransformer</code> or <code>MultiRegexReplaceStringTransformer</code>.</p>
 * <p>This class itself is not specific to either JavaScript rewriting or archival-URL mode.
 * It is all up to <code>transformer</code> how URLs in resource is rewritten.
 * As such, this class is also used for rewriting {@code https://} URLs in proxy-mode
 * in spite of its name.
 * </p>
 * <p>TODO:
 * @see org.archive.wayback.replay.html.transformer.JSStringTransformer
 * @see org.archive.wayback.replay.html.transformer.MultiRegexReplaceStringTransformer
 * @author ilya
 *
 */
public class ArchivalURLJSStringTransformerReplayRenderer extends TextReplayRenderer {
	
	public ArchivalURLJSStringTransformerReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
		super(httpHeaderProcessor);
	}


	private StringTransformer transformer;
	private ContextResultURIConverterFactory converterFactory = null;
	private boolean rewriteHttpsOnly;

	
	public StringTransformer getTransformer() {
		return transformer;
	}


	public void setTransformer(StringTransformer transformer) {
		this.transformer = transformer;
	}
	

	public boolean isRewriteHttpsOnly() {
		return rewriteHttpsOnly;
	}


	public void setRewriteHttpsOnly(boolean rewriteHttpsOnly) {
		this.rewriteHttpsOnly = rewriteHttpsOnly;
	}


	@Override
	protected void updatePage(TextDocument page,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, CaptureSearchResult result,
			Resource resource, ResultURIConverter uriConverter,
			CaptureSearchResults results) throws ServletException, IOException {
		
		// same code in ArchivalUrlSAXRewriteReplayRenderer
		ContextResultURIConverterFactory fact = null;
		
		// this uriConverter implementing factory should address all cases currently
		// known.
		if (converterFactory != null) {
			fact = converterFactory;
		} else if (uriConverter instanceof ContextResultURIConverterFactory) {
			fact = (ContextResultURIConverterFactory)uriConverter;
		} else {
			fact = new IdentityResultURIConverterFactory(uriConverter);			
		}		
		
		// set up the context:
		ReplayParseContext context = new ReplayParseContext(fact, result);
		
		context.setRewriteHttpsOnly(rewriteHttpsOnly);
		
		UIResults uiResults = new UIResults(wbRequest, uriConverter, results, result, resource);
		JSPExecutor jspExec = new JSPExecutor(httpRequest, httpResponse, uiResults);
		

		// To make sure we get the length, we have to buffer it all up...
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		context.setOutputCharset("utf-8");
		context.setOutputStream(baos);
		context.setJspExec(jspExec);
		context.setInJS(true); //for https://webarchive.jira.com/browse/ARI-3762
		
		// XXX same code in ArchivalUrlSAXReplayRenderer, and probably other
		// custom Archival-URL ReplayRenderers needs this, too.
		// We should move this code somewhere reusable (ReplayParseContext? -
		// which would push us to define new interface for rewriting).
		String policy = result.getOraclePolicy();
		if (policy == null) {
			AccessPoint accessPoint = wbRequest.getAccessPoint();
			if (accessPoint != null) {
				policy = accessPoint.getRewriteDirective(result);
			}
		}
		if (policy != null) {
			context.setOraclePolicy(policy);
		}
		
		//RewriteReplayParseEventHandler.addRewriteParseContext(context);
		
		
		String replaced = transformer.transform(context, page.sb.toString());
		
		page.sb.setLength(0);
		page.sb.ensureCapacity(replaced.length());
		page.sb.append(replaced);
		
		// if any JS-specific jsp inserts are configured, run and insert...
		page.insertAtStartOfDocument(buildInsertText(page, httpRequest,
				httpResponse, wbRequest, results, result, resource));
	}


	public ContextResultURIConverterFactory getConverterFactory() {
		return converterFactory;
	}

	/**
	 * Set a factory to be used for constructing contextualized
	 * {@link ResultURIConverter}.
	 * <p>
	 * If set, it will be used even when
	 * base ResultURIConverter implements {@link ContextResultURIConverterFactory}
	 * interface. Usually ResultURIConverter's own factory implementation is
	 * sufficient, and slightly more efficient. This property may be dropped in the
	 * near future.
	 * </p>
	 * @param converterFactory factory object
	 */
	public void setConverterFactory(
			ContextResultURIConverterFactory converterFactory) {
		this.converterFactory = converterFactory;
	}

}
