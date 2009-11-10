/* AdministrativeExclusionServlet
 *
 * $Id$
 *
 * Created on 4:31:48 PM May 12, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.ui;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.surt.SURTTokenizer;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.webapp.ServletRequestContext;

import com.sleepycat.je.DatabaseException;

/**
 * Servlet responsible for UI generation of the Administrative Exclustion 
 * system.
 * 
 * Primarily this includes:
 * 	1) generating query form
 * 	2) displaying current exclusion rules based on queries
 *  3) recieving POST requests from clients to add rules
 *  
 * @deprecated superseded by ExclusionOracle
 * @author brad
 * @version $Date$, $Revision$
 */
public class AdministrativeExclusionServlet extends ServletRequestContext {

	private static final String DEFAULT_USER_AGENT = "ia_archiver";
	private static final String HTML_BR = "<br></br>";
	private static final long serialVersionUID = 1L;

	private static String FORM_OPERATION = "operation";

	private static String MODIFY_FORM_START = "start";
	private static String MODIFY_FORM_END = "end";
	private static String MODIFY_FORM_URL = "modify_url";
	private static String MODIFY_FORM_SURT = "modify_surt";
	private static String MODIFY_FORM_TYPE = "type";
	private static String MODIFY_FORM_MOD = "mod";
	private static String MODIFY_FORM_WHO = "who";
	private static String MODIFY_FORM_WHY = "why";
	private static String MODIFY_FORM_PREFIX = "prefix";
	
	
	private static String DELETE_FORM_OPERATION = "delete";
	private static String MODIFY_FORM_OPERATION = "modify";
	
	private static String MODIFY_FORM_MOD_ADD_VALUE = "add";
	private static String MODIFY_FORM_MOD_DELETE_VALUE = "delete";

	private static String MODIFY_FORM_TYPE_EXCLUDE_VALUE = "exclude";
	private static String MODIFY_FORM_TYPE_INCLUDE_VALUE = "include";
	private static String MODIFY_FORM_TYPE_NOROBOTS_VALUE = "norobots";
	private static String MODIFY_FORM_TYPE_ROBOTS_VALUE = "robots";

	private static String MODIFY_FORM_PREFIX_YES_VALUE = "yes";

	private static String QUERY_FORM_URL = "query_url";
	private static String QUERY_FORM_ALL = "query_all";
	private static String QUERY_FORM_ALL_VALUE = "yes";
	private static String QUERY_FORM_OPERATION = "query";

	private static String CHECK_FORM_URL = "check_url";
	private static String CHECK_FORM_TIMESTAMP = "timestamp";
	private static String CHECK_FORM_OPERATION = "check";
	
	private static final int RULE_STATUS_HEADER = -1;
	private static final int RULE_STATUS_ACTIVE = 0;
	private static final int RULE_STATUS_INACTIVE = 1;
	private static final int RULE_STATUS_DELETE = 2;
	
	private AdministrativeExclusionAuthority exclusionAuthority = null;

	private void showPage(HttpServletResponse response, StringBuilder page) 
	throws IOException {
		response.setContentType("text/html");
		response.getOutputStream().print(page.toString());
	}
	
	private String formatException(Exception e) {
		return e.getMessage();
	}

	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		AdministrativeExclusionAuthority exclAuth = exclusionAuthority;

