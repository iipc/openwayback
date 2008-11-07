package org.archive.wayback.util.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

/**
 * Class containing common static URL methods. Primarily resolveUrl() and 
 * the (currently) unused isAuthority().
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlOperations {
	
	public final static String DNS_SCHEME = "dns:";
	public final static String HTTP_SCHEME = "http://";
	public final static String HTTPS_SCHEME = "https://";
	public final static String FTP_SCHEME = "ftp://";
	public final static String MMS_SCHEME = "mms://";
	public final static String RTSP_SCHEME = "rtsp://";
	// go brewster
	public final static String WAIS_SCHEME = "wais://";
	
	public final static String ALL_SCHEMES[] = { 
		HTTP_SCHEME,
		HTTPS_SCHEME,
		FTP_SCHEME,
		MMS_SCHEME,
		RTSP_SCHEME,
		WAIS_SCHEME
	};
	
	
	public final static char PORT_SEPARATOR = ':';
	public final static char PATH_START = '/';
	
	
	private static final String CC_TLDS = "ac|ad|ae|af|ag|ai|al|am|an|ao|aq" +
			"|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs" +
			"|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx" +
			"|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo" +
			"|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk" +
			"|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg" +
			"|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma" +
			"|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz" +
			"|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm" +
			"|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj" +
			"|sk|sl|sm|sn|so|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn" +
			"|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|um|us|uy|uz|va|vc|ve|vg|vi|vn|vu" +
			"|wf|ws|ye|yt|yu|za|zm|zw";
	
	private static final String GEN_TLDS = "aero|biz|cat|com|coop|edu|gov" +
			"|info|int|jobs|mil|mobi|museum|name|net|org|pro|travel";
	
	
	private static final String ALL_TLD_PATTERN = CC_TLDS + "|" + GEN_TLDS;

	private static final String IP_PATTERN = "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+";
	
    private static final Pattern AUTHORITY_REGEX =
        Pattern.compile("(([0-9a-z_.-]+)\\.(" + ALL_TLD_PATTERN + "))|" +
        		"(" + IP_PATTERN + ")");

    /**
	 * @param urlPart
	 * @return boolean indicating whether urlPart might be an Authority.
	 */
	public static boolean isAuthority(String urlPart) {
		Matcher m = AUTHORITY_REGEX.matcher(urlPart);
		
		return (m != null) && m.matches();
	}
	
	/**
	 * @param baseUrl
	 * @param url
	 * @return url resolved against baseUrl, unless it is absolute already
	 */
	public static String resolveUrl(String baseUrl, String url) {
		for(final String scheme : ALL_SCHEMES) {
			if(url.startsWith(scheme)) {
				try {
					return UURIFactory.getInstance(url).getEscapedURI();
				} catch (URIException e) {
					e.printStackTrace();
					// can't let a space exist... send back close to whatever came
					// in...
					return url.replace(" ", "%20");
				}
			}
		}
		UURI absBaseURI;
		UURI resolvedURI = null;
		try {
			absBaseURI = UURIFactory.getInstance(baseUrl);
			resolvedURI = UURIFactory.getInstance(absBaseURI, url);
		} catch (URIException e) {
			e.printStackTrace();
			return url.replace(" ", "%20");
		}
		return resolvedURI.getEscapedURI();
	}
	
	public static String urlToScheme(final String url) {
		for(final String scheme : ALL_SCHEMES) {
			if(url.startsWith(scheme)) {
				return scheme;
			}
		}
		return null;
	}
	
	public static int schemeToDefaultPort(final String scheme) {
		if(scheme.equals(HTTP_SCHEME)) {
			return 80;
		}
		if(scheme.equals(HTTPS_SCHEME)) {
			return 443;
		}
		if(scheme.equals(FTP_SCHEME)) {
			return 21;
		}
		if(scheme.equals(RTSP_SCHEME)) {
			return 554;
		}
		if(scheme.equals(MMS_SCHEME)) {
			return 1755;
		}
		return -1;
	}
	
	public static String urlToHost(String url) {
		if(url.startsWith("dns:")) {
			return url.substring(4);
		}
		for(String scheme : ALL_SCHEMES) {
			if(url.startsWith(scheme)) {
				int hostIdx = scheme.length();
				int portIdx = url.indexOf(PORT_SEPARATOR, hostIdx + 1);
				int pathIdx = url.indexOf(PATH_START, hostIdx + 1);
				if(portIdx == -1 && pathIdx == -1) {
					return url.substring(hostIdx);
				}
				if(portIdx == -1) {
					return url.substring(hostIdx,pathIdx);
				}
				if(pathIdx == -1) {
					return url.substring(hostIdx,portIdx);
				}
				if(pathIdx > portIdx) {
					return url.substring(hostIdx,portIdx);
				} else {
					return url.substring(hostIdx,pathIdx);
				}
			}
		}
		return url;
	}
}
