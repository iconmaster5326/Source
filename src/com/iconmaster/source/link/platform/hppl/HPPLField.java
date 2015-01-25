package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Field;

/**
 *
 * @author iconmaster
 */
public class HPPLField {
	public String compileName;
	public InlinedExpression expr;
	public Field f;

	public HPPLField(String compileName, InlinedExpression expr, Field f) {
		this.compileName = compileName;
		this.expr = expr;
		this.f = f;
	}
	
	public String output;
	
	public void toString(AssemblyData ad) {
		StringBuilder sb = new StringBuilder();
		
		ad.pushFrame();
		if (PlatformHPPL.shouldExport(f)) {
			sb.append("EXPORT ");
		}
		sb.append(PlatformHPPL.shouldExport(f) ? HPPLNaming.formatVarName(f.getName()) : compileName);
		if (f.getValue()!=null) {
			sb.append("=");
			sb.append(HPPLAssembler.getString(ad, expr));
		} else {
			sb.append(";");
		}
		ad.popFrame();
		
		output = sb.toString();
	}
}
