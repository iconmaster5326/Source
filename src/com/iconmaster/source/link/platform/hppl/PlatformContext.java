package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.Operation;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class PlatformContext {
	public ArrayList<Operation> expr;
	public Operation op;
	public StringBuilder sb;
	public AssemblyData ad;

	public PlatformContext(ArrayList<Operation> expr, Operation op, StringBuilder sb, AssemblyData ad) {
		this.expr = expr;
		this.op = op;
		this.sb = sb;
		this.ad = ad;
	}
}
