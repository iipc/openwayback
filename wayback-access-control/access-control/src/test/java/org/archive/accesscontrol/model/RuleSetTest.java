package org.archive.accesscontrol.model;

import java.util.Date;
import java.util.GregorianCalendar;

import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleSet;

import junit.framework.TestCase;

public class RuleSetTest extends TestCase {
    private RuleSet ruleset;
    
    public void testSimple() {
        ruleset = new RuleSet();
        ruleset.add(new Rule("robots", "("));
        ruleset.add(new Rule("allow", "(org,archive,"));
        ruleset.add(new Rule("block", "(org,archive,)/secret/"));
        ruleset.add(new Rule("allow", "(org,archive,)/secret/public/"));
        
        for (Rule rule: ruleset) {
            System.out.println(rule.getSurt());
        }
     
        assertEquals("robots", ruleset.getMatchingRule("(org", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/fishing.html", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/secret/", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/secrets/dingos", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/secret/hamsters.html", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/secret/public/feeding.html", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/secret/publics", new Date(), new Date(), null).getPolicy());
        
    }

    public void testSimplePrecedence() {
        ruleset = new RuleSet();
        //ruleset.add(new Rule("robots", "("));
        ruleset.add(new Rule("allow", "("));
        ruleset.add(new Rule("block", "(org,archive,)/secret/"));
        ruleset.add(new Rule("allow", "(org,archive,)/secret/public/"));
        //ruleset.add(new Rule("block", "(org,archive,)/secret/public/"));
     
        assertEquals("allow", ruleset.getMatchingRule("(org", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/fishing.html", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/secret/", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/secret/hamsters.html", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/secret/public/feeding.html", new Date(), new Date(), null).getPolicy());
    }

    public void testEmbargo() {
        ruleset = new RuleSet();
        ruleset.add(new Rule("robots", "("));
        ruleset.add(new Rule("allow", "(org,archive,"));
        ruleset.add(new Rule("block", "(org,archive,)/classified/"));
        ruleset.add(new Rule("allow", "(org,archive,)/classified/", 60 * 60 * 24 * 90));
        
        Date captureDate = new GregorianCalendar(2007, 8, 1).getTime();
        Date preReleaseDate = new GregorianCalendar(2007, 8, 15).getTime();
        Date postReleaseDate = new GregorianCalendar(2009, 8, 15).getTime();
        
        assertEquals("robots", ruleset.getMatchingRule("(org", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/", new Date(), new Date(), null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/fishing.html", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/classified/", captureDate, preReleaseDate, null).getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/classified/index.html", captureDate, postReleaseDate, null).getPolicy());
    }
    
    public void testGroup() {
        ruleset = new RuleSet();
        ruleset.add(new Rule("robots", "("));
        ruleset.add(new Rule("allow", "(org,archive,", "archivists"));
        ruleset.add(new Rule("block", "(org,archive,)/classified/"));
        ruleset.add(new Rule("allow", "(org,archive,)/classified/", "admins"));
        
        assertEquals("robots", ruleset.getMatchingRule("(org,archive,www,)/index.html", new Date(), new Date(), "dinosaurs").getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,www,)/index.html", new Date(), new Date(), "archivists").getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/classified/presto", new Date(), new Date(), "admins").getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/classified/presto", new Date(), new Date(), "public").getPolicy());
        assertEquals("robots", ruleset.getMatchingRule("(org,archive,)/classified-photons", new Date(), new Date(), "public").getPolicy());               
    }
    
    public void testGroupMore() {
        ruleset = new RuleSet();
        ruleset.add(new Rule("allow", "("));
        ruleset.add(new Rule("block", "(org,", "coll"));
        ruleset.add(new Rule("block", "(org,archive,)/collonly/"));
        ruleset.add(new Rule("block", "(org,archive,)/collonly/index.html"));
        ruleset.add(new Rule("allow", "(org,archive,)/collonly/", "coll"));
        
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,www,)/index.html", new Date(), new Date(), "dinosaurs").getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,www,)/index.html", new Date(), new Date(), "coll").getPolicy());
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/collonly/index.html", new Date(), new Date(), "coll").getPolicy());        
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/collonly/index.html", new Date(), new Date(), "dinosaurs").getPolicy());        
    }
    
    public void testExact() {
        ruleset = new RuleSet();
        ruleset.add(new Rule("block", "(org,archive,)/", true));
        ruleset.add(new Rule("allow", "(org,archive,)/", false));
        
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/somefile", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/", new Date(), new Date(), null).getPolicy());
        
        
        ruleset = new RuleSet();
        ruleset.add(new Rule("allow", "(org,archive,)/", false));        
        ruleset.add(new Rule("block", "(org,archive,)/", true));
        
        assertEquals("allow", ruleset.getMatchingRule("(org,archive,)/somefile", new Date(), new Date(), null).getPolicy());
        assertEquals("block", ruleset.getMatchingRule("(org,archive,)/", new Date(), new Date(), null).getPolicy());        
    }
    
    public void testIterator() {
    	ruleset = new RuleSet();
    	assertFalse(ruleset.iterator().hasNext());
    }
    
}
