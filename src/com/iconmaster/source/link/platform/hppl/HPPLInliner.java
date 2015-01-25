package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.InlineOp;
import com.iconmaster.source.link.platform.hppl.InlinedExpression.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

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
		for (Operation oop : ops) {
			InlineOp op = new InlineOp(oop);
			expr.add(op);
		}
		//make tallies of used vars
		for (i=ops.size()-1;i>=0;i--) {
			InlineOp op = expr.get(i);
			
			if (op.op.op.hasLVar() && ad.pkg.getField(op.op.args[0])!=null) { //make fileds always KEEP
				op.addRef();
				op.addRef();
			}
			
			int j = 0;
			for (boolean b : op.op.getVarSlots()) {
				if (b && !(op.op.op.hasLVar() && j==0)) {
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
		
		//be nice, and tell us what blocks match up with what
		Stack<Integer> doStack = new Stack<>();
		Stack<Integer> loopStack = new Stack<>();
		for (int j = 0;j<expr.size();j++) {
			InlineOp op = expr.get(j);
			
			if (op.op.op==OpType.DO) {
				doStack.push(j);
			} else if (op.op.op.isBlockStarter()) {
				int k = doStack.pop();
				expr.get(k).matchingBlock = j;
				loopStack.push(j);
			} else if (op.op.op==OpType.ENDB) {
				int k = loopStack.pop();
				expr.get(k).matchingBlock = j;
				op.matchingBlock = k;
			}
		}
		
		return expr;
	}
	
	public static ArrayList<InlinedExpression> getStatements(InlinedExpression expr) {
		ArrayList<InlinedExpression> a = new ArrayList<>();
		InlinedExpression stat = new InlinedExpression();
		
		for (InlineOp op : expr) {
			stat.add(op);
			if (op.status!=Status.INLINE) {
				a.add(stat);
				stat = new InlinedExpression();
			}
		}
		
		return a;
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
