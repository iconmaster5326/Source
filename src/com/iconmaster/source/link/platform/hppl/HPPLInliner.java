package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.InlineOp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class HPPLInliner {
	public static InlinedExpression inlineCode(AssemblyData ad, ArrayList<Operation> ops) {
		//make map of l-vars to where they appear
		HashMap<String,ArrayList<Integer>> refMap = new HashMap<>();
		int i = 0;
		for (Operation op : ops) {
			if (op.op.hasLVar()) {
				if (!refMap.containsKey(op.args[0])) {
					refMap.put(op.args[0], new ArrayList<>());
				}
				refMap.get(op.args[0]).add(i);
			}
			i++;
		}
		
		//create new inlined expression
		InlinedExpression expr = new InlinedExpression();
		for (i=ops.size()-1;i>=0;i--) {
			InlineOp op = new InlineOp(ops.get(i));
			expr.add(op);
			int j = 0;
			for (boolean b : op.op.getVarSlots()) {
				if (b && j!=0) {
					int pos = findLine(refMap, op.op.args[j], i);
					if (pos!=-1) {
						expr.get(pos).addRef();
					}
				}
				j++;
			}
		}
		
		//set inline status for stuff
		for (InlineOp op : expr) {
			op.setStatus();
		}
		
		return expr;
	}
	
	public static ArrayList<InlinedExpression> getStatements(InlinedExpression expr) {
		return null;
	}
	
	public static int findLine(HashMap<String,ArrayList<Integer>> refMap, String var, int pos) {
		ArrayList<Integer> refs = refMap.get(var);
		if (refs==null) {
			return -1;
		}
		for (int i=refs.size()-1;i>=0;i--) {
			if (refs.get(i)<pos) {
				return refs.get(i);
			}
		}
		return -1;
	}
}
