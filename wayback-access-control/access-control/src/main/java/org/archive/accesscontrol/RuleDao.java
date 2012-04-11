package org.archive.accesscontrol;

import java.util.Collection;

import org.apache.commons.httpclient.URIException;
import org.archive.accesscontrol.model.RuleSet;

/**
 * A RuleDao provides methods for retrieving rule information from a local
 * database or remote oracle.
 * 
 * @author aosborne
 * 
 */
public interface RuleDao {

    /**
     * Returns the "rule tree" for a given SURT. This is a sorted set of all
     * rules equal or lower in specificity than the given SURT plus all rules on
     * the path from this SURT to the root SURT "(".
     * 
     * The intention is to call this function with a domain or public suffix,
     * then queries within that domain can be made very fast by searching the
     * resulting list.
     * 
     * @param surt
     * @return
     * @throws RuleOracleUnavailableException 
     * @throws URIException
     */
    public RuleSet getRuleTree(String surt) throws RuleOracleUnavailableException;

    /**
     * This method allows a RuleDao to prepare for lookups from a given set of
     * surts. This can warm up a cache and/or enable a bulk lookup to be done in
     * parallel.  Many implementations may make it a no-op.
     * 
     * @param surts
     */
    public void prepare(Collection<String> surts);
}
