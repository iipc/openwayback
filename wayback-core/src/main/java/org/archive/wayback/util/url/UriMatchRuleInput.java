/**
 *
 */
package org.archive.wayback.util.url;

/**
 * Abstract class used to wrap input for the execution of an {@link UriMatchRule}.
 *
 * @author ngiraud
 *
 */
abstract class UriMatchRuleInput {

    /**
     * The URI to test.
     */
    private final String uri;

    /**
     * Constructor from URI.
     * @param uri the URI to test
     */
    protected UriMatchRuleInput(String uri) {
        this.uri = uri;
    }

    /**
     * @return the URI to test
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the text to be processed by rule processors.
     */
    public abstract String getTextToProcess();

}
