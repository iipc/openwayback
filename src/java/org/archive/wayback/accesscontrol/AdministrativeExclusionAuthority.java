/* AdministrativeExclusionAuthority
 *
 * $Id$
 *
 * Created on 2:47:39 PM May 10, 2006.
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

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.bdb.BDBRecord;
import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.bdb.BDBRecordIterator;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.surt.SURTTokenizer;

import com.sleepycat.je.DatabaseException;


/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AdministrativeExclusionAuthority implements ExclusionAuthority {


	private static String ADMIN_NO_ROBOTS_MSG = "Administrative Robots Ignore:";
	private static String ADMIN_INCLUDE_MSG = "Administrative Include:";
	private static String ADMIN_EXCLUDE_MSG = "Administrative Exclude:";
	private static String DB_NAME = "adminexclusion.dbname";
	private static String INDEX_PATH = "adminexclusion.dbpath";
	
	RoboCache roboCache;
	BDBRecordSet db;

	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionAuthority#checkExclusion(java.lang.String, java.lang.String, java.lang.String)
	 */
	public ExclusionResponse checkExclusion(String userAgent, String urlString, 
			String captureDate) throws Exception {
		SURTTokenizer tokenizer;
		try {
			tokenizer = new SURTTokenizer(urlString);
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e);
		}

		while(true) {
			String surtPrefix = tokenizer.nextSearch();
			if(surtPrefix == null) {
				break;
			}
			AdministrativeExclusionRule rule = getRuleFor(surtPrefix,captureDate);
			if(rule != null) {
				if(rule.isRobots()) {
					break;
				} else if(rule.isNoRobots()) {
					return new ExclusionResponse("-",
							ExclusionResponse.EXLCUSION_AUTHORITATIVE,
							ExclusionResponse.EXLCUSION_AUTHORIZED,
							ADMIN_NO_ROBOTS_MSG + rule.getWhy());
				} else if(rule.isInclude()) {
					return new ExclusionResponse("-",
							ExclusionResponse.EXLCUSION_AUTHORITATIVE,
							ExclusionResponse.EXLCUSION_AUTHORIZED,
							ADMIN_INCLUDE_MSG +	rule.getWhy());
				} else if(rule.isExclude()) {
					return new ExclusionResponse("-",
							ExclusionResponse.EXLCUSION_AUTHORITATIVE,
							ExclusionResponse.EXLCUSION_NOT_AUTHORIZED,
							ADMIN_EXCLUDE_MSG +	rule.getWhy());
				} else {
					// whoops.. how'd this happen.. just ignore it.
				}
			}
		}
		
		// we only get here when we are suppose to return the value from the
		// current robots.txt document:
		return roboCache.checkExclusion(userAgent,urlString,captureDate);
	}
	
	/**
	 * @param surt
	 * @return String representation of rules
	 * @throws DatabaseException 
	 */
	public ArrayList matchRules(String surt) throws DatabaseException {
		BDBRecordIterator itr = db.recordIterator(surt);
		ArrayList matching = new ArrayList();
		while(itr.hasNext()) {
			BDBRecord record = (BDBRecord) itr.next();
			AdministrativeExclusionRules rules = recordToRules(record);
			if(rules.getSurtPrefix().startsWith(surt)) {
				matching.add(rules);
			}
		}
		return matching;
	}

	private AdministrativeExclusionRules recordToRules(BDBRecord record) {
		String surtPrefix = new String(record.getKey().getData());
		String encodedRules = new String(record.getValue().getData());
		AdministrativeExclusionRules rules = new AdministrativeExclusionRules(surtPrefix);
		rules.loadRules(encodedRules);
		return rules;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {

		String dbPath = (String) p.get(INDEX_PATH);
		if (dbPath == null || (dbPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + INDEX_PATH);
		}
		String dbName = (String) p.get(DB_NAME);
		if (dbName == null || (dbName.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + DB_NAME);
		}

		try {

			db = new BDBRecordSet();
			db.initializeDB(dbPath,dbName);

		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new ConfigurationException(e.getMessage());
		}
		
		roboCache = new RoboCache();
		roboCache.init(p);
	}

	private AdministrativeExclusionRule getRuleFor(final String surtPrefix,
			final String dateStr) {
		AdministrativeExclusionRules rules = new AdministrativeExclusionRules(surtPrefix);
		String encoded = (String) db.get(surtPrefix);
		if(encoded != null) {
			rules.loadRules(encoded);
		}
		return rules.getApplicableRule(dateStr);
	}
	
	/**
	 * @param surtPrefix
	 * @param rule
	 */
	public void addRuleFor(final String surtPrefix, AdministrativeExclusionRule rule) {
		AdministrativeExclusionRules rules = new AdministrativeExclusionRules(surtPrefix);
		String encoded = (String) db.get(surtPrefix);
		if(encoded != null) {
			rules.loadRules(encoded);
		}
		rules.addRule(rule);
		db.put(surtPrefix,rules.encodeRules());
	}
}
