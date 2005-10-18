package org.archive.wayback;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.core.WaybackLogic;

public interface ReplayUI {

	public void init(final Properties p) throws IOException;

	public String makeReplayURI(final HttpServletRequest request,
			final ResourceResult result);

	public void handle(final WaybackLogic wayback, final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	public void replayResource(final WMRequest wmRequest,
			final ResourceResult result, final Resource resource,
			final HttpServletRequest request,
			final HttpServletResponse response, final ResourceResults results)
			throws IOException, ServletException;

	public void showNotInArchive(final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	public void showResourceNotAvailable(final WMRequest wmRequest,
			final HttpServletRequest request,
			final HttpServletResponse response, final String message)
			throws IOException, ServletException;

	public void showIndexNotAvailable(final WMRequest wmRequest,
			final HttpServletRequest request,
			final HttpServletResponse response, final String message)
			throws IOException, ServletException;
}
