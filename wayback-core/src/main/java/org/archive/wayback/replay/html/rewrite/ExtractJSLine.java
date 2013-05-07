package org.archive.wayback.replay.html.rewrite;

import org.archive.wayback.replay.html.ReplayParseContext;

public class ExtractJSLine extends RewriteRule {

	private String line;
	private String replaceNext;
	
	public String getLine() {
		return line;
	}


	public void setLine(String line) {
		this.line = line;
	}

	public String getReplaceNext() {
		return replaceNext;
	}


	public void setReplaceNext(String replaceNext) {
		this.replaceNext = replaceNext;
	}
	
	@Override
	public String rewrite(ReplayParseContext context, String policy,
			String input) {
		
		int index = input.indexOf(line);
		
		if (index >= 0) {
			int endOfLine = input.indexOf("\n", index);
			String replaceStr;
			
			if (endOfLine < 0) {
				replaceStr = input.substring(index);
			} else {
				replaceStr = input.substring(index, endOfLine);
			}
			
			if (replaceNext != null) {
				String nextLine = replaceNext;
				return nextLine.replace("$NEXTLINE", replaceStr);
			} else {
				return replaceStr;
			}
		}
		
		return input;
	}
}
