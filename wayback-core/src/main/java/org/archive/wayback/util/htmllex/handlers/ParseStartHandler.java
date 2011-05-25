package org.archive.wayback.util.htmllex.handlers;

import java.io.IOException;

import org.archive.wayback.util.htmllex.ParseContext;

public interface ParseStartHandler {
	public void handleParseStart(ParseContext context)
	throws IOException;
}
