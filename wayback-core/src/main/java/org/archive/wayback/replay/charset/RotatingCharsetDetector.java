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
