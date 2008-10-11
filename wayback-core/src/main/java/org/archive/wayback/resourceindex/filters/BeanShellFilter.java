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
