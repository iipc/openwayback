package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.FileRegexFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.flatfile.FlatFile;

public class FileRegexParamFilterAndFactory extends FileRegexFilter implements CustomResultFilterFactory {
	
	private static final Logger LOGGER =
        Logger.getLogger(FileRegexParamFilterAndFactory.class.getName());
	
	protected String paramFile;
	protected int paramIndex = 1;
	protected boolean isExclusion = true;
	protected char delim = '\t';
	
	protected String prefixMatch;
	
	protected Set<String> paramSet = null;
	
	// This method should be set as the init-method in the spring config
	// init-method="loadParamFile" when using this Filter
	public void loadParamFile()
	{
		FlatFile ff = new FlatFile(paramFile);
		
				
		CloseableIterator<String> itr = null;
		
		try {
			itr = ff.getSequentialIterator();
		} catch (IOException io) {
			LOGGER.warning(io.toString());
		}
		
		paramSet = new HashSet<String>();

		while (itr.hasNext()) {
			String param = itr.next();
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
	}

	// Filter and Factory are the same object to avoid creating a new object that is unmodified during
	// the filtering process
	
	@Override
	public ObjectFilter<CaptureSearchResult> get(AccessPoint ap) {
		return this;
	}
	
	@Override
	public int filterObject(CaptureSearchResult o) {
		final String file = o.getFile();
		boolean matched = false;
		
		if (prefixMatch != null) {
			if (!file.startsWith(prefixMatch)) {
				return (isExclusion ? FILTER_INCLUDE : FILTER_EXCLUDE);
			}
		}
		
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
		
		if (isExclusion) {
			return (matched ? FILTER_EXCLUDE : FILTER_INCLUDE);	
		} else {
			return (matched ? FILTER_INCLUDE : FILTER_EXCLUDE);
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

	public String getPrefixMatch() {
		return prefixMatch;
	}

	public void setPrefixMatch(String prefixMatch) {
		this.prefixMatch = prefixMatch;
	}
}
