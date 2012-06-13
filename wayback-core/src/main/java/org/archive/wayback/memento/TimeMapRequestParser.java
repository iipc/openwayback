package org.archive.wayback.memento;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.WrappedRequestParser;
import org.archive.wayback.webapp.AccessPoint;

public class TimeMapRequestParser extends WrappedRequestParser
  implements MementoConstants
{
  private static final Logger LOGGER = Logger.getLogger(TimeMapRequestParser.class.getName());

  public TimeMapRequestParser(BaseRequestParser wrapped)
  {
    super(wrapped);
  }

  public WaybackRequest parse(HttpServletRequest httpRequest, AccessPoint accessPoint)
    throws BadQueryException, BetterRequestException
  {
    String requestPath = accessPoint.translateRequestPathQuery(httpRequest);
    LOGGER.fine("requestpath:" + requestPath);

    if (requestPath.startsWith("timemap"))
    {
      String urlStrplus = requestPath.substring(requestPath.indexOf("/") + 1);

      String format = urlStrplus.substring(0, urlStrplus.indexOf("/"));

      String urlStr = urlStrplus.substring(urlStrplus.indexOf("/") + 1);
      LOGGER.fine(String.format("Parsed format(%s) URL(%s)", new Object[] { format, urlStr }));

      WaybackRequest wbRequest = new WaybackRequest();

      if (wbRequest.getStartTimestamp() == null) {
        wbRequest.setStartTimestamp(getEarliestTimestamp());
      }
      wbRequest.setAnchorTimestamp(getLatestTimestamp());
      if (wbRequest.getEndTimestamp() == null) {
        wbRequest.setEndTimestamp(getLatestTimestamp());
      }
      wbRequest.setCaptureQueryRequest();
      MementoUtils.setRequestFormat(wbRequest, format);
      wbRequest.setRequestUrl(urlStr);

      wbRequest.setResultsPerPage(getMaxRecords());

      return wbRequest;
    }
    return null;
  }
}