package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.Function;

/**
 *
 * @author iconmaster
 */
public class RealFunction {
	public Function fn;
	public boolean nameFound;
	public boolean method = false;
	public String methodOf;

	public RealFunction(Function fn, String methodOf, boolean nameFound) {
		this.fn = fn;
		this.nameFound = nameFound;
		if (methodOf!=null) {
			method = true;
			this.methodOf = methodOf;
		}
	}

}
