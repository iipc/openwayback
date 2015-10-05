/**
 *
 */
package org.archive.wayback.util.url;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base abstract class that defines a regexp based URI processor.
 * A rule will execute an operation provided that the candidate URI partially matches the
 * configured pattern.
 *
 * TODO instead of simply returning the processed URI, we should add a wrapper object that
 * also contains an enum value that would allow to control the sequence of rules,
 * something like Hetrix scoping rules.
 *
 * @author ngiraud
 *
 * @param <I> the class of input.
 */
public class UriMatchRule<I extends UriMatchRuleInput> {

    /**
     * The class logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(UriMatchRule.class.getName());

    /**
     * The match pattern.
     */
    private String pattern;

    /**
     * The list of processors.
     */
    private List<PatternBasedTextProcessor> processors;

    /**
     * The underlying pattern implementation.
     */
    private Pattern patternImpl;

    /**
     * @return the match pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @return the list of processors.
     */
    public List<PatternBasedTextProcessor> getProcessors() {
        return processors;
    }

    /**
     * @param processors the list of processors.
     */
    public void setProcessors(List<PatternBasedTextProcessor> processors) {
        this.processors = processors;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(final String pattern) {
        this.pattern = pattern;
        this.patternImpl = Pattern.compile(pattern);
    }

    /**
     * Processes the given URI if it matches the configured pattern.
     * @param uriAsString the candidate URI as string
     * @param context the execution context
     * @return the URI after processing
     */
    public String processIfMatches(final I input) {
        String uriAsString = input.getUri();
        String text = input.getTextToProcess();
        Matcher m = patternImpl.matcher(uriAsString);
        if (!m.find()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Rule " + getClass().getSimpleName()
                        + ": skipped " + uriAsString);
            }
            return text;
        }

        String result = new String(text);
        for (PatternBasedTextProcessor proc : processors) {
            result = proc.process(result);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Rule " + getClass().getSimpleName()
                    + ": processed " + uriAsString);
        }

        return result;
    }

}
