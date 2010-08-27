/**
 * 
 */
package org.archive.wayback.util.webapp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RequestHandler implementation which allows serving of static files, and
 * .jsp files within a ServletContext.
 * 
 * @author brad
 */
public class StaticFileRequestHandler extends AbstractRequestHandler {

	private static final Logger LOGGER = Logger.getLogger(
			StaticFileRequestHandler.class.getName());

	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException, IOException {
		boolean handled = false;
		String contextRelativePath = httpRequest.getServletPath();
		String absPath = getServletContext().getRealPath(contextRelativePath);
		File test = new File(absPath);
		// TODO: check for index.jsp(or configurable equivalent),
		//       if it's a directory?
		if(test.isFile()) {
			LOGGER.fine("static path:" + absPath);
			RequestDispatcher dispatcher = 
				httpRequest.getRequestDispatcher(contextRelativePath);
//			try {
				dispatcher.forward(httpRequest, httpResponse);
				handled = true;
//			} catch(Exception e) {
//			}
		} else {
			LOGGER.fine("Not-static path:" + absPath);
		}
		return handled;
	}
}
