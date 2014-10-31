package com.iconmaster.source.exception;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class SourceUndefinedFunctionException extends SourceException {
	public String var;
	
	public SourceUndefinedFunctionException(Range range, String message, String var) {
		super(range,message);
		this.var = var;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}
}
