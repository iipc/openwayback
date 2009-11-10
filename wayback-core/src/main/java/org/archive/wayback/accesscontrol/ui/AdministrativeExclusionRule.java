/* AdministrativeExclusionRuling
 *
 * $Id$
 *
 * Created on 11:20:22 AM May 11, 2006.
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;

import org.archive.wayback.util.Timestamp;

/**
 *
 *
 * @deprecated superseded by ExclusionOracle
 * @author brad
 * @version $Date$, $Revision$
 */
public class AdministrativeExclusionRule {

//
//	VALUE: <RULE>[,VALUE]
//	RULE: <TS-START>-<TS-END>:<TYPE>:<MOD>:<WHO>:<WHEN>:<COMMENT>
//	TS-START: [0-9]{0,14}
//	TS-END: [0-9]{0,14}
//	TYPE: ROBOTS|NOROBOTS|EXCLUDE|INCLUDE
//	MOD: ADD|DELETE
//	WHO: hex-encoded email address of person who create this rule
//	WHEN: seconds since epoch when this rule was submitted
//	COMMENT: hex-encoded string arbitrary comment

	private final static char SEPARATOR = ':';
	private final static String ENCODING = "UTF-8";
	
	private final static int TYPE_ROBOTS = 0;
	private final static int TYPE_NOROBOTS = 1;
	private final static int TYPE_INCLUDE = 2;
	private final static int TYPE_EXCLUDE = 3;
	
	private final static int MOD_ADD = 0;
	private final static int MOD_DELETE = 1;
	
	private String startDateStr;
	private String endDateStr;
	private int type;
	private int mod;
	private String who;
	private long when;
	private String why;
	
	/**
	 * @return string "key" including start, end and type information
	 */
	public String key() {
		return startDateStr + SEPARATOR + endDateStr + SEPARATOR + type;
	}
	
	/**
	 * sets type to Robots
	 */
	public void setRobots() {
		type = TYPE_ROBOTS;
	}
	/**
	 * sets type to NoRobots
	 */
	public void setNoRobots() {
		type = TYPE_NOROBOTS;
	}
	/**
	 * sets type to Include
	 */
	public void setInclude() {
		type = TYPE_INCLUDE;
	}
	/**
	 * sets type to Exclude
	 */
	public void setExclude() {
		type = TYPE_EXCLUDE;
	}
	/**
	 * sets mod to ADD
	 */
	public void setAdd() {
		mod = MOD_ADD;
	}
	/**
	 * sets mod to DELETE
	 */
	public void setDelete() {
		mod = MOD_DELETE;
	}
	
	/**
	 * extract values from this object into encoded String representation
	 * 
	 * @return String representation of values in this object
	 */
	public String encode() {
		StringBuilder encoded = new StringBuilder(100);
		try {
			encoded.append(startDateStr).append(SEPARATOR);
			encoded.append(endDateStr).append(SEPARATOR);
			encoded.append(type).append(SEPARATOR);
			encoded.append(mod).append(SEPARATOR);
			encoded.append(URLEncoder.encode(who,ENCODING)).append(SEPARATOR);
			encoded.append(when).append(SEPARATOR);
			encoded.append(URLEncoder.encode(why,ENCODING));
		} catch (UnsupportedEncodingException e) {
			// this should not happen with a hard-coded UTF-8...
			e.printStackTrace();
		}
		return encoded.toString();
	}
	
	/**
	 * set all values from encoded String version
	 * 
	 * @param encoded rule
	 * @throws ParseException if rule cannot be parsed
	 */
	public void decode(final String encoded) throws ParseException {
		String parts[] = encoded.split(String.valueOf(SEPARATOR));
		if(parts.length != 7) {
			throw new ParseException("Unable decode (" + encoded + ")",0);
		}
		startDateStr = Timestamp.padStartDateStr(parts[0]);
		endDateStr = Timestamp.padStartDateStr(parts[1]);
		type = Integer.valueOf(parts[2]).intValue();
		if(type < TYPE_ROBOTS || type > TYPE_EXCLUDE) {
			throw new ParseException("bad type in (" + encoded + ")",3);			
		}
		mod = Integer.valueOf(parts[3]).intValue();
		if(mod < MOD_ADD || mod > MOD_DELETE) {
			throw new ParseException("bad mod in (" + encoded + ")",4);			
		}
		try {
			who = URLDecoder.decode(parts[4],ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage(),5);
		}
		when = Long.valueOf(parts[5]).longValue();
		try {
			why = URLDecoder.decode(parts[6],ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage(),7);
		}
	}
	
