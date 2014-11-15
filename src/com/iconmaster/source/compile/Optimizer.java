package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

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
	
	public static ArrayList<Operation> optimize(SourcePackage pkg, ArrayList<Operation> code) {
		return code;
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