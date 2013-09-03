package org.archive.cdxserver.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;
import org.archive.format.cdx.FieldSplitLine;

/**
 * Matches a FieldSplitLine against a string of regex
 * Supports matching against individual fields if specified
 * eg:
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
	final static String FIELD_SEP_CHAR = ":";
	
	final protected FieldSplitFormat names;
	final protected List<RegexMatch> regexMatchers;
	
	class RegexMatch {
		final Pattern regex;
		final boolean inverted;
		final int fieldIndex;
		
		RegexMatch(String str)
		{
			try {
				str = URLDecoder.decode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {

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
				regex = Pattern.compile(str);
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
			regex = Pattern.compile(pattern);			
		}
		
		boolean matches(FieldSplitLine line)
		{
			boolean matched;
			
			if (fieldIndex < 0) {
				matched = regex.matcher(line.toString()).find();
			} else {
				matched = regex.matcher(line.getField(fieldIndex)).find();
			}
			
			if (inverted) {
				matched = !matched;
			}
			
			return matched;
		}
	}
	
	
	public FieldRegexFilter(String[] regexs, FieldSplitFormat names)
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
