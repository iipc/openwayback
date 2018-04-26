package org.archive.cdxserver.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.archive.cdxserver.format.CDXFormat;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitLine;

/**
 * Matches a FieldSplitLine against a string of regex
 * Supports matching against individual fields if specified
 * eg:
 * 
 * ~<containsstr> = look for containing sting <containsstr> and not a regex
 * 
 * <regex> = match whole line
 * <field>:<regex> = match <field> in FieldSplitLine, by name or number, and match only that field
 * 
 * Supports !<regex> for not matching
 * 
 * @author ilya
 *
 */
public class FieldRegexFilter implements CDXFilter {
	
	final static String INVERT_CHAR = "!";
	final static String CONTAINS_CHAR = "~";
	final static String FIELD_SEP_CHAR = ":";
	
	final protected CDXFormat names;
	final protected List<RegexMatch> regexMatchers;
	
	class RegexMatch {
		final Pattern regex;
		final boolean inverted;
		final String containsStr;
		final int fieldIndex;
		
		RegexMatch(String str)
		{
			boolean contains = false;
			
			if (str.startsWith(CONTAINS_CHAR)) {
				str = str.substring(1);
				contains = true;
			}
			
			if (str.startsWith(INVERT_CHAR)) {
				str = str.substring(1);
				inverted = true;
			} else {
				inverted = false;
			}
			
			int sepIndex = str.indexOf(FIELD_SEP_CHAR);
			
			// Match entire line
			if (sepIndex < 0) {
				fieldIndex = -1;
				if (contains) {
					containsStr = str;
					regex = null;
				} else {
					containsStr = null;
					regex = Pattern.compile(str);
				}
				return;
			}
			
			String field = str.substring(0, sepIndex);
			String pattern = str.substring(sepIndex + 1);
			
			int index = -1;
			
			// First try parsing as int
			try {
				index = Integer.parseInt(field);
			} catch (NumberFormatException n) {
				
			}
			
			// Then try names if available
			if ((index < 0) && (names != null)) {
				index = names.getFieldIndex(field);
			}
			
			fieldIndex = index;
			
			if (contains) {
				containsStr = pattern;
				regex = null;
			} else {
				containsStr = null;
				regex = Pattern.compile(pattern);
			}		
		}
		
		boolean matches(FieldSplitLine line)
		{
			boolean matched;
			
			if (fieldIndex < 0) {
				if (containsStr != null) {
					matched = line.toString().contains(containsStr);
				} else {
					matched = regex.matcher(line.toString()).matches();	
				}
			} else {
				if (containsStr != null) {
					matched = line.getField(fieldIndex).contains(containsStr);
				} else {
					matched = regex.matcher(line.getField(fieldIndex)).matches();
				}
			}
			
			if (inverted) {
				matched = !matched;
			}
			
			return matched;
		}
	}
	
	
	public FieldRegexFilter(String[] regexs, CDXFormat names)
	{
		this.names = names;
		this.regexMatchers = new ArrayList<RegexMatch>(regexs.length);
		
		for (String regex : regexs) {
		    if (!regex.isEmpty()) {
		        regexMatchers.add(new RegexMatch(regex));
		    }
		}
	}
	
	public boolean include(CDXLine line)
	{
		for (RegexMatch regexMatch : regexMatchers) 
		{
			if (!regexMatch.matches(line)) {
				return false;
			}
		}
		
		return true;
	}
}
