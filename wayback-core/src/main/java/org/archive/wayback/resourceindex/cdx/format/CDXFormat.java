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
package org.archive.wayback.resourceindex.cdx.format;

import org.archive.wayback.core.CaptureSearchResult;

/**
 * Class which allows serialization/deserialization of CaptureSearchResult
 * objects into/out of a single line String representation.
 * 
 * 
 * @author brad
 *
 */
public class CDXFormat {

	/*
	 * A canonized url
	 * B news group
	 * C rulespace category ***
	 * D compressed dat file offset
	 * F canonized frame
	 * G multi-columm language description (* soon)
	 * H canonized host
	 * I canonized image
	 * J canonized jump point
	 * K Some weird FBIS what's changed kinda thing
	 * L canonized link
	 * M meta tags (AIF) *
	 * N massaged url
	 * P canonized path
	 * Q language string
	 * R canonized redirect
	 * U uniqness ***
	 * V compressed arc file offset *
	 * X canonized url in other href tages
	 * Y canonized url in other src tags
	 * Z canonized url found in script
	 * a original url **
	 * b date **
	 * c old style checksum *
	 * d uncompressed dat file offset
	 * e IP **
	 * f frame *
	 * g file name
	 * h original host
	 * i image *
	 * j original jump point
	 * k new style checksum *
	 * l link *
	 * m mime type of original document *
	 * n arc document length *
	 * o port
	 * p original path
	 * r redirect *
	 * s response code *
	 * t title *
	 * v uncompressed arc file offset *
	 * x url in other href tages *
	 * y url in other src tags *
	 * z url found in script *
	 * # comment
	 *
	 * * in alexa-made dat file
	 * ** in alexa-made dat file meta-data line
	 * *** future data
	 */

	protected CDXField[] fields = null;
	protected char delimiter = ' ';
	protected String delimiterS = null;

	public static String CDX_MAGIC = " CDX";
	
	public static char URL_KEY = 'A';
	public static char TIMESTAMP = 'b';
	public static char ORIGINAL_URL = 'a';
	public static char MIME_TYPE = 'm';
	public static char HTTP_CODE = 's';
	public static char DIGEST = 'k';
	public static char REDIRECT = 'r';
	public static char ROBOT_FLAGS = 'M';
	public static char COMPRESSED_OFFSET = 'V';
	public static char COMPRESSED_LENGTH = 'n';
	public static char FILE = 'g';
	
	/**
	 * Construct a CDXFormat reader/writer based on the specification argument
	 * @param cdxSpec
	 * @throws CDXFormatException
	 */
	public CDXFormat(String cdxSpec) throws CDXFormatException {
		if(!cdxSpec.startsWith(CDX_MAGIC)) {
			throw new CDXFormatException("Spec '" + cdxSpec 
					+ "' does not start with '" + CDX_MAGIC + "'");
		}
		delimiter = cdxSpec.charAt(CDX_MAGIC.length());
		String fieldsString = cdxSpec.substring(CDX_MAGIC.length()+1);
		int fieldCount = (fieldsString.length() + 1) / 2;
		if(fieldsString.length() != (fieldCount * 2) - 1) {
			throw new CDXFormatException("Extra char after spec '"
					+ cdxSpec + "'");
		}
		fields = new CDXField[fieldCount];
		for(int i = 0; i < fieldCount; i++) {
			char f = fieldsString.charAt(i * 2);
			if(i < fieldCount - 1) {
				char d = fieldsString.charAt((i*2)+1);
				if(d != delimiter) {
					throw new CDXFormatException("Non-delimiter char in '" 
							+ fieldsString + "'");
				}
			}
			fields[i] = getField(f);
		}
		delimiterS = new String(""+delimiter);
	}
	
	protected CDXField getField(char fieldChar) throws CDXFormatException {
		CDXField field = null;
		switch (fieldChar) {
			case 'A':  field = new URLKeyCDXField(); break;
			// backvards compat with Alexa tools:
			case 'N':  field = new URLKeyCDXField(); break;
    		case 'b':  field = new TimestampCDXField(); break;
    		case 'a':  field = new OriginalURLCDXField(); break;
    		case 'm':  field = new MIMETypeCDXField(); break;
    		case 's':  field = new HTTPCodeCDXField(); break;
    		case 'k':  field = new DigestCDXField(); break;
    		case 'r':  field = new RedirectURLCDXField(); break;
    		case 'M':  field = new RobotFlagsCDXField(); break;
    		case 'V':  field = new StartOffsetCDXField(); break;
    		// Experimental..
    		case 'S':  field = new CompressedLengthCDXField(); break;
    		case 'g':  field = new FilenameCDXField(); break;
		}
		if(field == null) {
			throw new CDXFormatException("Unknown field '"+fieldChar+"'");
		}
		return field;
	}
	
	/**
	 * @param line
	 * @return CaptureSearchResult containing data from the 'line' argument
	 *         parsed according the the specification for this CDXFormat
	 * @throws CDXFormatException
	 */
	public CaptureSearchResult parseResult(String line) 
	throws CDXFormatException {
		CaptureSearchResult result = new CaptureSearchResult();
		String[] parts = line.split(delimiterS);

		if(parts.length != fields.length) {
			throw new CDXFormatException("Wrong number of fields");
		}
		for(int i = 0; i < fields.length; i++) {
			fields[i].apply(parts[i], result);
		}
		return result;
	}

	/**
	 * @param result
	 * @return String representation of the data in 'result' formatted according
	 *         to the specification for this CDXFormat
	 */
	public String serializeResult(CaptureSearchResult result) {
		StringBuilder sb = new StringBuilder(100);
		for(int i = 0; i < fields.length; i++) {
			String value = fields[i].serialize(result);
			if((value == null) || (value.length() == 0)) {
				sb.append(CDXField.DEFAULT_VALUE);
			} else {
				sb.append(value);
			}
			if(i < fields.length - 1) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
}
