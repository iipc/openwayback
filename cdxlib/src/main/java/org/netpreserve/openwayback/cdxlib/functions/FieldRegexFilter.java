package org.netpreserve.openwayback.cdxlib.functions;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.netpreserve.openwayback.cdxlib.CdxLine;

/**
 * Matches a FieldSplitLine against a string of regex.
 * <p>
 * Supports matching against individual fields if specified eg:
 * <p>
 * ~&lt;containsstr&gt; = look for containing string {@code <containsstr>} and not a regex
 * <p>
 * &lt;regex&gt; = match whole line &lt;field&gt;:&lt;regex&gt; = match &lt;field&gt; in
 * FieldSplitLine, by name or number, and match only that field
 * <p>
 * Supports !&lt;regex&gt; for not matching
 * <p>
 * @author ilya
 * <p>
 */
public class FieldRegexFilter implements Filter {

    static final String INVERT_CHAR = "!";

    static final String CONTAINS_CHAR = "~";

    static final String FIELD_SEP_CHAR = ":";

    final List<RegexMatch> regexMatchers;

    class RegexMatch {

        final Pattern regex;

        final boolean inverted;

        final String containsStr;

        String field;

        RegexMatch(String str) {
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
                if (contains) {
                    containsStr = str;
                    regex = null;
                } else {
                    containsStr = null;
                    regex = Pattern.compile(str);
                }
                return;
            }

            field = str.substring(0, sepIndex);
            String pattern = str.substring(sepIndex + 1);

            if (contains) {
                containsStr = pattern;
                regex = null;
            } else {
                containsStr = null;
                regex = Pattern.compile(pattern);
            }
        }

        boolean matches(CdxLine line) {
            boolean matched;

            if (field == null) {
                if (containsStr != null) {
                    matched = String.valueOf(line).contains(containsStr);
                } else {
                    matched = regex.matcher(line.getInputLine()).matches();
                }
            } else {
                if (containsStr != null) {
                    matched = String.valueOf(line.get(field)).contains(containsStr);
                } else {
                    matched = regex.matcher(line.get(field)).matches();
                }
            }

            if (inverted) {
                matched = !matched;
            }

            return matched;
        }

    }

    public FieldRegexFilter(List<String> regexs) {
        this.regexMatchers = new ArrayList<RegexMatch>(regexs.size());

        for (String regex : regexs) {
            if (!regex.isEmpty()) {
                regexMatchers.add(new RegexMatch(regex));
            }
        }
    }

    @Override
    public boolean include(CdxLine line) {
        for (RegexMatch regexMatch : regexMatchers) {
            if (!regexMatch.matches(line)) {
                return false;
            }
        }

        return true;
    }

}
