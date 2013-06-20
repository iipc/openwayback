package org.archive.wayback.memento;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.exception.BadQueryException;

public class TimeGateBadQueryException extends BadQueryException {
	
	private static final long serialVersionUID = 1L;

	public TimeGateBadQueryException(String message, String requestUrl) {
		super(message, requestUrl);
	}

	@Override
	public void setupResponse(HttpServletResponse response) {
		super.setupResponse(response);
		MementoUtils.addVaryHeader(response);
		MementoUtils.addOrigHeader(response, super.getDetails());
	}
}
