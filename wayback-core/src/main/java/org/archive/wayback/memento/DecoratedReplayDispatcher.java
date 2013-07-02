package org.archive.wayback.memento;

import java.util.HashMap;
import java.util.Map;

import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.replay.ReplayRendererDecoratorFactory;

public class DecoratedReplayDispatcher implements ReplayDispatcher {
	protected Map<ReplayRenderer, ReplayRenderer> decoratedMap;
	protected ReplayDispatcher decorated;
	protected ReplayRendererDecoratorFactory decoratorFactory;
	
	public DecoratedReplayDispatcher() {
		decoratedMap = new HashMap<ReplayRenderer, ReplayRenderer>();
	}
	public DecoratedReplayDispatcher(ReplayDispatcher decorated, 
			ReplayRendererDecoratorFactory decoratorFactory) {
		this.decorated = decorated;
		this.decoratorFactory = decoratorFactory;
		this.decoratedMap = new HashMap<ReplayRenderer, ReplayRenderer>();
	}
	protected synchronized ReplayRenderer getDecoratedRenderer(ReplayRenderer renderer) {
		if(decoratedMap.containsKey(renderer)) {
			return decoratedMap.get(renderer);
		}
		ReplayRenderer decorated = decoratorFactory.decorate(renderer);
		decoratedMap.put(renderer, decorated);
		return decorated;
	}
	
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource) {
		ReplayRenderer r = decorated.getRenderer(wbRequest, result, resource, resource);
		return getDecoratedRenderer(r);
	}
	
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource) {
		ReplayRenderer r = decorated.getRenderer(wbRequest, result, httpHeadersResource, payloadResource);
		return getDecoratedRenderer(r);
	}

	public CaptureSearchResult getClosest(WaybackRequest wbRequest,
			CaptureSearchResults results) throws BetterRequestException {
		CaptureSearchResult closest;
		try {
			closest = decorated.getClosest(wbRequest, results);
		} catch (BetterRequestException e) {
			e.addHeader(MementoConstants.LINK, 
					MementoUtils.generateMementoLinkHeaders(results,wbRequest,true));
			throw e;
		}
		return closest;
	}
	/**
	 * @return the decorated
	 */
	public ReplayDispatcher getDecorated() {
		return decorated;
	}
	/**
	 * @param decorated the decorated to set
	 */
	public void setDecorated(ReplayDispatcher decorated) {
		this.decorated = decorated;
	}
	/**
	 * @return the decoratorFactory
	 */
	public ReplayRendererDecoratorFactory getDecoratorFactory() {
		return decoratorFactory;
	}
	/**
	 * @param decoratorFactory the decoratorFactory to set
	 */
	public void setDecoratorFactory(ReplayRendererDecoratorFactory decoratorFactory) {
		this.decoratorFactory = decoratorFactory;
	}

}
