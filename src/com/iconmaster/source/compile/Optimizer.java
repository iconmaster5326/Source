package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Stack;

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
			return op.op+" "+op.args[0]+":"+used;
		}
	}
	
	public static class OpDataList extends ArrayList<OpData> {
		public HashSet<String> defs = new HashSet<>();
		
		public void addDef(String var) {
			defs.add(var);
		}
		
		public boolean hasDef(String var) {
			return defs.contains(var);
		}
	}
	
	public static class OpDataStack extends Stack<OpDataList> {
		public OpDataList flatten() {
			OpDataList a = new OpDataList();
			for (OpDataList ops : this) {
				a.addAll(ops);
			}
			return a;
		}
		
		public void addDef(String def) {
			peek().addDef(def);
		}
		
		public OpDataList hasDef(String def) {
			for (int i=this.size()-1;i>=0;i--) {
				OpDataList ops = this.get(i);
				if (ops.hasDef(def)) {
					return ops;
				}
			}
			return null;
		}
	}
	
	public static boolean isReplaceAnywhere(SourcePackage pkg, Operation op) {
		switch (op.op) {
			case MOV:
			case MOVN:
			case MOVS:
			case MOVL:
				return true;
			case CALL:
				return Directives.has(pkg.getFunction(op.args[1]),"pure");
			default:
				return false;
		}
	}
	
	public static ArrayList<Operation> optimize(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<OpData> a = new ArrayList<>();
		OpDataStack scopes = new OpDataStack();
		System.out.println(pkg);
		scopes.add(new OpDataList());
		for (Operation op : code) {
			System.out.println(scopes.peek()+" "+scopes.peek().defs);
			if (op.op==OpType.MOV) {
				boolean found = false;
				OpDataList sf = scopes.flatten();
				for (int i=sf.size()-1; i>=0; i--) {
					OpData opd = sf.get(i);
					if (isReplaceAnywhere(pkg, opd.op)) {
						if (opd.op.args[0].equals(op.args[1])) {
							found = true;
							opd.used = true;
							OpData opd2 = new OpData(new Operation(opd.op.op, opd.op.type, op.range, Arrays.copyOf(opd.op.args, opd.op.args.length)), false);
							opd2.op.args[0] = op.args[0];
							if (scopes.hasDef(op.args[0])==null) {
								scopes.addDef(op.args[0]);
							}
							a.add(opd2);
							scopes.hasDef(op.args[0]).add(opd2);
							break;
						}
					}
				}
				if (!found) {
					OpData opd2 = new OpData(op, false);
					if (scopes.hasDef(op.args[0])==null) {
						scopes.addDef(op.args[0]);
					}
					a.add(opd2);
					scopes.hasDef(op.args[0]).add(opd2);
				}
			} else if (op.op==OpType.BEGIN) {
				scopes.add(new OpDataList());
				
				OpData opd2 = new OpData(op, false);
				a.add(opd2);
			} else if (op.op==OpType.END) {
				scopes.pop();
				
				OpData opd2 = new OpData(op, false);
				a.add(opd2);
			} else if (op.op==OpType.DEF) {
				scopes.addDef(op.args[0]);
			} else {
				int argn;
				if (op.op.isMathOp()) {
					argn = 0;
				} else {
					switch (op.op) {
						case CALL:
							argn = 2;
							break;
						case RET:
						case MOVL:
						case IF:
						case WHILE:
						case REP:
							argn = 0;
							break;
						default:
							argn = -1;
					}
				}
				
				if (argn==-1) {
					a.add(new OpData(op, false));
				} else {
					Operation nop = op.cloneOp();
					for (int arg=argn; arg<op.args.length; arg++) {
						OpDataList sf = scopes.flatten();
						for (int i=sf.size()-1; i>=0; i--) {
							Optimizer.OpData opd = sf.get(i);
							if (opd.op.op==OpType.MOV) {
								if (opd.op.args[0].equals(op.args[arg])) {
									opd.used = true;
									nop.args[arg] = opd.op.args[1];
								}
							}
						}
					}
					OpData opd2 = new Optimizer.OpData(nop, false);
					if (scopes.hasDef(op.args[0])==null) {
						scopes.addDef(op.args[0]);
					}
					a.add(opd2);
					scopes.hasDef(op.args[0]).add(opd2);
				}
			}
		}
		ArrayList<Operation> a2 = new ArrayList<>();
		for (OpData opd : a) {
			if (!opd.used) {
				a2.add(opd.op);
			}
		}
		return a2;
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