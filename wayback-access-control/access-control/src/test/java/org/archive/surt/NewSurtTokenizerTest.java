package org.archive.surt;

import java.util.Iterator;

import junit.framework.TestCase;

public class NewSurtTokenizerTest extends TestCase {
    
    public void testRoot() {
        NewSurtTokenizer tok = new NewSurtTokenizer("http://(");
        Iterator<String> it = tok.iterator();
        
        assertEquals("http://(", it.next());
        assertFalse(it.hasNext());
        
    }

    public void testOneSeg() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org", it.next());
        assertFalse(it.hasNext());
    }

    public void testOneSegComma() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertFalse(it.hasNext());
    }

    public void testFewSegs() {
        NewSurtTokenizer tok = new NewSurtTokenizer("http://(org,archive,www");
        Iterator<String> it = tok.iterator();
        
        assertEquals("http://(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www", it.next());
        assertFalse(it.hasNext());
    }

    public void testFewSegsComma() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertFalse(it.hasNext());
    }

    public void testIndex() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertFalse(it.hasNext());
    }
    
    public void testPage() {
        NewSurtTokenizer tok = new NewSurtTokenizer("http://(org,archive,www,)/about.html");
        Iterator<String> it = tok.iterator();
        
        assertEquals("http://(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertEquals("about.html", it.next());
        assertFalse(it.hasNext());
    }

    public void testPath() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/one/two/");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertEquals("one/", it.next());
        assertEquals("two/", it.next());
        assertFalse(it.hasNext());
    }

    
    public void testPathPage() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/one/two/about.html");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertEquals("one/", it.next());
        assertEquals("two/", it.next());
        assertEquals("about.html", it.next());
        assertFalse(it.hasNext());
    }

 
    public void testQuery() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/one/two/about.html?yo=hey&hi");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertEquals("one/", it.next());
        assertEquals("two/", it.next());
        assertEquals("about.html", it.next());
        assertEquals("?yo=hey&hi", it.next());
        assertFalse(it.hasNext());
    }


    public void testAnchor() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/one/two/about.html#fishing/,)fish(?moo");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertEquals("one/", it.next());
        assertEquals("two/", it.next());
        assertEquals("about.html", it.next());
        assertEquals("#fishing/,)fish(?moo", it.next());
        assertFalse(it.hasNext());
    }
    
    public void testQueryAnchor() {
        NewSurtTokenizer tok = new NewSurtTokenizer("ftp://(org,archive,www,)/fishes/pinky.html?moo=yes&bar=12#423");
        Iterator<String> it = tok.iterator();
        
        assertEquals("ftp://(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertEquals("fishes/", it.next());
        assertEquals("pinky.html", it.next());
        assertEquals("?moo=yes&bar=12", it.next());
        assertEquals("#423", it.next());
        assertFalse(it.hasNext());
        
    }
    
    public void testListDiffing() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/fishes/");
        NewSurtTokenizer tok2 = new NewSurtTokenizer("(org,archive,www,)/fishes/pinky.html");
        assertEquals("pinky.html", tok2.toList().get(tok.toList().size()));
    }
    
    public void testTrailingTab() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/fishes/pinky.html?moo=yes&bar=12#423\t");
        Iterator<String> it = tok.iterator();
        
        assertEquals("(", it.next());
        assertEquals("org,", it.next());
        assertEquals("archive,", it.next());
        assertEquals("www,", it.next());
        assertEquals(")/", it.next());
        assertEquals("fishes/", it.next());
        assertEquals("pinky.html", it.next());
        assertEquals("?moo=yes&bar=12", it.next());
        assertEquals("#423", it.next());
        assertEquals("\t", it.next());        
        assertFalse(it.hasNext());
        
    }

    public void testSearchList() {
        NewSurtTokenizer tok = new NewSurtTokenizer("(org,archive,www,)/fishes/pinky.html?moo=yes&bar=12#423");
        Iterator<String> it = tok.getSearchList().iterator();
        
        assertEquals("(org,archive,www,)/fishes/pinky.html?moo=yes&bar=12#423", it.next());
        assertEquals("(org,archive,www,)/fishes/pinky.html?moo=yes&bar=12", it.next());
        assertEquals("(org,archive,www,)/fishes/pinky.html", it.next());
        assertEquals("(org,archive,www,)/fishes/", it.next());
        assertEquals("(org,archive,www,)/", it.next());
        assertEquals("(org,archive,www,", it.next());
        assertEquals("(org,archive,", it.next());
        assertEquals("(org,", it.next());
        assertEquals("(", it.next());
        assertFalse(it.hasNext());
    }
}
