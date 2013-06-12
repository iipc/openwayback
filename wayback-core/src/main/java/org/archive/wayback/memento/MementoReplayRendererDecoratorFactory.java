package org.archive.wayback.memento;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.replay.ReplayRendererDecoratorFactory;

public class MementoReplayRendererDecoratorFactory implements ReplayRendererDecoratorFactory {

	public ReplayRenderer decorate(ReplayRenderer renderer) {
		MementoReplayRendererDecorator decorator = new MementoReplayRendererDecorator();
		decorator.setDecorated(renderer);
		return decorator;
	}
}
