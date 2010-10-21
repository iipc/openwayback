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
package org.archive.wayback.replay.swf;

import java.io.IOException;
import java.util.List;

import com.flagstone.transform.MovieTag;
import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * @author brad
 *
 */
public class RobustMovieDecoder implements SWFFactory<MovieTag> {
	private SWFFactory<MovieTag> delegate = null;
	private int decodeRule = DECODE_RULE_STRICT;

    /** Decoding robustness/sloppiness factor. */
    public static final int DECODE_RULE = 19;
    /** Allow no unparsed data - very strict decoding. */
    public static final int DECODE_RULE_STRICT = 0;
    /** Allow trailing NULL values in decoded tags. */
    public static final int DECODE_RULE_NULLS = 1;
    /** Allow arbitrary trailing values in decoded tags - attempt anything. */
    public static final int DECODE_RULE_LAX = 2;
    
    /**
     * @param decodeRule the robustness level to use for re-aligning the
     * decoder. MovieDecoder just chokes if a given tag has trailing data - does
     * not need as many bytes as it declares it has. This decorator will attempt
     * to realign by either:
     * 1) LAX - chewing up *any* bytes to realign
     * 2) NULLS - chewing up only trailing NULL bytes trailing the tag
     * 3) STRICT - throw exception if an realignment is required. 
     */
    public void setDecodeRule(int decodeRule) {
    	this.decodeRule = decodeRule;
    }
    
    /**
     * @param delegate the underlying/wrapped MovieDecoder which does the heavy
     * lifting to parse out tags.
     */
    public void setDelegate(SWFFactory<MovieTag> delegate) {
		this.delegate = delegate;
	}
	
	public void getObject(List<MovieTag> list, SWFDecoder coder, Context context)
			throws IOException {
		try {
			delegate.getObject(list, coder, context);
		} catch(CoderException e) {
        	int delta = coder.getDelta();
            switch (decodeRule) {
            case DECODE_RULE_LAX:
            	// just eat the next 'delta' bytes and hope for the best..
            	while(delta-- > 0) {
            		coder.readByte();
            	}
                break;
            case DECODE_RULE_NULLS:
            	// make sure next 'delta' bytes are null:
            	while(delta-- > 0) {
            		if(coder.readByte() != 0) {
                        throw new CoderException(coder.getLocation(),
                                coder.getExpected(), coder.getDelta());
            		}
            	}
                break;
            case DECODE_RULE_STRICT:
            default:
                throw new CoderException(coder.getLocation(),
                        coder.getExpected(), coder.getDelta());
            }
			
		}
		
	}

}
