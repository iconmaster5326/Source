package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.Function;

/**
 *
 * @author iconmaster
 */
public class RealFunction {
	public Function fn;
	public boolean method;

	public RealFunction(Function fn, boolean method) {
		this.fn = fn;
		this.method = method;
	}
}
