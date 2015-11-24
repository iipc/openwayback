/**
 *
 */
package org.archive.wayback.util.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A generic abstract pattern-based string processor.
 *
 * The implementing classes are expected to be Spring beans.
 *
 * @see UriMatchRule
 *
 * @author ngiraud
 *
 */
public abstract class PatternBasedTextProcessor {

    /**
     * The pattern to find in the input.
     */
    private String pattern;

    /**
     * The underlying pattern implementation.
     */
    private Pattern patternImpl;

    /**
     * @return the pattern to match against the input.
     */
    public final String getPattern() {
        return pattern;
    }

    /**
     * @param pattern the pattern to match against the input.
     */
    public final void setPattern(final String pattern) {
        this.pattern = pattern;
        this.patternImpl = Pattern.compile(pattern);
    }

    /**
     * Processes a given text.
     * @param text the text to process.
     * @return the text after processing
     */
    public abstract String process(final String text);

    /**
     * Builds a matcher for the given text.
     * @param text the text to match against the pattern.
     * @return the matcher
     */
    protected final Matcher getMatcher(final String text) {
        return patternImpl.matcher(text);
    }

}
