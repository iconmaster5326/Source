package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;

/**
 *
 * @author iconmaster
 */
public class HPPLFunction {
	public String compileName;
	public String[] args;
	public InlinedExpression expr;
	public Function fn;

	public HPPLFunction(String compileName, String[] args, InlinedExpression expr, Function fn) {
		this.compileName = compileName;
		this.args = args;
		this.expr = expr;
		this.fn = fn;
	}
}