	/**
	 * @return Returns the when.
	 */
	public long getWhen() {
		return when;
	}
	/**
	 * @param when The when to set.
	 */
	public void setWhen(long when) {
		this.when = when;
	}
	/**
	 * @return Returns the who.
	 */
	public String getWho() {
		return who;
	}
	/**
	 * @param who The who to set.
	 */
	public void setWho(String who) {
		this.who = who;
	}
	/**
	 * @return Returns the why.
	 */
	public String getWhy() {
		return why;
	}
	/**
	 * @param why The why to set.
	 */
	public void setWhy(String why) {
		this.why = why;
	}
	/**
	 * @return true if this is an ADD rule
	 */
	public boolean isAdd() {
		return mod == MOD_ADD;
	}
	/**
	 * @return true if this is a DELETE rule
	 */
	public boolean isDelete() {
		return mod == MOD_DELETE;
	}
	/**
	 * @return true if this is a ROBOTS rule
	 */
	public boolean isRobots() {
		return type == TYPE_ROBOTS;
	}
	/**
	 * @return true if this is a NOROBOTS rule
	 */
	public boolean isNoRobots() {
		return type == TYPE_NOROBOTS;
	}
	/**
	 * @return true if this is an INCLUDE rule
	 */
	public boolean isInclude() {
		return type == TYPE_INCLUDE;
	}
	/**
	 * @return true if this is an EXCLUDE rule
	 */
	public boolean isExclude() {
		return type == TYPE_EXCLUDE;
	}

	/**
	 * @return String user friendly version of the mod
	 */
	public String getPrettyMod() {
		if(mod == MOD_ADD) {
			return "add";
		}
		return "delete";
	}
	/**
	 * @return pretty String representation of the Type
	 */
	public String getPrettyType() {
		String prettyType = null;
		switch (type) {
		case TYPE_INCLUDE:
			prettyType = "Include";
			break;
		case TYPE_EXCLUDE:
			prettyType = "Exclude";
			break;
		case TYPE_ROBOTS:
			prettyType = "Use Robots";
			break;
		case TYPE_NOROBOTS:
			prettyType = "No Robots";
			break;
		default:
			break;
		} 
		return prettyType;
	}
	
	/**
	 * @return Returns the endDateStr.
	 */
	public String getEndDateStr() {
		return endDateStr;
	}

	/**
	 * @return Returns pretty version of the endDateStr.
	 */
	public String getPrettyEndDateStr() {
		// TODO: Localization
		return Timestamp.parseAfter(endDateStr).prettyDateTime();
	}
	/**
	 * @return Returns pretty version of the startDateStr.
	 */
	public String getPrettyStartDateStr() {
		// TODO: Localization
		return Timestamp.parseBefore(startDateStr).prettyDateTime();
	}

	/**
	 * @param endDateStr The endDateStr to set.
	 */
	public void setEndDateStr(String endDateStr) {
		this.endDateStr = endDateStr;
	}

	/**
	 * @return Returns the startDateStr.
	 */
	public String getStartDateStr() {
		return startDateStr;
	}

	/**
	 * @param startDateStr The startDateStr to set.
	 */
	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
	}
	
	/**
	 * @param arg a dateString (possibly < 14 digits)
	 * @return true if arg is in range to which this rule applies
	 */
	public boolean appliesToDateStr(final String arg) {
		return (arg.compareTo(startDateStr.substring(0,arg.length())) >= 0 )
			&& (arg.compareTo(endDateStr.substring(  0,arg.length())) <= 0 );
	}
	
}
