package org.archive.wayback.memento;

import java.util.TimeZone;
import org.archive.wayback.util.StringFormatter;

public abstract interface MementoConstants
{
  public static final String AGGREGATION_PREFIX_CONFIG = "aggregationPrefix";
  public static final String TIMEGATE_PREFIX_CONFIG = "aggregationPrefix";
  public static final String GMT_TIMEZONE_STRING = "GMT";
  public static final TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");
  public static final String DATE_FORMAT_14 = "yyyyMMddHHmmss";
  public static final String HTTP_LINK_DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss z";
  public static final String APPLICATION_LINK_FORMAT = "application/link-format";
  public static final String APPLICATION_RDF_XML_FORMAT = "application/rdf+xml";
  public static final String RDF_XML_SERIALIZER = "RDF/XML";
  public static final String TIMEBUNDLE = "timebundle";
  public static final int TIMEBUNDLE_RESPONSE_CODE = 303;
  public static final String TIMEGATE = "timegate";
  public static final String TIMEMAP = "timemap";
  public static final String LINK_PATH = "LINK";
  public static final String LINK = "Link";
  public static final String VARY = "Vary";
  public static final String NEGOTIATE_DATETIME = "negotiate,accept-datetime";
  public static final String ACCPEPT_DATETIME = "Accept-Datetime";
  public static final String MEMENTO_DATETIME = "Memento-Datetime";
  public static final String MEMENTO = "memento";
  public static final String DATETIME = "datetime";
  public static final String FIRST = "first";
  public static final String PREV = "prev";
  public static final String NEXT = "next";
  public static final String LAST = "last";
  public static final String FIRST_MEMENTO = StringFormatter.join(" ", new String[] { "first", "memento" });

  public static final String PREV_MEMENTO = StringFormatter.join(" ", new String[] { "prev", "memento" });

  public static final String PREV_FIRST_MEMENTO = StringFormatter.join(" ", new String[] { "prev", "first", "memento" });

  public static final String LAST_MEMENTO = StringFormatter.join(" ", new String[] { "last", "memento" });

  public static final String NEXT_MEMENTO = StringFormatter.join(" ", new String[] { "next", "memento" });

  public static final String NEXT_LAST_MEMENTO = StringFormatter.join(" ", new String[] { "next", "last", "memento" });

  public static final String FIRST_LAST_MEMENTO = StringFormatter.join(" ", new String[] { "first", "last", "memento" });
  public static final String ORIGINAL = "original";
  public static final String REL = "rel";
  public static final String WBR_FORMAT_KEY = "TIMEMAP_FORMAT";
  public static final String WBR_TIMEGATE_KEY = "TIMEGATE_REQUEST";
  public static final String WBR_TIMEGATE_VALUE = "yes";
  public static final String FORMAT_RDF = "rdf";
  public static final String FORMAT_LINK = "link";
  public static final String TIMEGATE_JSP_HANDLER = "timegateJsp";
}