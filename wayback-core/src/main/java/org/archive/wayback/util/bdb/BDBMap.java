/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.util.bdb;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.archive.wayback.util.Timestamp;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Generic class for simple key-value pair lookup using BDBJE.
 *
 * @author oskar.grenholm@kb.se
 * @version $Date$, $Revision$
 */
public class BDBMap {

    // Acts as a mapping between an ID and a timestamp to surf at.
    // The dir should probably be configurable somehow.
    private static String BDB_DIR = System.getProperty("java.io.tmpdir") +
    	"/wayback/bdb";
    private static Properties bdbMaps = new Properties();

    protected Environment env = null;
    protected Database db = null;
    protected String name;
    protected String dir;
    
    /**
     * consturctor
     * @param name of database
     * @param dir path of directory where dbd files should be stored. The 
     * directory is created if it does not exist.
     */
    public BDBMap(String name, String dir) {
        this.name = name;
        this.dir = dir;
        init();        
    }
    
    protected void init() {
        try {
            EnvironmentConfig envConf = new EnvironmentConfig();
            envConf.setAllowCreate(true);
            File envDir = new File(dir);
            if (!envDir.exists())
                envDir.mkdirs();
            env = new Environment(envDir, envConf);
            
            DatabaseConfig dbConf = new DatabaseConfig();
            dbConf.setAllowCreate(true);
            dbConf.setSortedDuplicates(false);
            db = env.openDatabase(null, name, dbConf);  
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * persistantly store key-value pair 
     * @param keyStr
     * @param valueStr
     */
    public void put(String keyStr, String valueStr) {
        try {
            DatabaseEntry key = new DatabaseEntry(keyStr.getBytes("UTF-8"));
            DatabaseEntry data = new DatabaseEntry(valueStr.getBytes("UTF-8"));
            db.put(null, key, data);            
         } catch (DatabaseException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * retrieve the value assoicated with keyStr from persistant storage
     * @param keyStr
     * @return String value associated with key, or null if no key is found
     * or an error occurs
     */
    public String get(String keyStr) {
        String result = null;
        try {
            DatabaseEntry key = new DatabaseEntry(keyStr.getBytes("UTF-8"));
            DatabaseEntry data = new DatabaseEntry();
            if (db.get(null, key, data, LockMode.DEFAULT) == 
            	OperationStatus.SUCCESS) {
            	
                byte[] bytes = data.getData();
                result = new String(bytes, "UTF-8");
            }
         } catch (DatabaseException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    
	/**
	 * @param context
	 * @return singleton BDBMap for the context
	 */
	public static BDBMap getContextMap(String context) {
    	if(context == null) context = "";
    	if(context.startsWith("/")) {
    		context = context.substring(1);
    	}
		BDBMap map = null;
    	synchronized(BDBMap.class) {
    		if(!bdbMaps.containsKey(context)) {
    			File bdbDir = new File(BDB_DIR,context);
    			bdbMaps.put(context,new BDBMap(context, 
    					bdbDir.getAbsolutePath()));
    		}
    		map = (BDBMap) bdbMaps.get(context);
    	}
    	return map;
	}
    /**
     * return the timestamp associated with the identifier argument, or now
     * if no value is associated or something goes wrong.
     * @param context 
     * @param ip
     * @return timestamp string value
     */
    public static String getTimestampForId(String context, String ip) {
    	BDBMap bdbMap = getContextMap(context);
        String dateStr = bdbMap.get(ip);
        return (dateStr != null) ? dateStr : Timestamp.currentTimestamp().getDateStr();
    }
    
   /**
    * associate timestamp time with idenfier ip persistantly 
    * @param context 
    * @param ip
    * @param time
    */
    public static void addTimestampForId(String context, String ip, String time) {
    	BDBMap bdbMap = getContextMap(context);
    	bdbMap.put(ip, time);
    }
}
