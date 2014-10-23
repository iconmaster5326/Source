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
			} else if (op.op==OpType.DEF) {
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
			} else if (op.op==OpType.PROP) {
				String var = op.args[0];
				for (int i=1;i<op.args.length;i++) {
					String dir = op.args[i];
					if ("const".equals(dir)) {
						fn.varspace.putConst(var);
					}
				}
			}
			
			if (op.op.hasLVar() && fn.varspace.getConst(op.args[0])!=null) {
				Boolean result = fn.varspace.putConstValue(op.args[0]);
				if (result==null) {
					a.add(new SourceException(op.range,"Constant "+op.args[0]+" cannot be assigned to in this scope"));
				} else if (result) {
					a.add(new SourceException(op.range,"Constant "+op.args[0]+" already has a value"));
				}
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
