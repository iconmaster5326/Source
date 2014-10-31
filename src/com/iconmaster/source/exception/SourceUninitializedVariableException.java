package com.iconmaster.source.exception;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class SourceUninitializedVariableException extends SourceException {
	public String var;
	
	public SourceUninitializedVariableException(Range range, String message, String var) {
		super(range,message);
		this.var = var;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}
}
