package org.archive.accesscontrol;

import org.archive.accesscontrol.AccessControlClient;

import junit.framework.TestCase;

public class AccessControlClientTest extends TestCase {
    public static final String ORACLE_URL = "http://localhost:8080/oracle-0.0.1-SNAPSHOT/";
    private AccessControlClient client;
        
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("hello world");
        client = new AccessControlClient(ORACLE_URL);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        client = null;
    }
    
    public void testBasicOkToShow() throws Exception {
        //String policy = client.getPolicy("http://www.peagreenboat.com/", new Date(1987, 8, 30), new Date(), "blah");
        //System.out.println("Policy=" + policy);
    }
    
}
