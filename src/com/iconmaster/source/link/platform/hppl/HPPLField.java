package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.Field;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class HPPLField {
	public String compileName;
	public ArrayList<InlinedExpression> expr;
	public Field f;

	public HPPLField(String compileName, ArrayList<InlinedExpression> expr, Field f) {
		this.compileName = compileName;
		this.expr = expr;
		this.f = f;
	}
	
	public String output;
	
	public void toString(AssemblyData ad) {
		
	}
}
