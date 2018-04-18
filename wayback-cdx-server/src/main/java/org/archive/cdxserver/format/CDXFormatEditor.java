package org.archive.cdxserver.format;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

/**
 * property editor for CDXFormat property.
 * <p>
 * Meant to provide backward compatibility at Spring level for
 * CDXServer {@code cdxFormat} property.
 * Converts strings {@code cdx9} and {@code cdx11} to
 * corresponding CDXFormat implementation.
 * </P
 */
public class CDXFormatEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (!StringUtils.hasText(text)) {
			setValue(null);
			return;
		}
		if (text.equals("cdx9")) {
			setValue(new CDX9Format());
			return;
		}
		if (text.equals("cdx11")) {
			setValue(new CDX11Format());
			return;
		}
		throw new IllegalArgumentException("invalid textual name for CDXFormat");
	}

}
