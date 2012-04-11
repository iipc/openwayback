package org.archive.accesscontrol.webui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.archive.accesscontrol.model.Rule;

/**
 * Wrapper for Rule that holds extra fields and methods useful for rendering a rule.
 * @author ato
 *
 */
public class DisplayRule implements Comparable<DisplayRule> {
    private Rule rule;
    private boolean inherited;
    private boolean editing;
    private boolean highlight;
    
    public DisplayRule(Rule rule, boolean inherited) {
        super();
        this.rule = rule;
        this.inherited = inherited;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public int compareTo(DisplayRule o) {
        return getRule().compareTo(o.getRule());
    }

    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
    
    public String getEncodedSurt() throws UnsupportedEncodingException {
        return URLEncoder.encode(rule.getSurt(), "utf-8");
    }

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;		
	}
	
	public boolean isHighlight()
	{
		return this.highlight;
	}
    
}
