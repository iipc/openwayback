package org.archive.wayback.resourcestore.resourcefile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FilenameFilter which allows flexible configuration of accepted files. All files matching accepRegex are accepted,
 * until they match rejectRegex.
 *
 * @author mbitzl
 * @version $Date$, $Revision$
 */
public class RegexFilenameFilter implements FilenameFilter {
    private Pattern accept;
    private Pattern reject;

    private boolean should(Pattern pattern, String name) {
        if (pattern == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    @Override
    public boolean accept(File dir, String name) {
        return should(accept, name) && !should(reject, name);
    }

    public void setAcceptRegex(String acceptRegex) {
        this.accept = Pattern.compile(acceptRegex);
    }

    /**
     * All files with filenames matching this regular expression will be considered to accept.
     *
     */
    public String getAcceptRegex() {
        return accept.pattern();
    }

    public void setRejectRegex(String rejectRegex) {
        this.reject = Pattern.compile(rejectRegex);
    }


    /**
     * All files with filenames matching this regular expression will be rejected, even if they would match acceptRegex.
     *
     */
    public String getRejectRegex() {
        return reject.pattern();
    }
}
