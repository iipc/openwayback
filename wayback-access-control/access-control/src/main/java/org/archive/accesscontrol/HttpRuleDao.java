package org.archive.accesscontrol;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleSet;

import com.thoughtworks.xstream.XStream;

/**
 * The HTTP Rule Data Access Object enables a rule database to be queried via
 * the REST interface.
 * 
 * For details of the protocol, see:
 * http://webteam.archive.org/confluence/display/wayback/Exclusions+API
 * 
 * @author aosborne
 * 
 */
public class HttpRuleDao implements RuleDao {
    protected HttpClient http = new HttpClient(
            new MultiThreadedHttpConnectionManager());
    protected XStream xstream = new XStream();
    private String oracleUrl;

    public HttpRuleDao(String oracleUrl) {
        this.oracleUrl = oracleUrl;
        xstream.alias("rule", Rule.class);
        xstream.alias("ruleSet", RuleSet.class);
    }

    /**
     * @throws RuleOracleUnavailableException 
     * @see RuleDao#getRuleTree(String)
     */
    public RuleSet getRuleTree(String surt) throws RuleOracleUnavailableException {
        HttpMethod method = new GetMethod(oracleUrl + "/rules/tree/" + surt);
        RuleSet rules;

        try {
            http.executeMethod(method);
//            String response = method.getResponseBodyAsString();
//            System.out.println(response);
            rules = (RuleSet) xstream.fromXML(method.getResponseBodyAsStream());
        } catch (IOException e) {
            throw new RuleOracleUnavailableException(e);
        }
        method.releaseConnection();
        return rules;
    }

    /**
     * @return the oracleUrl
     */
    public String getOracleUrl() {
        return oracleUrl;
    }

    /**
     * @param oracleUrl
     *            the oracleUrl to set
     */
    public void setOracleUrl(String oracleUrl) {
        this.oracleUrl = oracleUrl;
    }

    public void prepare(Collection<String> surts) {
        // no-op
    }

}
