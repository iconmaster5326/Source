package com.iconmaster.source.compile;

import com.iconmaster.source.compile.Operation.OpType;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class CompileUtils {
	public static ArrayList<Operation> replaceWithGotos(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<Operation> a = new ArrayList<>();
		ArrayList<Operation> old = a;
		
		ArrayList<Operation> doBlock = null;
		
		String begin = null;
		String end = null;
		
		boolean isWhile = false;
		ArrayList<Operation> whileBlock = null;
		
		boolean isRep = false;
		String repCond;
		ArrayList<Operation> repBlock = null;
		
		for (int i=0;i<code.size();i++) {
			Operation op = code.get(i);
			switch (op.op) {
				case DO:
					begin = pkg.nameProvider.getTempName();
					doBlock = new ArrayList<>();
					a.add(new Operation(OpType.LABEL, op.range, begin));
					a = doBlock;
					break;
				case WHILE:
					isWhile = true;
					end = pkg.nameProvider.getTempName();
					whileBlock = new ArrayList<>();
					old.addAll(replaceWithGotos(pkg, doBlock));
					String temp = pkg.nameProvider.getTempName();
					old.add(new Operation(OpType.NOT, op.range, temp, op.args[0]));
					old.add(new Operation(OpType.GOTOIF, op.range, temp, end));
					a = whileBlock;
					break;
				case REP:
					isRep = true;
					repCond = op.args[0];
					repBlock = new ArrayList<>();
					a = repBlock;
					break;
				case ENDB:
					if (isWhile) {
						old.addAll(replaceWithGotos(pkg, whileBlock));
						old.add(new Operation(OpType.GOTO, op.range, begin));
						old.add(new Operation(OpType.LABEL, op.range, end));
						a = old;
						isWhile = false;
					}
					if (isRep) {
						old.addAll(repBlock);
						old.addAll(doBlock);
						temp = pkg.nameProvider.getTempName();
						old.add(new Operation(OpType.NOT, op.range, temp, op.args[0]));
						old.add(new Operation(OpType.GOTOIF, op.range, temp, begin));
						old.add(new Operation(OpType.LABEL, op.range, end));
						a = old;
						isRep = false;
					}
					break;
				default:
					a.add(op);
			}
		}
		return a;
	}
	
	public static ArrayList<Operation> replaceBlockScopes(SourcePackage pkg, ArrayList<Operation> code) {
		ArrayList<Operation> a = new ArrayList<>();
		return a;
	}
}
