package org.archive.wayback.memento;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.archive.wayback.util.StringFormatter;

public interface MementoConstants {	

	public final static String AGGREGATION_PREFIX_CONFIG = "aggregationPrefix";
	public final static String TIMEGATE_PREFIX_CONFIG = "aggregationPrefix";
	
	public final static String GMT_TIMEZONE_STRING = "GMT";
	public final static TimeZone GMT_TZ = 
		TimeZone.getTimeZone(GMT_TIMEZONE_STRING);
	
	public final static String DATE_FORMAT_14 = "yyyyMMddHHmmss";
	public final static String HTTP_LINK_DATE_FORMAT = 
		"E, dd MMM yyyy HH:mm:ss z";	
	
	
	public final static String APPLICATION_LINK_FORMAT = 
		"application/link-format";
	public final static String APPLICATION_RDF_XML_FORMAT = 
		"application/rdf+xml";
	public final static String RDF_XML_SERIALIZER = 
		"RDF/XML";
	
	
	
	public final static String TIMEBUNDLE = "timebundle";
	public final static int TIMEBUNDLE_RESPONSE_CODE = 303;
	public final static String TIMEGATE = "timegate";
	public final static String TIMEMAP = "timemap";
	public final static String LINK_PATH = "LINK";
	public final static String LINK = "Link";
	public final static String VARY = "Vary";
	public final static String NEGOTIATE_DATETIME = "negotiate,accept-datetime";
	
	
	public final static String ACCPEPT_DATETIME = "Accept-Datetime";
	public final static String MEMENTO_DATETIME = "Memento-Datetime";
	public final static String MEMENTO = "memento";
	public final static String DATETIME = "datetime";
	public final static String FIRST = "first";
	public final static String PREV = "prev";
	public final static String NEXT = "next";
	public final static String LAST = "last";

	public final static String FIRST_MEMENTO = 
		StringFormatter.join(" ", FIRST, MEMENTO);

	public final static String PREV_MEMENTO = 
		StringFormatter.join(" ", PREV, MEMENTO);
	
	public final static String PREV_FIRST_MEMENTO =
		StringFormatter.join(" ", PREV, FIRST, MEMENTO);
	
	public final static String LAST_MEMENTO = 
		StringFormatter.join(" ", LAST, MEMENTO);

	public final static String NEXT_MEMENTO = 
		StringFormatter.join(" ", NEXT, MEMENTO);
	
	public final static String NEXT_LAST_MEMENTO =
		StringFormatter.join(" ", NEXT, LAST, MEMENTO);
	
	public final static String FIRST_LAST_MEMENTO =
		StringFormatter.join(" ", FIRST, LAST, MEMENTO);
	
	public final static String ORIGINAL = "original";
	public final static String REL = "rel";
	
	public final static String WBR_FORMAT_KEY = "TIMEMAP_FORMAT";
	public final static String WBR_TIMEGATE_KEY = "TIMEGATE_REQUEST";
	public final static String WBR_TIMEGATE_VALUE = "yes";

	public final static String FORMAT_RDF = "rdf";
	public final static String FORMAT_LINK = "link";

	public final static String TIMEGATE_JSP_HANDLER = "timegateJsp";
}
