package org.archive.accesscontrol.model;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public abstract class DaoTestCase extends TestCase {
    protected ApplicationContext ctx = null;

    public DaoTestCase() {
        // Should put in a parent class that extends TestCase
        String[] paths = { "applicationContext.xml" };
        ctx = new ClassPathXmlApplicationContext(paths);
    }
}
