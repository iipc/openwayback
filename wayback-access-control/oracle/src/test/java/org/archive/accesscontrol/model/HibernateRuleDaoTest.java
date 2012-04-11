package org.archive.accesscontrol.model;

import java.util.List;
import org.archive.accesscontrol.model.HibernateRuleDao;
import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleChange;

import junit.framework.Assert;

public class HibernateRuleDaoTest extends DaoTestCase {
    private Rule rule = null;
    private HibernateRuleDao dao = null;
    

    protected void setUp() throws Exception {
        super.setUp();
        dao = (HibernateRuleDao) ctx.getBean("ruleDao");
        
        // clear database of rules
        for (Rule rule: dao.getAllRules()) {
            try {
                dao.deleteRule(rule.getId());
            } catch (Exception e) {}
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dao = null;
    }

    public void testSaveRecord() throws Exception {
        rule = new Rule();
        rule.setSurt("org,archive");
        rule.setWho("admins");
        dao.saveRule(rule);
        Assert.assertNotNull("primary key assigned", rule.getId());
    }
    
    public void testChange() throws Exception {
        RuleChange change = new RuleChange();
        change.setSurt("org,archive");
        change.setWho("admins");
        dao.saveChange(change);
        Assert.assertNotNull("primary key assigned", change.getId());
    }
    
    public void testSurtPrefixQuery() throws Exception {
        rule = new Rule();
        rule.setSurt("http://(org,archive,unique)/%%__/fish");
        dao.saveRule(rule);
       
        Rule rule2 = new Rule();
        rule2.setSurt("http://(org,archive,unique)/blasted/fish");
        dao.saveRule(rule2);
       
        List<Rule> rules = dao.getRulesWithSurtPrefix("http://(org,archive,unique)/%%__");
        
        Boolean foundRule1 = false;
        Boolean foundRule2 = false;
        for (Rule r: rules) {
            if (rule.getId().equals(r.getId())) {
                foundRule1 = true;
            }
            if (rule2.getId().equals(r.getId())) {
                foundRule2 = true;
            }
        }
        Assert.assertTrue("Should match rule 1", foundRule1);
        Assert.assertFalse("Should not match rule 2", foundRule2);
    }
    
    public void testExactSurtQuery() throws Exception {
        rule = new Rule();
        rule.setSurt("http://(org,archive,unique)/%%__/fish");
        dao.saveRule(rule);
       
        Rule rule2 = new Rule();
        rule2.setSurt("http://(org,archive,unique)/blasted/fish");
        dao.saveRule(rule2);
       
        
        List<Rule> rules = dao.getRulesWithExactSurt("http://(org,archive,unique)/%%__/fish");
        
        Boolean foundRule1 = false;
        Boolean foundRule2 = false;
        for (Rule r: rules) {
            if (rule.getId().equals(r.getId())) {
                foundRule1 = true;
            }
            if (rule2.getId().equals(r.getId())) {
                foundRule2 = true;
            }
        }
        
        Assert.assertTrue("Should match rule 1", foundRule1);
        Assert.assertFalse("Should not match rule 2", foundRule2);
    }
    
    public void testSurtTreeQuery() throws Exception {
        rule = new Rule();
        rule.setSurt("(org,archive,unique,)/secret");
        dao.saveRule(rule);

        Rule rule1 = new Rule();
        rule1.setSurt("(org,archive,unique,)/");
        dao.saveRule(rule1);
        
        Rule rule2 = new Rule();
        rule2.setSurt("(org,archive,unique");
        dao.saveRule(rule2);

        Rule rule3 = new Rule();
        rule3.setSurt("(org,archive");
        dao.saveRule(rule3);
        
        Rule rule4 = new Rule();
        rule4.setSurt("(");
        dao.saveRule(rule4);
        
        Rule rule5 = new Rule();
        rule5.setSurt("(org,archive,unique,)/other");
        dao.saveRule(rule5);
        
        Rule rule6 = new Rule();
        rule6.setSurt("(org,error,)/foobar");
        dao.saveRule(rule6);
        
        Iterable<Rule> rules = dao.getRuleTree("http://(org,archive,unique,)/");
        
        for (Rule r: rules) {
            assertTrue(r.getId().equals(rule.getId()) 
                    || r.getId().equals(rule1.getId()) 
                    || r.getId().equals(rule2.getId())
                    || r.getId().equals(rule3.getId())
                    || r.getId().equals(rule4.getId())
                    || r.getId().equals(rule5.getId()));
        }
    }

}
