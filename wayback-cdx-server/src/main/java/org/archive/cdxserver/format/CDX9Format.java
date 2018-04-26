/**
 * 
 */
package org.archive.cdxserver.format;

import org.archive.format.cdx.StandardCDXLineFactory;

/**
 * A StandardCDXFormat sub-class representing standard CDX9 format.
 */
public class CDX9Format extends StandardCDXFormat {
	public CDX9Format() {
		super(new StandardCDXLineFactory("cdx9"), urlkey, timestamp, original,
			mimetype, statuscode, digest, redirect, offset, filename);
	}

	@Override
	public String toString() {
		return "cdx9";
	}
}
