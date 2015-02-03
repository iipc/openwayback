package org.archive.cdxserver.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.format.cdx.CDXLine;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;

public class FilenamePrefixFilter implements CDXFilter {

	private static final Logger LOGGER = Logger
		.getLogger(FilenamePrefixFilter.class.getName());

	protected String paramFile;
	protected int paramIndex = 1;
	protected boolean isExclusion = true;
	protected char delim = '\t';

	protected String containsMatch;

	protected Set<String> paramSet = null;
	protected Pattern patterns[] = null;

	protected List<String> prefixList = null;

	/**
	 * Default constructor. Call setters to configure.
	 */
	public FilenamePrefixFilter() {
	}

	/**
	 * Initialize with essential configuration parameters.
	 * @param prefixList List of filename prefixes
	 * @param exclusion disposition: {@code true} for exclusion
	 */
	public FilenamePrefixFilter(List<String> prefixList, boolean exclusion) {
		this.prefixList = prefixList;
		this.isExclusion = exclusion;
	}

	// This method should be set as the init-method in the spring config
	// init-method="loadParamFile" when using this Filter
	public void loadParamFile() throws IOException {
		SeekableLineReaderFactory fact = null;
		SeekableLineReaderIterator iter = null;

		try {
			fact = GeneralURIStreamFactory.createSeekableStreamFactory(
				paramFile, false);
			iter = new SeekableLineReaderIterator(fact.get());

			paramSet = new HashSet<String>();

			while (iter.hasNext()) {
				String param = iter.next();
				param = param.trim();

				if (param.isEmpty() || param.startsWith("#")) {
					continue;
				}

				// Use only the first word, ignore the rest
				int wordEnd = param.indexOf(delim);

				if (wordEnd > 0) {
					param = param.substring(0, wordEnd);
				}

				paramSet.add(param);
			}
		} finally {
			if (iter != null) {
				iter.close();
			}

			if (fact != null) {
				fact.close();
			}
		}
	}

	public boolean include(CDXLine line) {
		final String file = line.getFilename();
		boolean matched = false;

		if (containsMatch != null) {
			if (!file.contains(containsMatch)) {
				return (isExclusion ? true : false);
			}
		}

		if (prefixList != null) {
			for (String prefix : prefixList) {
				if (file.startsWith(prefix)) {
					return (isExclusion ? false : true);
				}
			}
		}

		if (patterns != null) {
			for (Pattern pattern : patterns) {
				Matcher matcher = pattern.matcher(file);
				if (matcher.find()) {
					String param = matcher.group(paramIndex);
					if (paramSet.contains(param)) {
						if (LOGGER.isLoggable(Level.FINE)) {
							LOGGER.fine("Excluding (w)arc: " + file);
						}
						matched = true;
						break;
					}
				}
			}
		}

		if (isExclusion) {
			return (matched ? false : true);
		} else {
			return (matched ? true : false);
		}
	}

	//Getters/Setters

	public String getParamFile() {
		return paramFile;
	}

	public void setParamFile(String paramFile) {
		this.paramFile = paramFile;
	}

	public int getParamIndex() {
		return paramIndex;
	}

	public void setParamIndex(int paramIndex) {
		this.paramIndex = paramIndex;
	}

	public boolean isExclusion() {
		return isExclusion;
	}

	public void setExclusion(boolean isExclusion) {
		this.isExclusion = isExclusion;
	}

	public char getDelim() {
		return delim;
	}

	public void setDelim(char delim) {
		this.delim = delim;
	}

	public String getContainsMatch() {
		return containsMatch;
	}

	public void setContainsMatch(String containsMatch) {
		this.containsMatch = containsMatch;
	}

	public List<String> getPatterns() {
		ArrayList<String> s = new ArrayList<String>();
		for (Pattern p : patterns) {
			s.add(p.pattern());
		}
		return s;
	}

	public void setPatterns(List<String> patternStrings) {
		int size = patternStrings.size();
		patterns = new Pattern[size];
		for (int i = 0; i < size; i++) {
			patterns[i] = Pattern.compile(patternStrings.get(i));
		}
	}

	public List<String> getPrefixList() {
		return prefixList;
	}

	public void setPrefixList(List<String> prefixList) {
		this.prefixList = prefixList;
	}
}
