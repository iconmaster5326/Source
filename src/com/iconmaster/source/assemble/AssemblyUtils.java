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
}
