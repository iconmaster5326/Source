package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author iconmaster
 */
public class Optimizer {
	public static class OpData {
		public Operation op;
		public boolean used;

		public OpData(Operation op, boolean used) {
			this.op = op;
			this.used = used;
		}

		@Override
		public String toString() {
			return op.op+":"+used;
		}
	}
	
	public static ArrayList<Operation> optimize(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<OpData> data = new ArrayList<>();
		HashSet<String> defs = new HashSet<>();
		HashSet<String> currdefs = new HashSet<>();
		for (int i=code.size()-1;i>=0;i--) {
			Operation op = code.get(i).cloneOp();
			switch (op.op) {
				case END:
				case BEGIN:
					defs.addAll(currdefs);
					currdefs = new HashSet<>();
					data.add(0,new OpData(op, false));
					break;
				case MOV:
					boolean used = false;
					if (!defs.contains(op.args[0])) {
						for (OpData d : data) {
							int j = 0;
							for (Boolean arg : d.op.getVarSlots()) {
								if (arg && d.op.args[j].equals(op.args[0])) {
									d.op.args[j] = op.args[1];
									used = true;
								}
								j++;
							}
						}
					}
					data.add(0,new OpData(op, used));
					break;
				default:
					data.add(0,new OpData(op, false));
			}
			
			int j = 0;
			for (Boolean arg : op.getVarSlots()) {
				if (j==0 && op.op.hasLVar() && defs.contains(op.args[j])) {
					defs.remove(op.args[j]);
					currdefs.remove(op.args[j]);
				} else if (arg && !defs.contains(op.args[j])) {
					currdefs.add(op.args[j]);
				}
				j++;
			}
		}
		
		ArrayList<Operation> product = new ArrayList<>();
		for (OpData d : data) {
			if (!d.used) {
				product.add(d.op);
			}
		}
		//return product;
		product=removeDeadCode(pkg,product);
		if (code.hashCode()==product.hashCode()) {
			return product;
		} else {
			return optimize(pkg, product);
		}
	}
	
	public static ArrayList<Operation> removeDeadCode(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<Operation> a = new ArrayList<>();
		int i = 0;
		for (Operation op : code) {
			if (isOpCritical(pkg,code,i,op)) {
				a.add(op);
			}
			i++;
		}
		return a;
	}
	
	public static boolean isOpCritical(SourcePackage pkg, ArrayList<Operation> code, int begin, Operation op) {
		switch (op.op) {
			case CALL:
			case RET:
			case WHILE:
			case IF:
			case REP:
			case FORR:
			case FORC:
			case FORP:
			case FORE:
				return true;
			default:
				if (op.op.hasLVar()) {
					return isVarCritical(pkg, code, begin+1, op.args[0]);
				} else {
					return true;
				}
		}
	}
	
	public static boolean isVarCritical(SourcePackage pkg, ArrayList<Operation> code, int begin, String var) {
		for (int i = begin;i<code.size();i++) {
			Operation op = code.get(i);
						
			int arg = 0;
			for (Boolean b : op.getVarSlots()) {
				if (b) {
					if (op.args[arg].equals(var) && isOpCritical(pkg, code, begin, op)) {
						return true;
					}
				}
				arg++;
			}
		}
		return false;
	}
	
	public static void countUsages(SourcePackage pkg) {
		for (Function fn : pkg.getFunctionsAndIterators()) {
			fn.references = 0;
			CompileUtils.transform(pkg, (pkg2, work, code) -> {
				for (Operation op : code) {
					if (op.op == OpType.CALL && op.args[1].equals(fn.getFullName())) {
						fn.references++;
					}
					
					if (op.op == OpType.FORC && op.args[0].equals(fn.getFullName())) {
						fn.references++;
					}
				}
				return code;
			});
		}
	}
}