		StringBuilder page = new StringBuilder(1024);
		page.append("<html><head><title>Wayback: Administrative Exclusions</title></head><body>");
		@SuppressWarnings("unchecked")
		Map<String,String[]> queryArgs = httpRequest.getParameterMap();
		String operation = getMapParam(queryArgs,FORM_OPERATION);
		if(operation == null) {
			page.append(makeCheckForm(queryArgs));
			page.append(makeQueryForm(queryArgs));
			page.append(makeCreateForm(queryArgs));
		} else if(operation.equals(QUERY_FORM_OPERATION)) {
			page.append(makeCheckForm(queryArgs));
			page.append(makeQueryForm(queryArgs));
			try {
				page.append(handleRuleQuery(exclAuth,queryArgs));
			} catch (URIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				page.append(formatException(e));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				page.append(formatException(e));
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				page.append(formatException(e));
			}
			page.append(makeCreateForm(queryArgs));
		} else if(operation.equals(CHECK_FORM_OPERATION)) {
			page.append(makeCheckForm(queryArgs));
			try {
				page.append(handleRuleCheck(exclAuth,queryArgs));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				page.append(formatException(e));
			}
			page.append(makeQueryForm(queryArgs));
			page.append(makeCreateForm(queryArgs));

		} else if((operation.equals(MODIFY_FORM_OPERATION) 
				|| operation.equals(DELETE_FORM_OPERATION))) {
			
			try {
				handleRuleCreate(exclAuth,queryArgs);
				page.append("OK - added rule");
			} catch (URIException e) {
				e.printStackTrace();
				page.append(formatException(e));
			} catch (ParseException e) {
				e.printStackTrace();
				page.append(formatException(e));
			} catch (DatabaseException e) {
				e.printStackTrace();
				page.append(formatException(e));
			}

		} else {
			page.append("Unknown operation");
		}
		page.append("</body></html>");
		showPage(httpResponse,page);
		return true;
	}

	// HTML GENERATION METHODS: 
	
	private String htmlEncode(String orig) {
		return orig;
	}
	private String propertyHTMLEncode(String orig) {
		return orig;
	}
	
	private String makeFormTextInput(String label, String name, 
			Map<String,String[]> queryArgs,
			String suffix) {
		String value = "";
		if(queryArgs != null) {
			value = propertyHTMLEncode(getMapParamOrEmpty(queryArgs,name));
		}
		return label + ":<input type=\"text\" name=\"" + name + "\" VALUE=\"" + 
			 value + "\"></input>" + suffix;		
	}
	private String makeFormTextAreaInput(String label, String name, 
			Map<String,String[]> queryArgs, String suffix) {
		String value = "";
		if(queryArgs != null) {
			value = htmlEncode(getMapParamOrEmpty(queryArgs,name));
		}
		return label + ":<textarea cols=80 rows=4 name=\"" + name + "\">" +
			value + "</textarea>" + suffix;		
	}

	private String makeFormCheckInput(String label, String name, String value, 
			Map<String,String[]> queryArgs, String suffix) {
		String curValue = getMapParam(queryArgs,name);
		String checked = "";
		if(curValue != null && curValue.equals(value)) {
			checked = "checked";
		}
		return label + ":<input type=\"checkbox\" name=\"" + name + "\" VALUE=\"" + 
			 value + "\" " + checked + "></input>" + suffix;
	}

	private String makeHiddenInput(String name, String value) {
		return "<input type=\"hidden\" name=\"" + name + "\" value=\"" + 
			propertyHTMLEncode(value) + "\"></input>";
	}


	private String makeForm(String method, String submitValue, String content) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("<form method=\"").append(method);
		sb.append("\" action=\"admin-exclusion\">\n");
		sb.append(content);
		sb.append("<input type=\"SUBMIT\" name=\"").append(FORM_OPERATION);
		sb.append("\" value=\"").append(submitValue).append("\"></input>\n");
		sb.append("</form>");
		return sb.toString();
	}
	private String makeHeaderForm(String title,String method, String submitValue, 
			String content) {
		return "<hr></hr><p><h2>"+title+"</h2>" + makeForm(method,submitValue,content) + "</p>";
	}

	// CHECK FORM AND HANDLING
	
	private String makeCheckForm(Map<String,String[]> queryArgs) {
		String content = makeFormTextInput("Url",CHECK_FORM_URL,queryArgs," ") +
			makeFormTextInput("Timestamp",CHECK_FORM_TIMESTAMP,queryArgs," ");
		return makeHeaderForm("Test Exclusion","GET",CHECK_FORM_OPERATION,content);
	}

	private String handleRuleCheck(AdministrativeExclusionAuthority excl, 
			Map<String,String[]> queryArgs) throws Exception {
		String url = getRequiredMapParam(queryArgs,CHECK_FORM_URL);
		String timestamp = getRequiredMapParam(queryArgs,CHECK_FORM_TIMESTAMP);
		String userAgent = DEFAULT_USER_AGENT;
		
		ExclusionResponse response = excl.checkExclusion(userAgent,url,timestamp);
		return response.getContentText();
	}

	// RULE QUERY FORM AND HANDLING
	
	private String makeQueryForm(Map<String,String[]> queryArgs) {
		StringBuilder content = new StringBuilder(1024);
		content.append(makeFormTextInput("Url",QUERY_FORM_URL,queryArgs,""));
		content.append(makeFormCheckInput("all",QUERY_FORM_ALL,QUERY_FORM_ALL_VALUE,queryArgs,""));
		return makeHeaderForm("Rule Query","GET",QUERY_FORM_OPERATION,content.toString());
	}

	private String handleRuleQuery(AdministrativeExclusionAuthority excl, 
			Map<String,String[]> queryArgs) throws ParseException, URIException,
			DatabaseException {
		String url = getRequiredMapParam(queryArgs,QUERY_FORM_URL);
		String all = getMapParam(queryArgs,QUERY_FORM_ALL);
		boolean showAll = (all != null && all.equals(QUERY_FORM_ALL_VALUE));
		String surt = SURTTokenizer.prefixKey(url);
		ArrayList<AdministrativeExclusionRules> matching = excl.matchRules(surt);
		StringBuilder matchHTML = new StringBuilder();
		rulesToTable(matchHTML,matching,url,showAll);
		return matchHTML.toString();
	}
	
	private String ruleToType(AdministrativeExclusionRule rule) {
		String type = MODIFY_FORM_TYPE_EXCLUDE_VALUE;
		if(rule.isExclude()) {
			type = MODIFY_FORM_TYPE_EXCLUDE_VALUE;
		} else if(rule.isInclude()) {
			type = MODIFY_FORM_TYPE_INCLUDE_VALUE;			
		} else if(rule.isNoRobots()) {
			type = MODIFY_FORM_TYPE_NOROBOTS_VALUE;			
		} else if(rule.isRobots()) {
			type = MODIFY_FORM_TYPE_ROBOTS_VALUE;			
		}
		return type;
	}

	// RULE DELETION/CREATION FORM AND HANDLING 
	
	private String makeDeleteForm(String surt, AdministrativeExclusionRule rule) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(makeHiddenInput(MODIFY_FORM_SURT,surt));
		sb.append(makeHiddenInput(MODIFY_FORM_START,rule.getStartDateStr()));
		sb.append(makeHiddenInput(MODIFY_FORM_END,rule.getEndDateStr()));
		sb.append(makeHiddenInput(MODIFY_FORM_TYPE,ruleToType(rule)));
		sb.append(makeHiddenInput(MODIFY_FORM_MOD,MODIFY_FORM_MOD_DELETE_VALUE));
		sb.append(makeFormTextInput("Who",MODIFY_FORM_WHO,null," "));
		sb.append(makeFormTextInput("Why",MODIFY_FORM_WHY,null," "));
		return makeForm("POST", DELETE_FORM_OPERATION, sb.toString());
	}
	
	private String makeCreateForm(Map<String,String[]> queryArgs) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(makeFormTextInput("Url",MODIFY_FORM_URL,queryArgs,""));
		sb.append(makeFormCheckInput("(prefix)",MODIFY_FORM_PREFIX,
				MODIFY_FORM_PREFIX_YES_VALUE,queryArgs,HTML_BR));

		sb.append(makeFormTextInput("Start",MODIFY_FORM_START,queryArgs,""));
		sb.append(makeFormTextInput("End",MODIFY_FORM_END,queryArgs,HTML_BR));

		sb.append("Type: <select name=\""+MODIFY_FORM_TYPE+"\">" +
				"<option>"+MODIFY_FORM_TYPE_EXCLUDE_VALUE+"</option>" +
				"<option>"+MODIFY_FORM_TYPE_INCLUDE_VALUE+"</option>" +
				"<option>"+MODIFY_FORM_TYPE_ROBOTS_VALUE+"</option>" +
//				"<option>"+MODIFY_FORM_TYPE_NOROBOTS_VALUE+"</option>" +
			"</select>").append(HTML_BR).append("\n");

