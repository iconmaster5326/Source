package com.iconmaster.source.assemble;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class AssemblyUtils {
	public static boolean isFirstRef(SourcePackage pkg, ArrayList<Operation> code, Operation thisOp, String var) {
		if (pkg.getField(var)!=null) {
			return false;
		}
		for (Operation op : (ArrayList<Operation>)code.clone()) {
			if (op.op.hasLVar()) {
				String opVar = op.args[0];
				if (opVar.equals(var)) {
					return op==thisOp;
				}
			}
		}
		return false;
	}
	
	public static ArrayList<Operation> getReferences(SourcePackage pkg, ArrayList<Operation> code, String var) {
		ArrayList<Operation> a = new ArrayList<>();
		for (Operation op : code) {
			for (String arg : op.getVarNames()) {
				if (arg.equals(var)) {
					a.add(op);
				}
			}
		}
		return a;
	}
	
	public static ArrayList<Operation> getLReferences(SourcePackage pkg, ArrayList<Operation> code, String var) {
		ArrayList<Operation> a = new ArrayList<>();
		for (Operation op : code) {
			if (op.op.hasLVar() && op.args[0].equals(var)) {
				a.add(op);
			}
		}
		return a;
	}
	
	public static ArrayList<Operation> getRReferences(SourcePackage pkg, ArrayList<Operation> code, String var) {
		ArrayList<Operation> a = new ArrayList<>();
		for (Operation op : code) {
			int i = 0;
			for (String arg : op.getVarNames()) {
				if (arg.equals(var) && !(op.op.hasLVar() && i==0)) {
					a.add(op);
				}
				i++;
			}
		}
		return a;
	}
}
