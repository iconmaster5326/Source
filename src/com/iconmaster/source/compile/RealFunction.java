package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.Function;

/**
 *
 * @author iconmaster
 */
public class RealFunction {
	public Function fn;
	public boolean nameFound;
	public boolean method;

	public RealFunction(Function fn, boolean method, boolean nameFound) {
		this.fn = fn;
		this.nameFound = nameFound;
		this.method = method;
	}

}
