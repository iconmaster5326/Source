package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.Arrays;

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
	
	public static boolean isReplaceAnywhere(OpType ot) {
		switch (ot) {
			case MOV:
			case MOVN:
			case MOVS:
				return true;
			default:
				return false;
		}
	}
	
	public static ArrayList<Operation> optimize(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<OpData> a = new ArrayList<>();
		boolean found;
		for (Operation op : code) {
			if (op.op.isMathOp()) {
				Operation nop = op.cloneOp();
				for (int arg=0; arg<op.args.length; arg++) {
					for (int i=a.size()-1; i>=0; i--) {
						OpData opd = a.get(i);
						if (opd.op.op==OpType.MOV) {
							if (opd.op.args[0].equals(op.args[arg])) {
								opd.used = true;
								nop.args[arg] = opd.op.args[1];
							}
						}
					}
				}
				a.add(new OpData(nop, false));
			} else {
				switch (op.op) {
					case CALL:
						Operation nop = op.cloneOp();
						for (int arg=2; arg<op.args.length; arg++) {
							for (int i=a.size()-1; i>=0; i--) {
								OpData opd = a.get(i);
								if (opd.op.op==OpType.MOV) {
									if (opd.op.args[0].equals(op.args[arg])) {
										opd.used = true;
										nop.args[arg] = opd.op.args[1];
										break;
									}
								}
							}
						}
						a.add(new OpData(nop, false));
						break;
					case MOV:
						found = false;
						for (int i=a.size()-1; i>=0; i--) {
							OpData opd = a.get(i);
							if (isReplaceAnywhere(opd.op.op)) {
								if (opd.op.args[0].equals(op.args[1])) {
									found = true;
									opd.used = true;
									OpData opd2 = new OpData(new Operation(opd.op.op, opd.op.type, op.range, Arrays.copyOf(op.args, op.args.length)), false);
									opd2.op.args[1] = opd.op.args[1];
									a.add(opd2);
									break;
								}
							}
						}
						if (!found) {
							a.add(new OpData(op, false));
						}
						break;
					default:
						a.add(new OpData(op, false));
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
}
