package com.iconmaster.source.exception;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class SourceSyntaxException extends SourceException {
	public SourceSyntaxException(Range range, String message) {
		super(range,message);
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}
}
