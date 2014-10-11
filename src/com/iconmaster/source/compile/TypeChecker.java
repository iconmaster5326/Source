package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.Variable;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class TypeChecker {
	public static ArrayList<SourceException> check(SourcePackage pkg) {
		ArrayList<SourceException> a = new ArrayList<>();
		for (Function fn : pkg.getFunctions()) {
			if (!fn.isLibrary()) {
				a.addAll(checkFunction(pkg,fn));
			}
		}
		return a;
	}
	
	public static ArrayList<SourceException> checkField(SourcePackage pkg, Variable var) {
		ArrayList<SourceException> a = new ArrayList<>();
		return a;
	}
	
	public static ArrayList<SourceException> checkFunction(SourcePackage pkg, Function fn) {
		ArrayList<SourceException> a = new ArrayList<>();
		for (Variable arg : fn.getArguments()) {
			fn.varspace.putVar(arg);
		}
		for (Operation op : fn.getCode()) {
			if (op.op==OpType.CALL) {
				if (pkg.getFunction(op.args[1])!=null) {
					fn.varspace.putFunc(pkg.getFunction(op.args[1]));
				} else {
					a.add(new SourceException(new Range(0,1),"Undefined function "+op.args[1]));
				}
			}
			if (op.op.hasLVar()) {
				if (pkg.getField(op.args[0])!=null) {
					fn.varspace.putField(pkg.getField(op.args[0]));
				} else if (fn.varspace.getVar(op.args[0])==null) {
					fn.varspace.putVar(new Variable(op.args[0]));
				}
			}
		}
		return a;
	}
}
