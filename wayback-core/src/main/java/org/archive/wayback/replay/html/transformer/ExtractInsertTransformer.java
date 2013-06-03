package org.archive.wayback.replay.html.transformer;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

public class ExtractInsertTransformer implements StringTransformer {
	
	public static class Rule
	{
		String urlkeyContains;
		
		String startAfter;
		String fromPrefix;
		String fromBeforeMatch;
		String untilAny;
		
		String insert;
		
		boolean insertAtEnd;
		
		public String getUrlkeyContains() {
			return urlkeyContains;
		}

		public String getStartAfter() {
			return startAfter;
		}

		public String getFromPrefix() {
			return fromPrefix;
		}

		public String getFromBeforeMatch() {
			return fromBeforeMatch;
		}

		public String getUntilAny() {
			return untilAny;
		}

		public String getInsert() {
			return insert;
		}

		public boolean isInsertAtEnd() {
			return insertAtEnd;
		}

		public void setUrlkeyContains(String urlkeyContains) {
			this.urlkeyContains = urlkeyContains;
		}

		public void setStartAfter(String startAfter) {
			this.startAfter = startAfter;
		}

		public void setFromPrefix(String fromPrefix) {
			this.fromPrefix = fromPrefix;
		}

		public void setFromBeforeMatch(String fromBeforeMatch) {
			this.fromBeforeMatch = fromBeforeMatch;
		}

		public void setUntilAny(String untilAny) {
			this.untilAny = untilAny;
		}

		public void setInsert(String insert) {
			this.insert = insert;
		}

		public void setInsertAtEnd(boolean insertAtEnd) {
			this.insertAtEnd = insertAtEnd;
		}		
	}
	
	protected List<Rule> rules;
	
	protected boolean matchOnce = false;
	
	public final static String EXTRACT_INSERT_MATCHED = "_extractormatched";
	
	@Override
	public String transform(ReplayParseContext context, String input) {
		
		if (context.getData(EXTRACT_INSERT_MATCHED) != null) {
			return input;
		}
		
		for (Rule rule : rules) {
			
			// Check urlScope
			if (rule.urlkeyContains != null) {
				CaptureSearchResult result = context.getCaptureSearchResult();
				if ((result != null) && !result.getUrlKey().contains(rule.urlkeyContains)) {
					continue;
				}
			}
			
			int index = input.indexOf(rule.startAfter);
			
			if (index < 0) {
				continue;
			}
			
			index += rule.startAfter.length();
			
			String insertion;
			
			if (rule.fromPrefix != null) {
				boolean matching = true;
				
				boolean skipRule = false;
				
				while (matching) {
					index = input.indexOf(rule.fromPrefix, index);	
					
					if (index < 0) {
						skipRule = true;
						break;
					}
					
					if (rule.fromBeforeMatch == null || (isAny(input.charAt(index - 1), rule.fromBeforeMatch))) {
						matching = false;
					}
					
					index += rule.fromPrefix.length();
				}
				
				if (skipRule) {
					continue;
				}
				
				int endIndex = index;
				
				while ((endIndex < input.length()) && !isAny(input.charAt(endIndex), rule.untilAny)) {
					endIndex++;
				}
				
				if (endIndex == index) {
					continue;
				}
				
				String extract = input.substring(index, endIndex);
				insertion = String.format(rule.insert, extract);
			} else {
				insertion = rule.insert;
			}
			
			if (rule.insertAtEnd) {
				input = input + insertion;
			} else {
				input = insertion + input;
			}
			
			if (matchOnce) {
				context.putData(EXTRACT_INSERT_MATCHED, EXTRACT_INSERT_MATCHED);
				break;
			}
		}
		
		return input;
	}
	
	public boolean isAny(char c, String s)
	{
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				return true;
			}
		}
		
		return false;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public boolean isMatchOnce() {
		return matchOnce;
	}

	public void setMatchOnce(boolean matchOnce) {
		this.matchOnce = matchOnce;
	}
}
