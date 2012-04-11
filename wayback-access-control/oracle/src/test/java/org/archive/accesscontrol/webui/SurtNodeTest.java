package org.archive.accesscontrol.webui;

import java.util.Iterator;

import junit.framework.TestCase;

public class SurtNodeTest extends TestCase {
    public void testNodesFromSurt() {
        Iterator<SurtNode> it = SurtNode.nodesFromSurt("(org,archive,)/about").iterator();
        assertEquals("(", it.next().getName());
        assertEquals("(org,", it.next().getSurt());
        assertEquals("archive,", it.next().getName());
        assertEquals("(org,archive,)/", it.next().getSurt());
        assertEquals("about", it.next().getName());
        assertFalse(it.hasNext());
    }
}
