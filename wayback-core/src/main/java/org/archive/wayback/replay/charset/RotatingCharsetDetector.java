/* RotatingCharsetDetector
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.replay.charset;

import java.io.IOException;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;

/**
 * @author brad
 *
 * Provides a way to rotate through several detection schemes 
 */
public class RotatingCharsetDetector extends CharsetDetector {
	public final static int MODES[][] = {
		{0,1,2},
		{0,2,1},
		{1,0,2},
		{1,2,0},
		{2,1,0},
		{2,0,1}
	};
	public final static int MODE_COUNT = 6;
	public final static int GUESS_TYPES = 3;

	public int nextMode(int curMode) {
		if(curMode >= MODE_COUNT - 1) {
			return 0;
		}
		return curMode + 1;
	}
	public String getCharsetType(Resource resource, int type) throws IOException {
		if(type == 0) {
			return getCharsetFromHeaders(resource);
		} else if(type == 1) {
			return getCharsetFromMeta(resource);
		} else if(type == 2) {
			return getCharsetFromBytes(resource);
		}
		return null;
	}
	public String getCharset(Resource resource, int mode) throws IOException {
		String charset = null;
		if(mode >= MODE_COUNT) {
			mode = 0;
		}
		for(int type = 0; type < GUESS_TYPES; type++) {
			charset = getCharsetType(resource,MODES[mode][type]);
			if(charset != null) {
				break;
			}
		}
		if(charset == null) {
			charset = DEFAULT_CHARSET;
		}
		return charset;
	}
	@Override
	public String getCharset(Resource resource, WaybackRequest request) 
	throws IOException {
		int mode = request.getCharsetMode();
		return getCharset(resource,mode);
	}
}