//		sb.append("Add/Remove: <select NAME=\""+MODIFY_FORM_MOD+"\">" +
//				"<option>"+MODIFY_FORM_MOD_ADD_VALUE+"</option>" +
//				"<option>"+MODIFY_FORM_MOD_DELETE_VALUE+"</option>" +
//			"</select><br></br>\n");
		sb.append(makeHiddenInput(MODIFY_FORM_MOD,MODIFY_FORM_MOD_ADD_VALUE));
		
		sb.append(makeFormTextInput("Who",MODIFY_FORM_WHO,queryArgs,HTML_BR));
		sb.append(makeFormTextAreaInput("Why",MODIFY_FORM_WHY,queryArgs,HTML_BR));
		
		return makeHeaderForm("Compose Rule","POST",MODIFY_FORM_OPERATION,
				sb.toString());
	}

	private void handleRuleCreate(AdministrativeExclusionAuthority auth,
			Map<String,String[]> queryMap) throws ParseException, URIException, DatabaseException {

		AdministrativeExclusionRule rule = new AdministrativeExclusionRule();

		String start = getRequiredMapParam(queryMap,MODIFY_FORM_START);
		String startDateStr = Timestamp.padStartDateStr(start);
		if(!startDateStr.equals(start)) {
			throw new ParseException("invalid value: " + MODIFY_FORM_START,0);			
		}

		String end = getRequiredMapParam(queryMap,MODIFY_FORM_END);
		String endDateStr = Timestamp.padEndDateStr(end);
		if(!endDateStr.equals(end)) {
			throw new ParseException("invalid value: " + MODIFY_FORM_END,0);			
		}

		String type = getRequiredMapParam(queryMap,MODIFY_FORM_TYPE);
		if(type.equals(MODIFY_FORM_TYPE_EXCLUDE_VALUE)) {
			rule.setExclude();
		} else if(type.equals(MODIFY_FORM_TYPE_INCLUDE_VALUE)) {
			rule.setInclude();
		} else if(type.equals(MODIFY_FORM_TYPE_ROBOTS_VALUE)) {
			rule.setRobots();
		} else if(type.equals(MODIFY_FORM_TYPE_NOROBOTS_VALUE)) {
			rule.setNoRobots();
		} else {
			throw new ParseException("invalid value: " + MODIFY_FORM_TYPE,0);			
		}
		
		String mod = getRequiredMapParam(queryMap,MODIFY_FORM_MOD);
		if(mod.equals(MODIFY_FORM_MOD_ADD_VALUE)) {
			rule.setAdd();
		} else if(mod.equals(MODIFY_FORM_MOD_DELETE_VALUE)) {
			rule.setDelete();
		} else {
			throw new ParseException("invalid value: " + MODIFY_FORM_MOD,0);
		}

		String who = getRequiredMapParam(queryMap,MODIFY_FORM_WHO);
		String why = getRequiredMapParam(queryMap,MODIFY_FORM_WHY);
		
		String url = getMapParam(queryMap,MODIFY_FORM_URL);
		String surt = getMapParam(queryMap,MODIFY_FORM_SURT);
		if(surt == null) {
			if(url == null) {
				throw new ParseException("Missing argument " + MODIFY_FORM_URL,0);
			}
			String prefix = getMapParam(queryMap,MODIFY_FORM_PREFIX);
			if(prefix == null || !prefix.equals(MODIFY_FORM_PREFIX_YES_VALUE)) {
				surt = SURTTokenizer.exactKey(url);
			} else {
				surt = SURTTokenizer.prefixKey(url);
			}			
		}
		rule.setWho(who);
		rule.setWhy(why);
		rule.setWhen(new Date().getTime());
		rule.setStartDateStr(startDateStr);
		rule.setEndDateStr(endDateStr);
		
		auth.addRuleFor(surt,rule);
	}

	
	private int statusForRule(AdministrativeExclusionRule rule,
			AdministrativeExclusionRules rules) {
		int status = RULE_STATUS_ACTIVE;
		if(rule.isDelete()) {
			status = RULE_STATUS_DELETE;
		} else {
			// has rule been deleted?
			String key = rule.key();
			boolean deleted = false;
			
			Iterator<AdministrativeExclusionRule> itr = 
				rules.getRules().iterator();
			while(itr.hasNext()) {
				AdministrativeExclusionRule daRule = 
					(AdministrativeExclusionRule) itr.next();
				if(daRule.isDelete() && key.equals(daRule.key()) && 
						daRule.getWhen() > rule.getWhen()) {
					deleted = true;
					break;
				}
			}
			
			if(deleted) {
				status = RULE_STATUS_INACTIVE;
			}
		}
		return status;
	}
	
	private void tableCell(StringBuilder page,String content, int status) {
		content = htmlEncode(content);
		String style = "";
		if(status == RULE_STATUS_DELETE) {
			style = "style=\"color:red;\"";
		} else if (status == RULE_STATUS_INACTIVE) {
			style = "style=\"text-decoration:line-through;\"";			
		} else if (status == RULE_STATUS_HEADER) {
			style = "style=\"bold:yes;\"";			
		}
		page.append("<td ").append(style).append(">");
		page.append(htmlEncode(content));
		page.append("</td>");
	}

	private void ruleHeaderRow(StringBuilder page) {
		int status = RULE_STATUS_HEADER;
		page.append("<tr>");
		tableCell(page,"Url Prefix",status);
		tableCell(page,"Starts",status);
		tableCell(page,"Ends",status);
		tableCell(page,"Rule Type",status);
		tableCell(page,"Add/Delete",status);
		tableCell(page,"When",status);
		tableCell(page,"Admin",status);
		tableCell(page,"Comment",status);
		page.append("</tr>\n");
	}
	
	private void ruleToRow(StringBuilder page, String surt, 
			AdministrativeExclusionRule rule,int status) {
		String surtPrefix = surt;
		if(surtPrefix.endsWith("\t")) {
			surtPrefix = surtPrefix.substring(0,surtPrefix.length()-1);
		} else {
			surtPrefix = surtPrefix + "*";
		}

		page.append("<tr>");
		tableCell(page,surtPrefix,status);
		tableCell(page,rule.getPrettyStartDateStr(),status);
		tableCell(page,rule.getPrettyEndDateStr(),status);
		tableCell(page,rule.getPrettyType(),status);
		tableCell(page,rule.getPrettyMod(),status);
		int sse = (int) (rule.getWhen()/1000);
		// TODO: Localization
		tableCell(page, Timestamp.fromSse(sse).prettyDateTime(), status);
		tableCell(page,rule.getWho(),status);
		// TODO: shrink
		if(status == RULE_STATUS_ACTIVE) {
			page.append("<td>").append(rule.getWhy()).append(" ").
					append(makeDeleteForm(surt,rule)).append("</td>");
		} else {
			tableCell(page,rule.getWhy(),status);
		}
		page.append("</tr>\n");
	}

	private void rulesToTable(StringBuilder page,
			ArrayList<AdministrativeExclusionRules> matching, String url,
			boolean showAll) {
		
		if(matching.size() > 0) {
			page.append("<table border=1>");
			ruleHeaderRow(page);
			Iterator<AdministrativeExclusionRules> itr = matching.iterator();
			while(itr.hasNext()) {
				AdministrativeExclusionRules rules = itr.next();

				String surtPrefix = rules.getSurtPrefix();
				Iterator<AdministrativeExclusionRule> ruleItr = null;
				if(showAll) {
					ruleItr = rules.getRules().iterator();
				} else {
					ruleItr = rules.filterRules("").iterator();
				}
				while(ruleItr.hasNext()) {
					AdministrativeExclusionRule rule = 
						(AdministrativeExclusionRule) ruleItr.next();
					int status = statusForRule(rule,rules);
					ruleToRow(page,surtPrefix,rule, status);
				}
			}
			page.append("</table>\n");
		} else {
			page.append("<p>No records match("+url+")</p>\n");
		}
	}
}
