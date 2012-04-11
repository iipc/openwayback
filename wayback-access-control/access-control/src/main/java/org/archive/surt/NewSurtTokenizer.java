package org.archive.surt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The new SURT tokenizer breaks a SURT up into tokens.
 * 
 * For example "http://(org,archive,www,)/path/file.html?query#anchor" is broken up into:
 * 
 * ["http://(" 
 *  "org,"
 *  "archive," 
 *  "www," 
 *  ")/"
 *  "path/"
 *  "file.html"
 *  "?query"
 *  "#anchor"]
 * 
 * @author aosborne
 *
 */
public class NewSurtTokenizer implements Iterable<String> {
    private String surt;
    private int endOfAuthority;
    private int endOfPath;
    private int surtLength;
    private int preTabLength;

    public NewSurtTokenizer(String surt) {
        super();
        this.surt = surt;
        surtLength = surt.length();
        
        if (surt.charAt(surtLength - 1) == '\t') {
            preTabLength = surtLength - 1;
        } else {
            preTabLength = surtLength;
        }
        
        endOfAuthority = surt.indexOf(')');
        if (endOfAuthority == -1) {
            endOfAuthority = surtLength;
        }
        
        int hash = surt.indexOf('#');
        int question = surt.indexOf('?');
        if (hash == -1) {
            endOfPath = question;
        } else if (question == -1) {
            endOfPath = hash;
        } else {
            endOfPath = hash < question ? hash : question;
        }
        if (endOfPath == -1) {
            endOfPath = surtLength;
        }
        
    }

    private class NewSurtTokenizerIterator implements Iterator<String> {
        int pos = 0;

        public boolean hasNext() {
            return pos < surtLength;
        }

        private int nextPieceEnd() {
            // Special case: If the SURT ends with a tab, we treat that as an extra token.
            // A trailing tab is sometimes used (for better or worse) to make a distinction between
            // and exact match and prefix match.
            if (pos >= preTabLength && pos < surtLength) {
                return surtLength;
            }
            
            // Scheme: "http://(..."
            if (pos == 0) {
                int i = surt.indexOf('(');
                if (i == -1) {
                    return preTabLength;
                }
                return i + 1; // "http://("
            }
            // Host components: "foo,..."
            if (pos < endOfAuthority || endOfAuthority == -1) {
                int endOfHostComponent = surt.indexOf(',', pos);
                if (endOfHostComponent == -1) {
                    return preTabLength;
                } else {
                    return endOfHostComponent + 1;
                }
            } 
            
            // Host index: ")/..."
            if (pos == endOfAuthority) {
                return pos + 2;
            }
            
            // Path segments: "directory/"
            if (pos < endOfPath || endOfPath == -1) {
                int endOfPathSegment = surt.indexOf('/', pos);
                if (endOfPathSegment < endOfPath && endOfPathSegment != -1) {
                    return endOfPathSegment + 1;
                } else if (endOfPath != -1) { // file: "hello.html"
                    return endOfPath;
                } else {
                    return preTabLength;
                }   
            }
            
            // Query string
            if (surt.charAt(pos) == '?') {
                int endOfQuery = surt.indexOf('#');
                if (endOfQuery != -1) {
                    return endOfQuery;
                } else {
                    return preTabLength;
                }
            }
            
            // Anchor "#boo"
            return preTabLength;
        }

        public String next() {
            int pieceEnd = nextPieceEnd();
            String piece = surt.substring(pos, pieceEnd);
            pos = pieceEnd;
            return piece;
        }

        public void remove() {
            // TODO Auto-generated method stub

        }

    }

    public Iterator<String> iterator() {
        return new NewSurtTokenizerIterator();
    }

    public List<String> toList() {
        List<String> list = new ArrayList<String>();
        for (String piece: this) {
            list.add(piece);
        }
        return list;
    }
    public String[] toArray() {
        return (String[]) toList().toArray();
    }

    /**
     * Return a list of searches in order of decreasing length.  For example
     * given the surt "(org,archive,)/fishing" return:
     * 
     * [ "(org,archive,)/fishing",
     *   "(org,archive,)/",
     *   "(org,archive,",
     *   "(org,",
     *   "("
     * ]
     * @return
     */
    public List<String> getSearchList() {
        List<String> searches = new ArrayList<String>();
        String running = "";
        for (String token: this) {
            running += token;
            searches.add(0, running);
        }
        return searches;
    }

}
