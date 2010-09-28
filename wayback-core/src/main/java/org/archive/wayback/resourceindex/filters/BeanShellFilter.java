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
package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellFilter implements ObjectFilter<CaptureSearchResult> {
	
    private String expression = null;
    private String method = null;
	private String scriptPath = null;

    @SuppressWarnings("unchecked")
	private final ThreadLocal tl = new ThreadLocal() {
        protected synchronized Object initialValue() {
        	return new Interpreter();
        }
    };
    private Interpreter getInterpreter() {
    	Interpreter i = (Interpreter) tl.get();
    	if(method != null) {
    		
    	}
    	return i;
    }
	
	public BeanShellFilter() {
	}

	public int filterObject(CaptureSearchResult o) {
		int result = FILTER_EXCLUDE;
		try {
			boolean bResult = false;
		    Interpreter interpreter = getInterpreter();
			interpreter.set("result", o);

		    if(expression != null) {
				bResult = (Boolean) interpreter.eval(expression);
			} else if(method != null) {
				bResult = (Boolean) interpreter.eval("matches(result)");
			} else if(scriptPath != null) {
				bResult = (Boolean) interpreter.eval("matches(result)");				
			}
			
			if(bResult) {
				result = FILTER_INCLUDE;
			}
			
		} catch (EvalError e) {
            e.printStackTrace();
		}
		return result;
	}

    public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getScriptPath() {
		return scriptPath;
	}

	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}
}
