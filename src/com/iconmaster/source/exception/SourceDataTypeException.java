package com.iconmaster.source.exception;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class SourceDataTypeException extends SourceException {
	public SourceDataTypeException(Range range, String message) {
		super(range,message);
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}
}
