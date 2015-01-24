package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class HPPLFunction {
	public String compileName;
	public ArrayList<HPPLVariable> args;
	public ArrayList<InlinedExpression> code;
	public Function fn;

	public HPPLFunction(String compileName, ArrayList<HPPLVariable> args, ArrayList<InlinedExpression> expr, Function fn) {
		this.compileName = compileName;
		this.args = args;
		this.code = expr;
		this.fn = fn;
	}
}
