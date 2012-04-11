    package org.archive.accesscontrol.model;

import java.util.Collection;
import java.util.List;

import org.archive.accesscontrol.RuleDao;
import org.archive.surt.NewSurtTokenizer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The rule data access object provides convenience methods for using Hibernate
 * to access stored rules.  The database connection is expected to be configured 
 * using the SpringFramework ORM layer.
 * 
 * @author aosborne
 */
@SuppressWarnings("unchecked")
public class HibernateRuleDao extends HibernateDaoSupport implements RuleDao {
    public Rule getRule(Long id) {
        return (Rule) getHibernateTemplate().get(Rule.class, id);
    }

    public List<Rule> getAllRules() {
        return getHibernateTemplate().find("from Rule");
    }

    public List<Rule> getRulesWithSurtPrefix(String prefix) {
        // escape wildcard characters % and _ using ! as the escape character.
        prefix = prefix.replace("!", "!!").replace("%", "!%")
                .replace("_", "!_");
        return getHibernateTemplate().find(
                "from Rule rule where rule.surt like ? escape '!'",
                prefix + "%");
    }

    public List<Rule> getRulesWithExactSurt(String surt) {
        return getHibernateTemplate().find(
                "from Rule rule where rule.surt = ?", surt);
    }
    
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
     */
    public RuleSet getRuleTree(String surt) {
        RuleSet rules = new RuleSet();
        
        // add the root SURT
        rules.addAll(getRulesWithExactSurt("("));
        
        boolean first = true;
        for (String search: new NewSurtTokenizer(surt).getSearchList()) {
            if (first) {
                first = false;
                rules.addAll(getRulesWithSurtPrefix(search));
            } else {
                rules.addAll(getRulesWithExactSurt(search));
            }
        }
        
        return rules;
    }

    public void saveRule(Rule rule) {
        getHibernateTemplate().saveOrUpdate(rule);
    }
    
    public boolean saveRuleIfNotDup(Rule rule)
    {
    	List<Rule> allRules = getAllRules();
    	
    	for (Rule existingRule : allRules)
    	{
    		if (existingRule.compareTo(rule) == 0) {
    			// If we're not the same, rule then we're a dup!
    			if ((rule.getId() == null) || !rule.getId().equals(existingRule.getId())) {
    				return false;	
    			}
    		}
    	}
    	
    	saveRule(rule);
    	return true;
    } 
    
    /**
     * Save a rule and a change log entry in one go. (Uses a transaction).
     * @param rule
     * @param change
     */
    public void saveRule(Rule rule, RuleChange change) {
        Session session1 = getHibernateTemplate().getSessionFactory().openSession();
        Transaction tx = session1.beginTransaction();
        session1.saveOrUpdate(rule);
        session1.save(change);
        tx.commit();
        session1.close();
    }
    
    public void saveChange(RuleChange change) {
        getHibernateTemplate().saveOrUpdate(change);
    }
    
    public void deleteRule(Long id) {
        Object record = getHibernateTemplate().load(Rule.class, id);
        getHibernateTemplate().delete(record);
    }
    
    public void deleteAllRules() {
        getHibernateTemplate().bulkUpdate("delete from Rule");
    }

    public void prepare(Collection<String> surts) {
        // no-op
    }
}
