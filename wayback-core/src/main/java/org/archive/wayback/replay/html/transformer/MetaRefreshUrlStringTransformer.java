/* MetaRefreshUrlStringTransformer
 *
 * $Id$:
 *
 * Created on Jan 12, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.replay.html.transformer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

/**
 * @author brad
 *
 */
public class MetaRefreshUrlStringTransformer extends URLStringTransformer 
implements StringTransformer {

	private final static Pattern refreshURLPattern = 
		Pattern.compile("^\\d+\\s*;\\s*url\\s*=\\s*(.+?)\\s*$",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.html.StringTransformer#transform(org.archive.wayback.replay.html.ReplayParseContext, java.lang.String)
	 */
	public String transform(ReplayParseContext context, String input) {
		/*
		 <META 
          HTTP-EQUIV="Refresh"
          CONTENT="0; URL=/ics/default.asp">
          
          Our argument "input" is set to the value of the "CONTENT" attribute.
          
          So, we need to search for the "URL=", take everything to the right
          of that, trim it, contextualize it, and return that.
		 */
		Matcher m = refreshURLPattern.matcher(input);
		if(m.matches()) {
			if(m.groupCount() == 1) {
				StringBuilder sb = new StringBuilder(input.length() * 2);
	
				sb.append(input.substring(0,m.start(1)));

				sb.append(super.transform(context, m.group(1)));
				
				// This was temporarily used for testing the regex:
//				sb.append("(((").append(m.group(1)).append(")))");
				
				sb.append(input.substring(m.end(1)));
				return sb.toString();
			}
		}
		return input;
	}

}
