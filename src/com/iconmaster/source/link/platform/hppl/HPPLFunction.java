package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class HPPLFunction {
	public String compileName;
	public ArrayList<HPPLVariable> args;
	public InlinedExpression code;
	public Function fn;

	public HPPLFunction(String compileName, ArrayList<HPPLVariable> args, InlinedExpression expr, Function fn) {
		this.compileName = compileName;
		this.args = args;
		this.code = expr;
		this.fn = fn;
	}
	
	public String output;
	
	public void toString(AssemblyData ad) {
		boolean oldM = ad.minify;
		ad.minify = !Directives.has(fn, "!minify");
		
		StringBuilder sb = new StringBuilder();
		
		if (PlatformHPPL.shouldExport(fn)) {
			sb.append("EXPORT ");
		}
		
		sb.append(compileName);
		sb.append("(");
		if (!args.isEmpty()) {
			for (HPPLVariable var : args) {
				sb.append(var.compileName);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length()-1);
		}
		if (ad.minify) {
			sb.append(")BEGIN ");
		} else {
			sb.append(")\nBEGIN\n");
		}
		
		ad.pushFrame();
		for (HPPLVariable var : args) {
			ad.frame().localVars.add(var);
		}
		sb.append(HPPLAssembler.getString(ad, code));
		ad.popFrame();
		
		if (!ad.minify) {
			sb.append("\n");
		}
		
		sb.append("END;");
		output = sb.toString();
		
		ad.minify = oldM;
	}
}
