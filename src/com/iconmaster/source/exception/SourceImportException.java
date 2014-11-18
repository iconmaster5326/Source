package com.iconmaster.source.exception;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class SourceImportException extends SourceException {
	public String var;
	
	public SourceImportException(Range range, String message, String var) {
		super(range,message);
		this.var = var;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}
}
