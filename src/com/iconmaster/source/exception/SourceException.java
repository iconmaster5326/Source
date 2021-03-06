
package com.iconmaster.source.exception;

import com.iconmaster.source.util.Range;

/**
 *
 * @author iconmaster
 */
public class SourceException extends Exception {
	private final Range range;
	
	public SourceException(Range range) {
		super(range.toString()+": Unknown Source exception");
		this.range = range;
	}
	
	public SourceException(Range range, String message) {
		super((range==null?"null: ":range.toString()+": ")+message);
		this.range = range;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}
	
	public Range getRange() {
		return range;
	}
}
