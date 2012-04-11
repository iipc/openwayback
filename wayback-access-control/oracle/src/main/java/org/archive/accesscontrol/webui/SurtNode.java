package org.archive.accesscontrol.webui;

import java.util.ArrayList;
import java.util.List;

import org.archive.surt.NewSurtTokenizer;

/**
 * A node in the SURT tree.
 * 
 * @author aosborne
 *
 */
public class SurtNode {
    private String name;
    private String surt;
    
    public SurtNode(String name, String surt) {
        super();
        this.name = name;
        this.surt = surt;
    }
    public String getName() {
        return name;
    }
    public String getSurt() {
        return surt;
    }
    
    /**
     * Return a list of the elements in a given SURT.
     * 
     * For example for "(org,archive," we return:
     * 
     *  [new SurtNode("(", "("),
     *   new SurtNode("org,", "(org"),
     *   new SurtNode("archive,", "archive,")]
     * 
     * @param surt
     * @return
     */
    public static List<SurtNode> nodesFromSurt(String surt) {
        List<SurtNode> list = new ArrayList<SurtNode>();
        String running = "";
        for (String token: new NewSurtTokenizer(surt)) {
            running += token;
            list.add(new SurtNode(token, running));
        }
        return list;
    }
}
