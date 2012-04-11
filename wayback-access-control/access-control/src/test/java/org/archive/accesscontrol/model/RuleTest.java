package org.archive.accesscontrol.model;

import java.util.GregorianCalendar;

import org.archive.accesscontrol.model.Rule;

import junit.framework.TestCase;

public class RuleTest extends TestCase {
    Rule rule;
    
    public void testBasicMatching() {
        rule = new Rule();
        rule.setSurt("(org,archive,)/");      
        assertTrue("exact surt match", rule.matches("(org,archive,)/"));
        assertTrue("prefix surt match", rule.matches("(org,archive,)/foobar/fishing.html?foo=bar"));
        assertFalse("surt non-match", rule.matches("(org,example,)/blah"));
        assertTrue("capture date always match", rule.matches("(org,archive,)/", new GregorianCalendar(2007, 4, 12).getTime()));
        assertTrue("retrieval date always match", rule.matches("(org,archive,)/", null, new GregorianCalendar(2007, 4, 12).getTime()));
        assertTrue("capture and retrieval date always match", rule.matches("(org,archive,)/", new GregorianCalendar(2007, 4, 12).getTime(), new GregorianCalendar(2007, 4, 12).getTime()));
        assertFalse("capture and retrieval date, surt non-match", rule.matches("(org,example,)/", new GregorianCalendar(2007, 4, 12).getTime(), new GregorianCalendar(2007, 4, 12).getTime()));
        
    }
    
    public void testCaptureDateMatching() {
        rule = new Rule();
        rule.setSurt("(org,archive,)/");  
        
        rule.setCaptureStart(new GregorianCalendar(2007, 1, 12).getTime());
        assertTrue("start-bounded capture date match", rule.matches("(org,archive,)/", new GregorianCalendar(2007, 4, 12).getTime()));
        assertFalse("start-bounded capture date non-match", rule.matches("(org,archive,)/", new GregorianCalendar(2005, 4, 12).getTime()));

        rule.setCaptureStart(null);
        rule.setCaptureEnd(new GregorianCalendar(2007, 1, 12).getTime());
        assertTrue("end-bounded capture date match", rule.matches("(org,archive,)/", new GregorianCalendar(2005, 4, 12).getTime()));
        assertFalse("end-bounded capture date non-match", rule.matches("(org,archive,)/", new GregorianCalendar(2007, 4, 12).getTime()));
                
        rule.setCaptureStart(new GregorianCalendar(2007, 1, 12).getTime());
        rule.setCaptureEnd(new GregorianCalendar(2007, 12, 12).getTime());
        assertTrue("capture date match", rule.matches("(org,archive,)/", new GregorianCalendar(2007, 4, 12).getTime()));
        assertFalse("capture date too early", rule.matches("(org,archive,)/", new GregorianCalendar(2005, 1, 11).getTime()));
        assertFalse("capture date too late", rule.matches("(org,archive,)/", new GregorianCalendar(2008, 9, 11).getTime()));       
    }
    
    public void testRetrievalDateMatching() {
        rule = new Rule();
        rule.setSurt("(org,archive,)/");  
        
        rule.setRetrievalStart(new GregorianCalendar(2007, 1, 12).getTime());
        assertTrue("start-bounded retrieval date match", rule.matches("(org,archive,)/", null, new GregorianCalendar(2007, 4, 12).getTime()));
        assertFalse("start-bounded retrieval date non-match", rule.matches("(org,archive,)/", null, new GregorianCalendar(2005, 4, 12).getTime())); 
        
        rule.setRetrievalStart(null);
        rule.setRetrievalEnd(new GregorianCalendar(2007, 1, 12).getTime());
        assertTrue("end-bounded retrieval date match", rule.matches("(org,archive,)/", null, new GregorianCalendar(2005, 4, 12).getTime()));        
        assertFalse("end-bounded retrieval date non-match", rule.matches("(org,archive,)/", null, new GregorianCalendar(2007, 4, 12).getTime()));   
                
        rule.setRetrievalStart(new GregorianCalendar(2007, 1, 12).getTime());
        rule.setRetrievalEnd(new GregorianCalendar(2007, 12, 12).getTime());
        assertTrue("retrieval date match", rule.matches("(org,archive,)/", null, new GregorianCalendar(2007, 4, 12).getTime()));
        assertFalse("retrieval date too early", rule.matches("(org,archive,)/", null, new GregorianCalendar(2005, 1, 11).getTime()));    
        assertFalse("retrieval date too late", rule.matches("(org,archive,)/", null, new GregorianCalendar(2008, 9, 11).getTime()));
    }
    
    public void testEmbargoPeriodMatching() {
        rule = new Rule();
        rule.setSurt("(org,archive,)/");
        
        rule.setSecondsSinceCapture(60 * 60 * 24 * 5); // embargo period of 5 days
        assertFalse("under embargo, so non-match", rule.matches("(org,archive,)/", new GregorianCalendar(2007, 4, 10).getTime(), new GregorianCalendar(2007, 4, 12).getTime()));
        assertTrue("outside embargo, so match", rule.matches("(org,archive,)/", new GregorianCalendar(2007, 4, 10).getTime(), new GregorianCalendar(2007, 4, 25).getTime()));
    }
    
    public void testBlankGroupMatching() {
    	rule = new Rule();
    	rule.setSurt("(");
    	rule.setWho("");
    	assertTrue(rule.matches("(org,",null, null, "blah"));
    }
}
