/**
 *
 */
package org.archive.wayback.util.url;

import java.util.regex.Matcher;

/**
 *
 * @author ngiraud
 *
 */
public class UriStripper extends PatternBasedTextProcessor {

    public UriStripper() {
        super();
    }

    @Override
    public String process(final String uriAsString) {
        Matcher m = getMatcher(uriAsString);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(result, "");;
        }
        m.appendTail(result);
        return result.toString();
    }

}
