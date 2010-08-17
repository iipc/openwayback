/* URLStringTransformer
 *
 * $Id$
 *
 * Created on 12:36:59 PM Nov 5, 2009.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.replay.html.transformer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

/**
 * Attempts to rewrite any absolute URLs found within the text/javascript MIME
 * 
 * @author brad
 *
 */
public class JSStringTransformer implements StringTransformer {
	private final static Pattern httpPattern = Pattern
	.compile("(http:\\\\?/\\\\?/[A-Za-z0-9:_@.-]+)");

	public String transform(ReplayParseContext context, String input) {

		StringBuffer replaced = new StringBuffer(input.length());
		Matcher m = httpPattern.matcher(input);
		while (m.find()) {
			String host = m.group(1);
			m.appendReplacement(replaced, context.contextualizeUrl(host));
		}
		m.appendTail(replaced);
		return replaced.toString();
	}
}
