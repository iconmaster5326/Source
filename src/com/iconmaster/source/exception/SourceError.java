package com.iconmaster.source.exception;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class SourceError {
	public static enum ErrorType {
		GENERAL,UNKNOWN_SYMBOL
	}
	
	public Range range;
	public String msg;
	public ErrorType type = ErrorType.GENERAL;

	public SourceError(Range range, String msg) {
		this.range = range;
		this.msg = msg;
	}

	public SourceError(ErrorType type, Range range, String msg) {
		this.range = range;
		this.msg = msg;
		this.type = type;
	}

	@Override
	public String toString() {
		return "SourceError{" + "range=" + range + ", msg='" + msg + "', type=" + type + '}';
	}
}
