/* AdministrativeExclusionRules
 *
 * $Id$
 *
 * Created on 12:28:48 PM May 11, 2006.
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
package org.archive.wayback.accesscontrol;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AdministrativeExclusionRules {

	private static String DELIMITER = ",";
	private ArrayList rules = null;
	private String surtPrefix;
	/**
	 * @param surtPrefix
	 */
	public AdministrativeExclusionRules(String surtPrefix) {
		this.surtPrefix = surtPrefix;
	}
	/**
	 * prune down rules to only those which apply for a particular timestamp
	 * first eliminating those outside the timestamp range, and then removing
	 * ADD which have a (subsequent) DELETE
	 * 
	 * @param dateStr
	 * @return ArrayList of applicable and current rules for dateStr
	 */
	public ArrayList filterRules(final String dateStr) {
		
		if(rules == null) {
			return new ArrayList();
		}
		
		// first separate the rules into ADD and DELETE, only keeping the newest
		// for any START-END-TYPE
		
		HashMap adds = new HashMap();
		HashMap deletes = new HashMap();
		HashMap cur = null;
		for(int i = 0; i < rules.size(); i++) {
			AdministrativeExclusionRule rule = 
				(AdministrativeExclusionRule) rules.get(i);
			if(!rule.appliesToDateStr(dateStr)) {
				continue;
			}
			String key = rule.key();
			if(rule.isAdd()) {
				cur = adds;
			} else {
				cur = deletes;
			}
			if(cur.containsKey(key)) {
				AdministrativeExclusionRule last = 
					(AdministrativeExclusionRule) cur.get(key);
				if(rule.getWhen() > last.getWhen()) {
					cur.put(key,rule);
				}
			} else {
				cur.put(key,rule);
			}
		}

		// now, remove any ADD for which there is a later DELETE:

		Iterator itr = deletes.values().iterator();
		while(itr.hasNext()) {
			AdministrativeExclusionRule deleteRule = 
				(AdministrativeExclusionRule) itr.next();
			String key = deleteRule.key();
			if(adds.containsKey(key)) {
				AdministrativeExclusionRule addRule = 
					(AdministrativeExclusionRule) adds.get(key);
				if(deleteRule.getWhen() > addRule.getWhen()) {
					adds.remove(key);
				}
			}
		}
		
		// now the "adds" HashMap contains only rules that apply now, for the
		// current time we are interested in.

		return new ArrayList(adds.values());
	}
	
	/**
	 * finds the most applicable rule for the date in question, and returns it
	 * 
	 * @param dateStr
	 * @return most applicable AdministrativeExclusionRule, or null if none 
	 * applied
	 */
	public AdministrativeExclusionRule getApplicableRule(final String dateStr) {
		ArrayList applicable = filterRules(dateStr);
		// first look for Excludes:
		Iterator itr = applicable.iterator();
		while(itr.hasNext()) {
			AdministrativeExclusionRule rule = 
				(AdministrativeExclusionRule) itr.next();
			if(rule.isExclude()) {
				return rule;
			}
		}
		// then Includes:
		itr = applicable.iterator();
		while(itr.hasNext()) {
			AdministrativeExclusionRule rule = 
				(AdministrativeExclusionRule) itr.next();
			if(rule.isInclude()) {
				return rule;
			}
		}
		// then NoRobots:
		itr = applicable.iterator();
		while(itr.hasNext()) {
			AdministrativeExclusionRule rule = 
				(AdministrativeExclusionRule) itr.next();
			if(rule.isNoRobots()) {
				return rule;
			}
		}
		// then Robots:
		itr = applicable.iterator();
		while(itr.hasNext()) {
			AdministrativeExclusionRule rule = 
				(AdministrativeExclusionRule) itr.next();
			if(rule.isRobots()) {
				return rule;
			}
		}
		// nothing:
		return null;
	}
	
	/**
	 * load rules found in the encoded string argument
	 * 
	 * @param encodedRules
	 */
	public void loadRules(final String encodedRules) {
		rules = new ArrayList();
		String ruleChunks[] = encodedRules.split(DELIMITER);
		for(int i = 0; i < ruleChunks.length; i++) {
			AdministrativeExclusionRule rule = parseRule(ruleChunks[i]);
			if(rule != null) {
				rules.add(rule);
			}
		}
	}

	/**
	 * @param rule
	 */
	public void addRule(AdministrativeExclusionRule rule) {
		if(rules == null) {
			rules = new ArrayList();
		}
		rules.add(rule);
	}
	
	/**
	 * @return String encoded version of the rules.
	 */
	public String encodeRules() {
		if(rules == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder(rules.size() * 120);
		Iterator itr = rules.iterator();
		while(itr.hasNext()) {
			AdministrativeExclusionRule rule = (AdministrativeExclusionRule) 
				itr.next();
			if(builder.length() > 0) {
				builder.append(DELIMITER);
			}
			builder.append(rule.encode());
		}
		return builder.toString();
	}
	
	private AdministrativeExclusionRule parseRule(final String encoded) {
		AdministrativeExclusionRule rule = new AdministrativeExclusionRule();
		try {
			rule.decode(encoded);
			return rule;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @return Returns the surtPrefix.
	 */
	public String getSurtPrefix() {
		return surtPrefix;
	}
	/**
	 * @return Returns the rules.
	 */
	public ArrayList getRules() {
		return rules;
	}
}
