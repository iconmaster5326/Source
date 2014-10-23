package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.prototype.Variable;
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
			fn.varspace.putVar(arg.getName());
		}
		for (Operation op : fn.getCode()) {
			if (op.op==OpType.CALL) {
				if (pkg.getFunction(op.args[1])!=null) {
					Function tfn = pkg.getFunction(op.args[1]);
					if (tfn.getArguments().size()!=op.args.length-2) {
						a.add(new SourceException(op.range,"Illegal argument count for "+op.args[1]));
					}
					fn.varspace.putFunc(tfn);
				} else {
					a.add(new SourceException(op.range,"Undefined function "+op.args[1]));
				}
			}
			
			if (op.op==OpType.DEF) {
				for (String arg : op.args) {
					if (fn.varspace.varsUsed.get(arg)!=null) {
						a.add(new SourceException(op.range,"Variable "+arg+" already defined"));
					} else {
						fn.varspace.putVar(arg);
					}
				}
			} else if (op.op==OpType.BEGIN) {
				fn.varspace = new VarSpace(fn.varspace);
			} else if (op.op==OpType.END) {
				fn.varspace = fn.varspace.parent;
			}
			
			if (op.getVarNames().length!=0) {
				for (String name : op.getVarNames()) {
					if (pkg.getField(name)!=null) {
						fn.varspace.putField(pkg.getField(name));
					} else if (fn.varspace.getVar(name)==null && name!=null && !name.startsWith("%")) {
						a.add(new SourceException(op.range,"Undefined variable "+name));
					}
				}
			}
		}
		return a;
	}
}
