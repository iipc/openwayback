/* PropertyConfiguration
 *
 * $Id$
 *
 * Created on 5:40:23 PM Apr 2, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.core;

import java.io.File;
import java.util.Properties;

import org.archive.wayback.exception.ConfigurationException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PropertyConfiguration {
	Properties p = null;
	/**
	 * @param p
	 */
	public PropertyConfiguration(Properties p) {
		this.p = p;
	}
	
	private String invalidConfig(String propName) {
		return "Invalid " + propName + " configuration.";
	}
	private String missingConfig(String propName) {
		return "No " + propName + " configuration.";
	}
	/**
	 * @param propName
	 * @param defaultValue
	 * @return int value from the Properties, or defaultValue if
	 * no value is present, or the value is not parseable
	 */
	public int getInt(final String propName, int defaultValue) {
		String intV = (String) p.get(propName);
		if(intV == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(intV);
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}
	/**
	 * @param propName
	 * @return int value from the Properties
	 * @throws ConfigurationException if not present, or unparseable.
	 */
	public int getInt(final String propName) throws ConfigurationException {
		String intV = (String) p.get(propName);
		if(intV == null) {
			throw new ConfigurationException(missingConfig(propName));
		}
		int i = 0;
		try {
			i = Integer.parseInt(intV);
		} catch(NumberFormatException e) {
			throw new ConfigurationException(invalidConfig(propName));
		}
		return i;
	}

	/**
	 * @param propName
	 * @param defaultValue
	 * @return int value from the Properties, or defaultValue if
	 * no value is present, or the value is not parseable
	 */
	public long getLong(final String propName, long defaultValue) {
		String longV = (String) p.get(propName);
		if(longV == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(longV);
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}
	/**
	 * @param propName
	 * @return long value from the Properties
	 * @throws ConfigurationException if not present, or unparseable.
	 */
	public long getLong(final String propName) throws ConfigurationException {
		String longV = (String) p.get(propName);
		if(longV == null) {
			throw new ConfigurationException(missingConfig(propName));
		}
		long l = 0;
		try {
			l = Long.parseLong(longV);
		} catch(NumberFormatException e) {
			throw new ConfigurationException(invalidConfig(propName));
		}
		return l;
	}
	/**
	 * @param propName
	 * @param defaultValue
	 * @return String value for propName from the Properties, or defaultValue
	 * if no value is found, or the value is empty.
	 * @throws ConfigurationException
	 */
	public String getString(final String propName, final String defaultValue) 
		throws ConfigurationException {
		
		String stringV = (String) p.get(propName);
		if((stringV == null) || (stringV.length() == 0)) {
			if(defaultValue == null) {
				throw new ConfigurationException(missingConfig(propName));
			}
			return defaultValue;
		}
		return stringV;
	}
	/**
	 * @param propName
	 * @return String value for propName from the Properties,
	 * @throws ConfigurationException
	 */
	public String getString(final String propName) throws ConfigurationException {
		return getString(propName,null);
	}
	/**
	 * @param propName
	 * @param defaultValue
	 * @param createIfMissing
	 * @return File for directory specified in Properties by propName
	 * @throws ConfigurationException if defaultValue is null and property 
	 * is missing, or if a configuration is found, but no directory exists (and
	 * createIfMissing is false) or if a configuration is found, but the 
	 * directory cannot be created.
	 */
	public File getDir(final String propName, final String defaultValue, 
			boolean createIfMissing) throws ConfigurationException {

		String stringPath = getString(propName,defaultValue);
		File dir = new File(stringPath);
		if(!dir.exists()) {
			if(createIfMissing) {
				if(!dir.mkdirs()) {
					throw new ConfigurationException("Unable to mkdirs(" + 
							stringPath + ")"); 
				}
			} else {
				throw new ConfigurationException("Missing directory(" + 
						stringPath + ")"); 				
			}
		}
		return dir;
	}
	/**
	 * @param propName
	 * @param defaultValue
	 * @return File for directory specified in Properties by propName
	 * @throws ConfigurationException if the directory cannot be created.
	 */
	public File getDir(final String propName, final String defaultValue) 
		throws ConfigurationException {

		return getDir(propName,defaultValue,false);
	}
	/**
	 * @param propName
	 * @param createIfMissing
	 * @return File for directory specified in Properties by propName
	 * @throws ConfigurationException if no configuration is found, or if 
	 * createIfMissing is true, but the directory cannot be created.
	 */
	public File getDir(final String propName, boolean createIfMissing) 
		throws ConfigurationException {
		
		return getDir(propName,null,createIfMissing);
	}
	/**
	 * @param propName
	 * @return File for directory specified in Properties by propName
	 * @throws ConfigurationException if no property is found, or if the directory
	 * does not exist.
	 */
	public File getDir(final String propName) throws ConfigurationException {
		return getDir(propName,null,false);
	}
	
	/**
	 * @param propName
	 * @param defaultValue
	 * @return File pointing to configuration for propName, or defaultValue if
	 * no configuration is found
	 * @throws ConfigurationException if the File pointed to by configuration
	 * does not exist.
	 */
	public File getFile(final String propName, final String defaultValue) 
		throws ConfigurationException {
		
		String stringPath = getString(propName,defaultValue);
		File file = new File(stringPath);
		if(!file.exists()) {
			throw new ConfigurationException("No file at " + stringPath + 
					" for configuration " + propName);
		}
		return file;
	}
	/**
	 * @param propName
	 * @return File pointed to by configuration propName
	 * @throws ConfigurationException if there is no configuration, or the file
	 * does not exist.
	 */
	public File getFile(final String propName) throws ConfigurationException {
		return getFile(propName,null);
	}
}
