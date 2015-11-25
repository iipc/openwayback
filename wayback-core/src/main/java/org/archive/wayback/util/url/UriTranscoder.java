/**
 *
 */
package org.archive.wayback.util.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 *
 * Transcode the first group in the matched pattern.
 *
 * TODO make group(s) configurable.
 *
 * @author ngiraud
 *
 */
public class UriTranscoder extends PatternBasedTextProcessor {

    private static final Logger LOGGER = Logger.getLogger(UriTranscoder.class.getName());

    private String sourceEncoding;

    private String targetEncoding;

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public String getTargetEncoding() {
        return targetEncoding;
    }

    public void setTargetEncoding(String targetEncoding) {
        this.targetEncoding = targetEncoding;
    }

    @Override
    public String process(final String uriAsString) {
        Matcher m = getMatcher(uriAsString);
        StringBuilder result = new StringBuilder(uriAsString);
        while (m.find()) {
            int start = m.start(1);
            int end = m.end(1);
            try {
                String decodedGroup = URLDecoder.decode(
                        uriAsString.substring(start, end), getSourceEncoding());
                result.replace(
                        start,
                        end,
                        URLEncoder.encode(decodedGroup, getTargetEncoding()));
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
        return result.toString();
    }

}
