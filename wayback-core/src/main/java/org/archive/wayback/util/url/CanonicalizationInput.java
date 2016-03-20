/**
 *
 */
package org.archive.wayback.util.url;

/**
 * @author ngiraud
 *
 */
public class CanonicalizationInput extends UriMatchRuleInput {

    CanonicalizationInput(String uri) {
        super(uri);
    }

    @Override
    public String getTextToProcess() {
        return getUri();
    }

}
