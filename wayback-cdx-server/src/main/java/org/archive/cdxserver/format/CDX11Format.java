/**
 * 
 */
package org.archive.cdxserver.format;

import org.archive.format.cdx.StandardCDXLineFactory;

/**
 * A StandardCDXFormat sub-class representing standard CDX11 format.
 *
 */
public class CDX11Format extends StandardCDXFormat {
	public CDX11Format() {
		super(new StandardCDXLineFactory("cdx11"), urlkey, timestamp, original,
			mimetype, statuscode, digest, redirect, robotflags, length, offset,
			filename);
	}

	@Override
	public String toString() {
		return "cdx11";
	}
}
